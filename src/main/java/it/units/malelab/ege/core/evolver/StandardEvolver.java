/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver;

import it.units.malelab.ege.core.fitness.Fitness;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.event.BirthEvent;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import it.units.malelab.ege.core.listener.event.EvolutionEndEvent;
import it.units.malelab.ege.core.listener.event.EvolutionStartEvent;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.listener.event.MappingEvent;
import it.units.malelab.ege.core.listener.event.OperatorApplicationEvent;
import it.units.malelab.ege.core.mapper.MappingException;
import it.units.malelab.ege.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author eric
 */
public class StandardEvolver<G, T, F extends Fitness> implements Evolver<G, T, F> {

  protected static final int CACHE_SIZE = 10000;
  public final static String MAPPING_CACHE_NAME = "mapping";
  public final static String FITNESS_CACHE_NAME = "fitness";

  private final StandardConfiguration<G, T, F> configuration;
  protected final boolean saveAncestry;

  public StandardEvolver(StandardConfiguration<G, T, F> configuration, boolean saveAncestry) {
    this.configuration = configuration;
    this.saveAncestry = saveAncestry;
  }

  @Override
  public Configuration<G, T, F> getConfiguration() {
    return configuration;
  }

  @Override
  public List<Node<T>> solve(ExecutorService executor, Random random, List<EvolverListener<G, T, F>> listeners) throws InterruptedException, ExecutionException {
    LoadingCache<G, Pair<Node<T>, Map<String, Object>>> mappingCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).recordStats().build(getMappingCacheLoader());
    LoadingCache<Node<T>, F> fitnessCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).recordStats().build(getFitnessCacheLoader());
    //initialize population
    int births = 0;
    List<Callable<List<Individual<G, T, F>>>> tasks = new ArrayList<>();
    for (G genotype : configuration.getPopulationInitializer().build(configuration.getPopulationSize(), configuration.getInitGenotypeValidator(), random)) {
      tasks.add(individualFromGenotypeCallable(genotype, 0, mappingCache, fitnessCache, listeners, null, null));
      births = births + 1;
    }
    List<Individual<G, T, F>> population = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
    int lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
    Utils.broadcast(new EvolutionStartEvent<>(this, cacheStats(mappingCache, fitnessCache)), listeners);
    Utils.broadcast(new GenerationEvent<>(configuration.getRanker().rank(population), lastBroadcastGeneration, this, cacheStats(mappingCache, fitnessCache)), listeners);
    //iterate
    while (Math.round(births / configuration.getPopulationSize()) < configuration.getNumberOfGenerations()) {
      int currentGeneration = (int) Math.floor(births / configuration.getPopulationSize());
      tasks.clear();
      //re-rank
      List<List<Individual<G, T, F>>> rankedPopulation = configuration.getRanker().rank(population);
      //produce offsprings
      int i = 0;
      while (i < configuration.getOffspringSize()) {
        GeneticOperator<G> operator = Utils.selectRandom(configuration.getOperators(), random);
        List<Individual<G, T, F>> parents = new ArrayList<>(operator.getParentsArity());
        for (int j = 0; j < operator.getParentsArity(); j++) {
          parents.add(configuration.getParentSelector().select(rankedPopulation, random));
        }
        tasks.add(operatorApplicationCallable(operator, parents, random, currentGeneration, mappingCache, fitnessCache, listeners));
        i = i + operator.getChildrenArity();
      }
      List<Individual<G, T, F>> newPopulation = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
      births = births + newPopulation.size();
      //build new population
      if (configuration.isOverlapping()) {
        population.addAll(newPopulation);
      } else {
        if (newPopulation.size() >= configuration.getPopulationSize()) {
          population = newPopulation;
        } else {
          //keep missing individuals from old population
          int targetSize = population.size() - newPopulation.size();
          while (population.size() > targetSize) {
            Individual<G, T, F> individual = configuration.getUnsurvivalSelector().select(rankedPopulation, random);
            population.remove(individual);
          }
          population.addAll(newPopulation);
        }
      }
      //select survivals
      while (population.size() > configuration.getPopulationSize()) {
        //re-rank
        rankedPopulation = configuration.getRanker().rank(population);
        Individual<G, T, F> individual = configuration.getUnsurvivalSelector().select(rankedPopulation, random);
        population.remove(individual);
      }
      if ((int) Math.floor(births / configuration.getPopulationSize()) > lastBroadcastGeneration) {
        lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
        Utils.broadcast(new GenerationEvent<>((List) rankedPopulation, lastBroadcastGeneration, this, cacheStats(mappingCache, fitnessCache)), listeners);
      }
    }
    //end
    executor.shutdown();
    List<Node<T>> bestPhenotypes = new ArrayList<>();
    List<List<Individual<G, T, F>>> rankedPopulation = configuration.getRanker().rank(population);
    Utils.broadcast(new EvolutionEndEvent<>((List) rankedPopulation, configuration.getNumberOfGenerations(), this, cacheStats(mappingCache, fitnessCache)), listeners);
    for (Individual<G, T, F> individual : rankedPopulation.get(0)) {
      bestPhenotypes.add(individual.getPhenotype());
    }
    return bestPhenotypes;
  }

  protected CacheLoader<G, Pair<Node<T>, Map<String, Object>>> getMappingCacheLoader() {
    return new CacheLoader<G, Pair<Node<T>, Map<String, Object>>>() {
      @Override
      public Pair<Node<T>, Map<String, Object>> load(G genotype) throws Exception {
        Node<T> phenotype = null;
        Map<String, Object> report = new LinkedHashMap<>();
        try {
          phenotype = configuration.getMapper().map(genotype, report);
        } catch (MappingException ex) {
          phenotype = Node.EMPTY_TREE;
        }
        return new Pair<>(phenotype, report);
      }
    };
  }

  protected CacheLoader<Node<T>, F> getFitnessCacheLoader() {
    return new CacheLoader<Node<T>, F>() {
      @Override
      public F load(Node<T> phenotype) throws Exception {
        if (Node.EMPTY_TREE.equals(phenotype)) {
          return configuration.getProblem().getLearningFitnessComputer().worstValue();
        }
        return configuration.getProblem().getLearningFitnessComputer().compute(phenotype);
      }
    };
  }

  protected Callable<List<Individual<G, T, F>>> individualFromGenotypeCallable(
          final G genotype,
          final int generation,
          final LoadingCache<G, Pair<Node<T>, Map<String, Object>>> mappingCache,
          final LoadingCache<Node<T>, F> fitnessCache,
          final List<EvolverListener<G, T, F>> listeners,
          final GeneticOperator<G> operator,
          final List<Individual<G, T, F>> parents) {
    final Evolver<G, T, F> evolver = this;
    return new Callable<List<Individual<G, T, F>>>() {
      @Override
      public List<Individual<G, T, F>> call() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Pair<Node<T>, Map<String, Object>> mappingOutcome = mappingCache.getUnchecked(genotype);
        Node<T> phenotype = mappingOutcome.getFirst();
        long elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
        Utils.broadcast(new MappingEvent<>(genotype, phenotype, elapsed, generation, evolver, null), listeners);
        stopwatch.reset().start();
        F fitness = fitnessCache.getUnchecked(phenotype);
        elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
        Individual<G, T, F> individual = new Individual<>(genotype, phenotype, fitness, generation, saveAncestry ? (List) parents : null, mappingOutcome.getSecond());
        Utils.broadcast(new BirthEvent<>(individual, elapsed, generation, evolver, null),  listeners);
        return Collections.singletonList(individual);
      }
    };
  }
  
  protected Map<String, Object> cacheStats(LoadingCache mappingCache, LoadingCache fitnessCache) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(MAPPING_CACHE_NAME, mappingCache.stats());
    map.put(FITNESS_CACHE_NAME, fitnessCache.stats());
    return map;
  }

  protected Callable<List<Individual<G, T, F>>> operatorApplicationCallable(
          final GeneticOperator<G> operator,
          final List<Individual<G, T, F>> parents,
          final Random random,
          final int generation,
          final LoadingCache<G, Pair<Node<T>, Map<String, Object>>> mappingCache,
          final LoadingCache<Node<T>, F> fitnessCache,
          final List<EvolverListener<G, T, F>> listeners
  ) {
    final Evolver<G, T, F> evolver = this;
    return new Callable<List<Individual<G, T, F>>>() {
      @Override
      public List<Individual<G, T, F>> call() throws Exception {
        List<Individual<G, T, F>> children = new ArrayList<>(operator.getChildrenArity());
        List<G> parentGenotypes = new ArrayList<>(operator.getParentsArity());
        for (Individual<G, T, F> parent : parents) {
          parentGenotypes.add(parent.getGenotype());
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<G> childGenotypes = operator.apply(parentGenotypes, random).subList(0, operator.getChildrenArity());
        long elapsed = stopwatch.elapsed(TimeUnit.NANOSECONDS);
        if (childGenotypes!=null) {
          for (G childGenotype : childGenotypes) {
            children.addAll(individualFromGenotypeCallable(childGenotype, generation, mappingCache, fitnessCache, listeners, operator, parents).call());
          }
        }
        Utils.broadcast(new OperatorApplicationEvent<>(parents, children, operator, elapsed, generation, evolver, null), listeners);
        return children;
      }
    };
  }
}
