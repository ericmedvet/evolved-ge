/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver;

import it.units.malelab.ege.evolver.fitness.Fitness;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.event.BirthEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.evolver.event.EvolutionEndEvent;
import it.units.malelab.ege.evolver.event.MappingEvent;
import it.units.malelab.ege.evolver.event.OperatorApplicationEvent;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.Node;
import it.units.malelab.ege.Utils;
import it.units.malelab.ege.evolver.operator.GeneticOperator;
import it.units.malelab.ege.evolver.selector.Selector;
import it.units.malelab.ege.mapper.MappingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import sun.rmi.server.Util;

/**
 *
 * @author eric
 */
public class StandardEvolver<G extends Genotype, T> implements Evolver<G, T> {

  private static final int CACHE_SIZE = 10000;

  private final Configuration<G, T> configuration;
  private final ExecutorService executor;
  private final Random random;

  public StandardEvolver(int numberOfThreads, Configuration<G, T> configuration, Random random) {
    this.configuration = configuration;
    executor = Executors.newFixedThreadPool(numberOfThreads);
    this.random = random;
  }

  @Override
  public Configuration<G, T> getConfiguration() {
    return configuration;
  }

  @Override
  public void go(List<EvolutionListener<G, T>> listeners) throws InterruptedException, ExecutionException {
    LoadingCache<G, Node<T>> mappingCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(getMappingCacheLoader());
    LoadingCache<Node<T>, Fitness> fitnessCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(getFitnessCacheLoader());
    //initialize population
    int births = 0;
    List<Callable<List<Individual<G, T>>>> tasks = new ArrayList<>();
    for (G genotype : configuration.getPopulationInitializer().getGenotypes(configuration.getPopulationSize(), configuration.getInitGenotypeValidator())) {
      tasks.add(individualFromGenotypeCallable(genotype, 0, mappingCache, fitnessCache, listeners, null, null));
      births = births+1;
    }
    List<Individual<G, T>> population = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
    int lastBroadcastGeneration = (int)Math.floor(births/configuration.getPopulationSize());
    Utils.broadcast(new GenerationEvent<>(population, lastBroadcastGeneration, this), listeners);
    //iterate
    while (Math.round(births/configuration.getPopulationSize())<configuration.getNumberOfGenerations()) {
      int currentGeneration = (int)Math.floor(births/configuration.getPopulationSize());
      tasks.clear();
      //produce offsprings
      for (int i = 0; i<configuration.getOffspringSize(); i++) {
        GeneticOperator<G> operator = Utils.selectRandom(configuration.getOperators(), random);
        tasks.add(operatorApplicationCallable(population, operator, configuration.getParentSelector(), currentGeneration, mappingCache, fitnessCache, listeners));
      }
      List<Individual<G, T>> newPopulation = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
      births = births+newPopulation.size();
      //select survivals
      population.addAll(newPopulation);
      while (population.size()>configuration.getPopulationSize()) {
        Individual<G, T> individual = configuration.getSurvivalSelector().select((List)population, true);
        population.remove(individual);
      }
      if ((int)Math.floor(births/configuration.getPopulationSize())>lastBroadcastGeneration) {
        lastBroadcastGeneration = (int)Math.floor(births/configuration.getPopulationSize());
        Utils.sortByFitness(population);
        Utils.broadcast(new GenerationEvent<>(population, lastBroadcastGeneration, this), listeners);
      }
    }
    Utils.sortByFitness(population);
    //end
    Utils.broadcast(new EvolutionEndEvent<>(population, configuration.getNumberOfGenerations(), this), listeners);
    executor.shutdown();
  }
  
  private CacheLoader<G, Node<T>> getMappingCacheLoader() {
    return new CacheLoader<G, Node<T>>() {
      @Override
      public Node<T> load(G genotype) throws Exception {
        Node<T> phenotype = null;
        try {
          phenotype = configuration.getMapper().map(genotype);
        } catch (MappingException ex) {
          phenotype = Node.EMPTY_TREE;
        }
        return phenotype;
      }
    };
  }

  private CacheLoader<Node<T>, Fitness> getFitnessCacheLoader() {
    return new CacheLoader<Node<T>, Fitness>() {
      @Override
      public Fitness load(Node<T> phenotype) throws Exception {
        if (Node.EMPTY_TREE.equals(phenotype)) {
          return configuration.getFitnessComputer().worstValue();
        }
        return configuration.getFitnessComputer().compute(phenotype);
      }
    };
  }

  private Callable<List<Individual<G, T>>> individualFromGenotypeCallable(
          final G genotype,
          final int generation,
          final LoadingCache<G, Node<T>> mappingCache,
          final LoadingCache<Node<T>, Fitness> fitnessCache,
          final List<EvolutionListener<G, T>> listeners,
          final GeneticOperator<G> operator,
          final List<Individual<G, T>> parents) {
    final Evolver<G, T> evolver = this;
    return new Callable<List<Individual<G, T>>>() {
      @Override
      public List<Individual<G, T>> call() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Node<T> phenotype = mappingCache.getUnchecked(genotype);
        long elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
        Utils.broadcast(new MappingEvent<>(genotype, phenotype, elapsed, generation, evolver), listeners);
        stopwatch.reset().start();
        Fitness fitness = fitnessCache.getUnchecked(phenotype);
        elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
        Individual<G, T> individual = new Individual<>(genotype, phenotype, fitness, generation, operator, parents);
        Utils.broadcast(new BirthEvent<>(individual, elapsed, generation, evolver), listeners);
        return Collections.singletonList(individual);
      }
    };
  }

  private Callable<List<Individual<G, T>>> operatorApplicationCallable(
          final List<Individual<G, T>> population,
          final GeneticOperator<G> operator,
          final Selector selector,
          final int generation,
          final LoadingCache<G, Node<T>> mappingCache,
          final LoadingCache<Node<T>, Fitness> fitnessCache,
          final List<EvolutionListener<G, T>> listeners
  ) {
    final Evolver<G, T> evolver = this;
    return new Callable<List<Individual<G, T>>>() {
      @Override
      public List<Individual<G, T>> call() throws Exception {
        List<Individual<G, T>> parents = new ArrayList<>(operator.getParentsArity());
        for (int p = 0; p < operator.getParentsArity(); p++) {
          parents.add(selector.select((List)population, false));
        }
        List<Individual<G, T>> children = new ArrayList<>(operator.getChildrenArity());
        List<G> parentGenotypes = new ArrayList<>(operator.getParentsArity());
        for (Individual<G, T> parent : parents) {
          parentGenotypes.add(parent.getGenotype());
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<G> childGenotypes = operator.apply(parentGenotypes).subList(0, operator.getChildrenArity());
        long elapsed = stopwatch.elapsed(TimeUnit.NANOSECONDS);
        for (G childGenotype : childGenotypes) {
          children.addAll(individualFromGenotypeCallable(childGenotype, generation, mappingCache, fitnessCache, listeners, operator, parents).call());
        }
        Utils.broadcast(new OperatorApplicationEvent<>(parents, children, operator, elapsed, generation, evolver), listeners);
        return children;
      }
    };
  }

}
