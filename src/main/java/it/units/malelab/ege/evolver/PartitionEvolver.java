/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver;

import it.units.malelab.ege.ge.evolver.StandardEvolver;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import it.units.malelab.ege.core.grammar.Node;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.evolver.event.EvolutionEndEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.ge.genotype.Genotype;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.ge.operator.GeneticOperator;
import it.units.malelab.ege.evolver.selector.Best;
import it.units.malelab.ege.evolver.selector.First;
import it.units.malelab.ege.core.selector.IndividualComparator;
import it.units.malelab.ege.evolver.selector.Selector;
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
public class PartitionEvolver<G extends Genotype, T> extends StandardEvolver<G, T> {

  private final PartitionConfiguration<G, T> configuration;

  private final Selector<Individual<G, T>> representerSelector;
  private final Selector<List<Individual<G, T>>> bestPartitionSelector;

  public PartitionEvolver(int numberOfThreads, PartitionConfiguration<G, T> configuration, Random random, boolean saveAncestry) {
    super(numberOfThreads, configuration, random, saveAncestry);
    this.configuration = configuration;
    this.representerSelector = new First<>();
    this.bestPartitionSelector = new RepresenterBasedListSelector<>(
            new First<>(),
            new Best(new IndividualComparator(IndividualComparator.Attribute.FITNESS))
    );
  }

  @Override
  public void go(List<EvolutionListener<G, T>> listeners) throws InterruptedException, ExecutionException {
    LoadingCache<G, Pair<Node<T>, Map<String, Object>>> mappingCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(getMappingCacheLoader());
    LoadingCache<Node<T>, Fitness> fitnessCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(getFitnessCacheLoader());
    //initialize population
    int births = 0;
    List<Callable<List<Individual<G, T>>>> tasks = new ArrayList<>();
    for (G genotype : configuration.getPopulationInitializer().getGenotypes(configuration.getPopulationSize(), configuration.getInitGenotypeValidator())) {
      tasks.add(individualFromGenotypeCallable(genotype, 0, mappingCache, fitnessCache, listeners, null, null));
      births = births + 1;
    }
    List<List<Individual<G, T>>> partitionedPopulation = new ArrayList<>();
    for (Individual<G, T> individual : Utils.getAll(executor.invokeAll(tasks))) {
      addToPartition(partitionedPopulation, individual);
    }
    int lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
    broadcastGenerationEvent(partitionedPopulation, lastBroadcastGeneration, listeners);
    //iterate
    while (Math.round(births / configuration.getPopulationSize()) < configuration.getNumberOfGenerations()) {
      int currentGeneration = (int) Math.floor(births / configuration.getPopulationSize());
      tasks.clear();
      //produce offsprings
      int i = 0;
      while (i < configuration.getOffspringSize()) {
        GeneticOperator<G> operator = Utils.selectRandom(configuration.getOperators(), random);
        List<Individual<G, T>> parents = new ArrayList<>(operator.getParentsArity());
        for (int j = 0; j < operator.getParentsArity(); j++) {
          parents.add(
                  configuration.getParentSelector().select(
                          configuration.getParentPartitionSelector().select(partitionedPopulation)
                  )
          );
        }
        tasks.add(operatorApplicationCallable(operator, parents, currentGeneration, mappingCache, fitnessCache, listeners));
        i = i + operator.getChildrenArity();
      }
      List<Individual<G, T>> newPopulation = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
      births = births + newPopulation.size();
      //build new population
      if (configuration.isOverlapping()) {
        for (Individual<G, T> individual : newPopulation) {
          addToPartition(partitionedPopulation, individual);
        }
      } else {
        List<List<Individual<G, T>>> newPartitionedPopulation = new ArrayList<>();
        for (Individual<G, T> individual : newPopulation) {
          addToPartition(newPartitionedPopulation, individual);
        }
        while (!partitionedPopulation.isEmpty()) {
          if (newPartitionedPopulation.size() >= configuration.getPopulationSize()) {
            break;
          }
          List<Individual<G, T>> bestOldPartition = bestPartitionSelector.select(partitionedPopulation);
          partitionedPopulation.remove(bestOldPartition);
          for (Individual<G, T> individual : bestOldPartition) {
            addToPartition(newPartitionedPopulation, individual);
          }
        }
        partitionedPopulation = newPartitionedPopulation;
      }
      //select survivals
      while (partitionedPopulation.size() > configuration.getPopulationSize()) {
        List<Individual<G, T>> partition = configuration.getUnsurvivalPartitionSelector().select(partitionedPopulation);
        partitionedPopulation.remove(partition);
      }
      if ((int) Math.floor(births / configuration.getPopulationSize()) > lastBroadcastGeneration) {
        lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
        broadcastGenerationEvent(partitionedPopulation, lastBroadcastGeneration, listeners);
      }
    }
    //end
    Utils.broadcast(new EvolutionEndEvent<>(representers(partitionedPopulation), configuration.getNumberOfGenerations(), this, null), listeners);
    executor.shutdown();
  }

  private void broadcastGenerationEvent(List<List<Individual<G, T>>> partitionedPopulation, int generation, List<EvolutionListener<G, T>> listeners) {
    //Utils.broadcast(new GenerationEvent<>(representers(partitionedPopulation), generation, this, (Map)Collections.singletonMap("pop", "representers")), listeners);
    //Utils.broadcast(new GenerationEvent<>(all(partitionedPopulation), generation, this, (Map)Collections.singletonMap("pop", "all")), listeners);
    Utils.broadcast(new GenerationEvent<>(all(partitionedPopulation), generation, this, null), listeners);
  }

  private void addToPartition(List<List<Individual<G, T>>> partitionedPopulation, Individual<G, T> individual) {
    boolean found = false;
    for (List<Individual<G, T>> partition : partitionedPopulation) {
      Individual<G, T> representer = representerSelector.select(partition);
      if (configuration.getPartitionerComparator().compare(individual, representer) == 0) {
        found = true;
        partition.add(individual);
        while (partition.size() > configuration.getPartitionSize()) {
          Individual<G, T> toRemove = configuration.getUnsurvivalSelector().select(partition);
          partition.remove(toRemove);
        }
        break;
      }
    }
    if (!found) {
      List<Individual<G, T>> newPartition = new ArrayList<>();
      newPartition.add(individual);
      partitionedPopulation.add(newPartition);
    }
  }

  private List<Individual<G, T>> representers(List<List<Individual<G, T>>> partitions) {
    List<Individual<G, T>> representers = new ArrayList<>(partitions.size());
    for (List<Individual<G, T>> partition : partitions) {
      representers.add(representerSelector.select(partition));
    }
    return representers;
  }
  
  private List<Individual<G, T>> all(List<List<Individual<G, T>>> partitions) {
    List<Individual<G, T>> all = new ArrayList<>(partitions.size());
    for (List<Individual<G, T>> partition : partitions) {
      all.addAll(partition);
    }
    return all;
  }

}
