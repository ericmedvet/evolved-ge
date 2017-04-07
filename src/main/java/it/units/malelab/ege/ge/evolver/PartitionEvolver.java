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
import it.units.malelab.ege.core.selector.FirstBest;
import it.units.malelab.ege.core.selector.Selector;
import it.units.malelab.ege.ge.genotype.Genotype;
import it.units.malelab.ege.ge.operator.GeneticOperator;
import it.units.malelab.ege.ge.GEIndividual;
import it.units.malelab.ege.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
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
    Utils.broadcast(new EvolutionStartEvent<>((List) all(partitionedPopulation), lastBroadcastGeneration, this, null), (List) listeners);
    Utils.broadcast(new GenerationEvent<>((List) all(partitionedPopulation), lastBroadcastGeneration, this, null), (List) listeners);
    //iterate
    while (Math.round(births / configuration.getPopulationSize()) < configuration.getNumberOfGenerations()) {
      int currentGeneration = (int) Math.floor(births / configuration.getPopulationSize());
      tasks.clear();
      //re-rank
      configuration.getProblem().getIndividualRanker().rank((List) all(partitionedPopulation));
      //produce offsprings
      int i = 0;
      while (i < configuration.getOffspringSize()) {
        GeneticOperator<G> operator = Utils.selectRandom(configuration.getOperators(), random);
        List<GEIndividual<G, T, F>> parents = new ArrayList<>(operator.getParentsArity());
        for (int j = 0; j < operator.getParentsArity(); j++) {
          parents.add(selectRepresenter(partitionedPopulation, configuration.getParentSelector()));
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
        while (!partitionedPopulation.isEmpty()) {
          Selector<GEIndividual<G, T, F>> bestSelector = new FirstBest<>();
          if (newPartitionedPopulation.size() >= configuration.getPopulationSize()) {
            break;
          }
          List<GEIndividual<G, T, F>> bestOldPartition = selectPartition(partitionedPopulation, bestSelector);
          partitionedPopulation.remove(bestOldPartition);
          for (GEIndividual<G, T, F> individual : bestOldPartition) {
            addToPartition(newPartitionedPopulation, individual);
          }
        }
        partitionedPopulation = newPartitionedPopulation;
      }
      //re-rank
      configuration.getProblem().getIndividualRanker().rank((List) all(partitionedPopulation));
      //select survivals
      while (partitionedPopulation.size() > configuration.getPopulationSize()) {
        List<GEIndividual<G, T, F>> partition = selectPartition(partitionedPopulation, configuration.getUnsurvivalSelector());
        partitionedPopulation.remove(partition);
      }
      if ((int) Math.floor(births / configuration.getPopulationSize()) > lastBroadcastGeneration) {
        lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
        Utils.broadcast(new GenerationEvent<>((List) all(partitionedPopulation), lastBroadcastGeneration, this, null), (List) listeners);
      }
    }
    //end
    Utils.broadcast(new EvolutionEndEvent<>((List) all(partitionedPopulation), configuration.getNumberOfGenerations(), this, null), (List) listeners);
    executor.shutdown();
    configuration.getProblem().getIndividualRanker().rank((List) all(partitionedPopulation));
    List<Node<T>> bestPhenotypes = new ArrayList<>();
    for (GEIndividual<G, T, F> individual : all(partitionedPopulation)) {
      if (individual.getRank() == 0) {
        bestPhenotypes.add(individual.getPhenotype());
      }
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
          GEIndividual<G, T, F> toRemove = configuration.getUnsurvivalSelector().select(partition);
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

  private List<GEIndividual<G, T, F>> all(List<List<GEIndividual<G, T, F>>> partitions) {
    List<GEIndividual<G, T, F>> all = new ArrayList<>(partitions.size());
    for (List<GEIndividual<G, T, F>> partition : partitions) {
      all.addAll(partition);
    }
    return all;
  }

  private List<GEIndividual<G, T, F>> selectPartition(List<List<GEIndividual<G, T, F>>> partitions, Selector<GEIndividual<G, T, F>> selector) {
    GEIndividual<G, T, F> selected = selectRepresenter(partitions, selector);
    for (List<GEIndividual<G, T, F>> partition : partitions) {
      if (partition.contains(selected)) {
        return partition;
      }
    }
    return Collections.EMPTY_LIST;
  }

  private GEIndividual<G, T, F> selectRepresenter(List<List<GEIndividual<G, T, F>>> partitions, Selector<GEIndividual<G, T, F>> selector) {
    List<GEIndividual<G, T, F>> representers = new ArrayList<>(partitions.size());
    for (List<GEIndividual<G, T, F>> partition : partitions) {
      representers.add(configuration.getRepresenterSelector().select(partition));
    }
    return selector.select(representers);
  }

}
