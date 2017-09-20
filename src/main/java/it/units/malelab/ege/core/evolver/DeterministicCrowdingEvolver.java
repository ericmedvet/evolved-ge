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
import static it.units.malelab.ege.core.evolver.StandardEvolver.CACHE_SIZE;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.event.EvolutionEndEvent;
import it.units.malelab.ege.core.listener.event.EvolutionStartEvent;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.util.Pair;
import it.units.malelab.ege.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author eric
 */
public class DeterministicCrowdingEvolver<G, T, F extends Fitness> extends StandardEvolver<G, T, F> {

  private final DeterministicCrowdingConfiguration<G, T, F> configuration;

  public DeterministicCrowdingEvolver(DeterministicCrowdingConfiguration<G, T, F> configuration, boolean saveAncestry) {
    super(configuration, saveAncestry);
    this.configuration = configuration;
  }    

  @Override
  public List<Node<T>> solve(ExecutorService executor, Random random, List<EvolverListener<G, T, F>> listeners) throws InterruptedException, ExecutionException {
    LoadingCache<G, Pair<Node<T>, Map<String, Object>>> mappingCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).recordStats().build(getMappingCacheLoader());
    LoadingCache<Node<T>, F> fitnessCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).recordStats().build(getFitnessCacheLoader());
    //initialize population
    int births = 0;
    List<Callable<List<Individual<G, T, F>>>> tasks = new ArrayList<>();
    for (G genotype : configuration.getPopulationInitializer().build(configuration.getPopulationSize(), configuration.getInitGenotypeValidator(), random)) {
      tasks.add(individualFromGenotypeCallable(genotype, 0, mappingCache, fitnessCache, listeners, null, null, executor));
      births = births + 1;
    }
    List<Individual<G, T, F>> population = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
    int lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
    Utils.broadcast(new EvolutionStartEvent<>(this, cacheStats(mappingCache, fitnessCache)), listeners, executor);
    Utils.broadcast(new GenerationEvent<>(configuration.getRanker().rank(population, random), lastBroadcastGeneration, this, cacheStats(mappingCache, fitnessCache)), listeners, executor);
    //iterate
    while (Math.round(births / configuration.getPopulationSize()) < configuration.getNumberOfGenerations()) {
      int currentGeneration = (int) Math.floor(births / configuration.getPopulationSize());
      tasks.clear();
      //re-rank
      List<List<Individual<G, T, F>>> rankedPopulation = configuration.getRanker().rank(population, random);
      //produce offsprings once
      GeneticOperator<G> operator = Utils.selectRandom(configuration.getOperators(), random);
      List<Individual<G, T, F>> parents = new ArrayList<>(operator.getParentsArity());
      for (int j = 0; j < operator.getParentsArity(); j++) {
        parents.add(configuration.getParentSelector().select(rankedPopulation, random));
      }
      tasks.add(operatorApplicationCallable(operator, parents, random, currentGeneration, mappingCache, fitnessCache, listeners, executor));
      List<Individual<G, T, F>> children = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
      births = births + children.size();
      //replace
      for (Individual<G, T, F> child : children) {
        //find closest parent
        int closestParentIndex = 0;
        if (parents.size()>1) {
          double closestParentDistance = configuration.getIndividualDistance().d(child, parents.get(0));
          for (int j = 1; j<parents.size(); j++) {
            double distance = configuration.getIndividualDistance().d(child, parents.get(j));
            if (distance<closestParentDistance) {
              closestParentDistance = distance;
              closestParentIndex = j;
            }
          }
        }
        //replace if better
        List<Individual<G,T,F>> competitors = new ArrayList<>(2);
        competitors.add(child);
        competitors.add(parents.get(closestParentIndex));
        List<List<Individual<G,T,F>>> rankedCompetitors = configuration.getRanker().rank(competitors, random);
        if ((rankedCompetitors.get(0).size()==1)&&(rankedCompetitors.get(0).contains(child))) {
          population.remove(parents.get(closestParentIndex));
          if (population.size()<configuration.getPopulationSize()) {
            population.add(child);
          }
        }
      }
      if ((int) Math.floor(births / configuration.getPopulationSize()) > lastBroadcastGeneration) {
        lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
        Utils.broadcast(new GenerationEvent<>((List) rankedPopulation, lastBroadcastGeneration, this, cacheStats(mappingCache, fitnessCache)), listeners, executor);
      }
    }
    //end
    executor.shutdown();
    List<Node<T>> bestPhenotypes = new ArrayList<>();
    List<List<Individual<G, T, F>>> rankedPopulation = configuration.getRanker().rank(population, random);
    Utils.broadcast(new EvolutionEndEvent<>((List) rankedPopulation, configuration.getNumberOfGenerations(), this, cacheStats(mappingCache, fitnessCache)), listeners, executor);
    for (Individual<G, T, F> individual : rankedPopulation.get(0)) {
      bestPhenotypes.add(individual.getPhenotype());
    }
    return bestPhenotypes;
  }

}
