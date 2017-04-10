/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.evolver;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import it.units.malelab.ege.core.grammar.Node;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.event.EvolutionEndEvent;
import it.units.malelab.ege.core.listener.event.EvolutionStartEvent;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import it.units.malelab.ege.core.ranker.Ranker;
import it.units.malelab.ege.core.selector.FirstBest;
import it.units.malelab.ege.core.selector.Selector;
import it.units.malelab.ege.ge.genotype.Genotype;
import it.units.malelab.ege.ge.operator.GeneticOperator;
import it.units.malelab.ege.ge.GEIndividual;
import it.units.malelab.ege.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class PartitionEvolver<G extends Genotype, T, F extends Fitness> extends StandardEvolver<G, T, F> {

  private final PartitionConfiguration<G, T, F> configuration;

  public PartitionEvolver(int numberOfThreads, PartitionConfiguration<G, T, F> configuration, Random random, boolean saveAncestry) {
    super(numberOfThreads, configuration, random, saveAncestry);
    this.configuration = configuration;
  }

  //TODO
  // - likely to modify: partition should be ranked internally according to a parent/unsurvival ranker to be specified in the configuration
  // - modify population parameter in broadcast call 
  @Override
  public List<Node<T>> solve(List<EvolverListener<T, F>> listeners) throws InterruptedException, ExecutionException {
    LoadingCache<G, Pair<Node<T>, Map<String, Object>>> mappingCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(getMappingCacheLoader());
    LoadingCache<Node<T>, F> fitnessCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(getFitnessCacheLoader());
    //initialize population
    int births = 0;
    List<Callable<List<GEIndividual<G, T, F>>>> tasks = new ArrayList<>();
    for (G genotype : configuration.getPopulationInitializer().getGenotypes(configuration.getPopulationSize(), configuration.getInitGenotypeValidator())) {
      tasks.add(individualFromGenotypeCallable(genotype, 0, mappingCache, fitnessCache, listeners, null, null));
      births = births + 1;
    }
    List<List<GEIndividual<G, T, F>>> partitionedPopulation = new ArrayList<>();
    for (GEIndividual<G, T, F> individual : Utils.getAll(executor.invokeAll(tasks))) {
      addToPartition(partitionedPopulation, individual);
    }
    int lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
    Utils.broadcast(new EvolutionStartEvent<>(this, null), (List) listeners);
    Utils.broadcast(new GenerationEvent<>((List) all(partitionedPopulation), lastBroadcastGeneration, this, null), (List) listeners);
    //iterate
    while (Math.round(births / configuration.getPopulationSize()) < configuration.getNumberOfGenerations()) {
      int currentGeneration = (int) Math.floor(births / configuration.getPopulationSize());
      tasks.clear();
      //re-rank
      Map<GEIndividual<G, T, F>, List<GEIndividual<G, T, F>>> representedPartitions = representedPartitions(configuration.getParentRepresenterSelector(), partitionedPopulation);
      List<List<GEIndividual<G, T, F>>> rankedRepresenters = rankRepresenters(
              (Ranker)configuration.getProblem().getIndividualRanker(),
              representedPartitions);
      //produce offsprings
      int i = 0;
      while (i < configuration.getOffspringSize()) {
        GeneticOperator<G> operator = Utils.selectRandom(configuration.getOperators(), random);
        List<GEIndividual<G, T, F>> parents = new ArrayList<>(operator.getParentsArity());
        for (int j = 0; j < operator.getParentsArity(); j++) {
          parents.add(configuration.getParentSelector().select(rankedRepresenters));
        }
        tasks.add(operatorApplicationCallable(operator, parents, currentGeneration, mappingCache, fitnessCache, listeners));
        i = i + operator.getChildrenArity();
      }
      List<GEIndividual<G, T, F>> newPopulation = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
      births = births + newPopulation.size();
      //build new population
      if (configuration.isOverlapping()) {
        for (GEIndividual<G, T, F> individual : newPopulation) {
          addToPartition(partitionedPopulation, individual);
        }
      } else {
        List<List<GEIndividual<G, T, F>>> newPartitionedPopulation = new ArrayList<>();
        for (GEIndividual<G, T, F> individual : newPopulation) {
          addToPartition(newPartitionedPopulation, individual);
        }
        //keep missing individuals from old population
        Selector<GEIndividual<G, T, F>> bestSelector = new FirstBest<>();
        List<GEIndividual<G, T, F>> availableRepresenters = all(rankedRepresenters);
        while (!availableRepresenters.isEmpty()) {
          if (newPartitionedPopulation.size() >= configuration.getPopulationSize()) {
            break;
          }
          GEIndividual<G, T, F> toAddRepresenter = bestSelector.select(spread(availableRepresenters));
          availableRepresenters.remove(toAddRepresenter);
          for (GEIndividual<G, T, F> individual : representedPartitions.get(toAddRepresenter)) {
            addToPartition(newPartitionedPopulation, individual);
          }
        }
        partitionedPopulation = newPartitionedPopulation;
      }
      //select survivals
      while (partitionedPopulation.size() > configuration.getPopulationSize()) {
        //re-rank
        representedPartitions = representedPartitions(configuration.getUnsurvivalRepresenterSelector(), partitionedPopulation);
        rankedRepresenters = rankRepresenters(
                (Ranker)configuration.getProblem().getIndividualRanker(),
                representedPartitions);
        partitionedPopulation.remove(representedPartitions.get(configuration.getUnsurvivalSelector().select(rankedRepresenters)));
      }
      if ((int) Math.floor(births / configuration.getPopulationSize()) > lastBroadcastGeneration) {
        lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
        Utils.broadcast(new GenerationEvent<>((List) all(partitionedPopulation), lastBroadcastGeneration, this, null), (List) listeners);
      }
    }
    //end
    Utils.broadcast(new EvolutionEndEvent<>((List) all(partitionedPopulation), configuration.getNumberOfGenerations(), this, null), (List) listeners);
    executor.shutdown();
    List<Node<T>> bestPhenotypes = new ArrayList<>();
    List<List<GEIndividual<G, T, F>>> rankedPopulation = configuration.getProblem().getIndividualRanker().rank((List)all(partitionedPopulation));
    for (GEIndividual<G, T, F> individual : rankedPopulation.get(0)) {
      bestPhenotypes.add(individual.getPhenotype());
    }
    return bestPhenotypes;
  }

  private void addToPartition(List<List<GEIndividual<G, T, F>>> partitionedPopulation, GEIndividual<G, T, F> individual) {
    boolean found = false;
    for (List<GEIndividual<G, T, F>> partition : partitionedPopulation) {
      if (configuration.getPartitionerComparator().compare(individual, partition.get(0)) == 0) {
        found = true;
        partition.add(individual);
        while (partition.size() > configuration.getPartitionSize()) {
          GEIndividual<G, T, F> toRemove = configuration.getUnsurvivalSelector().select(spread(partition));
          partition.remove(toRemove);
        }
        break;
      }
    }
    if (!found) {
      List<GEIndividual<G, T, F>> newPartition = new ArrayList<>();
      newPartition.add(individual);
      partitionedPopulation.add(newPartition);
    }
  }

  private static <K> List<K> all(List<List<K>> partitions) {
    List<K> all = new ArrayList<>(partitions.size());
    for (List<K> partition : partitions) {
      all.addAll(partition);
    }
    return all;
  }

  private static <K> List<List<K>> rankRepresenters(Ranker<K> ranker, Map<K, List<K>> representedPartitions) {
    return ranker.rank(new ArrayList<>(representedPartitions.keySet()));
  }
  
  private static <K> Map<K, List<K>> representedPartitions(Selector<K> selector, List<List<K>> partitions) {
    Map<K, List<K>> representedPartitions = new LinkedHashMap<>();
    for (List<K> partition : partitions) {
      representedPartitions.put(selector.select(spread(partition)), partition);
    }
    return representedPartitions;
  }
  
  private static <K> List<List<K>> spread(List<K> ks) {
    List<List<K>> kss = new ArrayList<>(ks.size());
    for (K k : ks) {
      kss.add(Collections.singletonList(k));
    }
    return kss;
  }

}
