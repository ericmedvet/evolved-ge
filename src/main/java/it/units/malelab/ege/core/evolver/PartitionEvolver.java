/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.event.EvolutionEndEvent;
import it.units.malelab.ege.core.listener.event.EvolutionStartEvent;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import it.units.malelab.ege.core.ranker.Ranker;
import it.units.malelab.ege.core.selector.Selector;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.util.Pair;
import java.util.ArrayList;
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
public class PartitionEvolver<G, T, F extends Fitness> extends StandardEvolver<G, T, F> {

  private final PartitionConfiguration<G, T, F> configuration;

  public PartitionEvolver(int numberOfThreads, PartitionConfiguration<G, T, F> configuration, Random random, boolean saveAncestry) {
    super(numberOfThreads, configuration, random, saveAncestry);
    this.configuration = configuration;
  }

  @Override
  public List<Node<T>> solve(List<EvolverListener<G, T, F>> listeners) throws InterruptedException, ExecutionException {
    LoadingCache<G, Pair<Node<T>, Map<String, Object>>> mappingCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(getMappingCacheLoader());
    LoadingCache<Node<T>, F> fitnessCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(getFitnessCacheLoader());
    //initialize population
    int births = 0;
    List<Callable<List<Individual<G, T, F>>>> tasks = new ArrayList<>();
    for (G genotype : configuration.getPopulationInitializer().build(configuration.getPopulationSize(), configuration.getInitGenotypeValidator())) {
      tasks.add(individualFromGenotypeCallable(genotype, 0, mappingCache, fitnessCache, listeners, null, null));
      births = births + 1;
    }
    List<List<Individual<G, T, F>>> partitionedPopulation = new ArrayList<>();
    for (Individual<G, T, F> individual : Utils.getAll(executor.invokeAll(tasks))) {
      addToPartition(partitionedPopulation, individual);
    }
    //trim partitions
    trimPartitions(partitionedPopulation);
    int lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
    Utils.broadcast(new EvolutionStartEvent<>(this, cacheStats(mappingCache, fitnessCache)), (List) listeners);
    Utils.broadcast(new GenerationEvent<>(configuration.getRanker().rank(all(partitionedPopulation)), lastBroadcastGeneration, this, cacheStats(mappingCache, fitnessCache)), (List) listeners);
    //iterate
    while (Math.round(births / configuration.getPopulationSize()) < configuration.getNumberOfGenerations()) {
      int currentGeneration = (int) Math.floor(births / configuration.getPopulationSize());
      tasks.clear();
      //re-rank
      Map<Individual<G, T, F>, List<Individual<G, T, F>>> parentRepresentedPartitions = representedPartitions(
              configuration.getParentInPartitionRanker(),
              configuration.getParentInPartitionSelector(),
              partitionedPopulation);
      List<List<Individual<G, T, F>>> parentRankedRepresenters = rankRepresenters(
              (Ranker) configuration.getRanker(),
              parentRepresentedPartitions);
      //produce offsprings
      int i = 0;
      while (i < configuration.getOffspringSize()) {
        GeneticOperator<G> operator = Utils.selectRandom(configuration.getOperators(), random);
        List<Individual<G, T, F>> parents = new ArrayList<>(operator.getParentsArity());
        for (int j = 0; j < operator.getParentsArity(); j++) {
          parents.add(configuration.getParentSelector().select(parentRankedRepresenters));
        }
        tasks.add(operatorApplicationCallable(operator, parents, currentGeneration, mappingCache, fitnessCache, listeners));
        i = i + operator.getChildrenArity();
      }
      List<Individual<G, T, F>> newPopulation = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
      births = births + newPopulation.size();
      //build new population
      if (configuration.isOverlapping()) {
        for (Individual<G, T, F> individual : newPopulation) {
          addToPartition(partitionedPopulation, individual);
        }
      } else {
        List<List<Individual<G, T, F>>> newPartitionedPopulation = new ArrayList<>();
        for (Individual<G, T, F> individual : newPopulation) {
          addToPartition(newPartitionedPopulation, individual);
        }
        //keep missing individuals from old population
        for (List<Individual<G, T, F>> oldRank : parentRankedRepresenters) {
          if (newPartitionedPopulation.size() >= configuration.getPopulationSize()) {
            break;
          }
          for (Individual<G, T, F> oldRepresenter : oldRank) {
            if (newPartitionedPopulation.size() >= configuration.getPopulationSize()) {
              break;
            }
            for (Individual<G, T, F> oldIndividual : parentRepresentedPartitions.get(oldRepresenter)) {
              if (newPartitionedPopulation.size() >= configuration.getPopulationSize()) {
                break;
              }
              addToPartition(newPartitionedPopulation, oldIndividual);
            }
          }
        }
        partitionedPopulation = newPartitionedPopulation;
      }
      //select survivals
      while (partitionedPopulation.size() > configuration.getPopulationSize()) {
      //re-rank
      Map<Individual<G, T, F>, List<Individual<G, T, F>>> unsurvivalRepresentedPartitions = representedPartitions(
              configuration.getUnsurvivalInPartitionRanker(),
              configuration.getUnsurvivalInPartitionSelector(),
              partitionedPopulation);
      List<List<Individual<G, T, F>>> unsurvivalRankedRepresenters = rankRepresenters(
              (Ranker) configuration.getRanker(),
              unsurvivalRepresentedPartitions);
        partitionedPopulation.remove(unsurvivalRepresentedPartitions.get(configuration.getUnsurvivalSelector().select(unsurvivalRankedRepresenters)));
      }
      //trim partitions
      trimPartitions(partitionedPopulation);
      if ((int) Math.floor(births / configuration.getPopulationSize()) > lastBroadcastGeneration) {
        lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
        Utils.broadcast(new GenerationEvent<>(configuration.getRanker().rank(all(partitionedPopulation)), lastBroadcastGeneration, this, cacheStats(mappingCache, fitnessCache)), (List) listeners);
      }
    }
    //end
    Utils.broadcast(new EvolutionEndEvent<>(configuration.getRanker().rank(all(partitionedPopulation)), configuration.getNumberOfGenerations(), this, cacheStats(mappingCache, fitnessCache)), (List) listeners);
    executor.shutdown();
    List<Node<T>> bestPhenotypes = new ArrayList<>();
    List<List<Individual<G, T, F>>> rankedPopulation = configuration.getRanker().rank(all(partitionedPopulation));
    for (Individual<G, T, F> individual : rankedPopulation.get(0)) {
      bestPhenotypes.add(individual.getPhenotype());
    }
    return bestPhenotypes;
  }

  private void trimPartitions(List<List<Individual<G, T, F>>> partitionedPopulation) {
    for (List<Individual<G, T, F>> partition : partitionedPopulation) {
      while (partition.size()>configuration.getPartitionSize()) {
        List<List<Individual<G, T, F>>> rankedPartition = configuration.getUnsurvivalInPartitionRanker().rank(partition);
        Individual<G, T, F> unsurvival = configuration.getUnsurvivalInPartitionSelector().select(rankedPartition);
        partition.remove(unsurvival);
      }
    }
  }

  private void addToPartition(List<List<Individual<G, T, F>>> partitionedPopulation, Individual<G, T, F> individual) {
    boolean found = false;
    for (List<Individual<G, T, F>> partition : partitionedPopulation) {
      if (configuration.getPartitionerComparator().compare(individual, partition.get(0)) == 0) {
        found = true;
        partition.add(individual);
        break;
      }
    }
    if (!found) {
      List<Individual<G, T, F>> newPartition = new ArrayList<>();
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

  private static <K> Map<K, List<K>> representedPartitions(Ranker<K> ranker, Selector<K> selector, List<List<K>> partitions) {
    Map<K, List<K>> representedPartitions = new LinkedHashMap<>();
    for (List<K> partition : partitions) {      
      representedPartitions.put(selector.select(ranker.rank(partition)), partition);
    }
    return representedPartitions;
  }

}
