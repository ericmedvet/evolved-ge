/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver;

import it.units.malelab.ege.evolver.fitness.Fitness;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.event.FitnessComputationEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.evolver.event.EvolutionEndEvent;
import it.units.malelab.ege.evolver.event.MappingEvent;
import it.units.malelab.ege.evolver.event.OperatorApplicationEvent;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.Node;
import it.units.malelab.ege.Utils;
import it.units.malelab.ege.mapper.MappingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author eric
 */
public class StandardEvolver<T> implements Evolver<T> {

  private static final int CACHE_SIZE = 10000;

  private final Configuration<T> configuration;

  public StandardEvolver(Configuration<T> configuration) {
    this.configuration = configuration;
  }

  @Override
  public Configuration<T> getConfiguration() {
    return configuration;
  }

  @Override
  public void go(List<EvolutionListener<T>> listeners) throws InterruptedException, ExecutionException {
    LoadingCache<Genotype, Node<T>> mappingCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(getMappingCacheLoader());
    LoadingCache<Node<T>, Fitness> fitnessCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(getFitnessCacheLoader());
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    //initialize population
    List<Callable<List<Individual<T>>>> tasks = new ArrayList<>();
    for (Genotype genotype : configuration.getPopulationInitializer().getGenotypes(configuration.getPopulationSize(), configuration.getInitGenotypeValidator())) {
      tasks.add(individualFromGenotypeCallable(genotype, 0, mappingCache, fitnessCache, listeners));
    }
    List<Individual<T>> population = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
    Utils.broadcast(new GenerationEvent<>(population, 0, this), listeners);
    //iterate over generation
    for (int g = 1; g < configuration.getNumberOfGenerations(); g++) {
      tasks.clear();
      for (Configuration.GeneticOperatorConfiguration operator : configuration.getOperators()) {
        int numberOfApplications = (int) Math.round((double) population.size() * operator.getRate() / (double) operator.getOperator().getChildrenArity());
        for (int i = 0; i < numberOfApplications; i++) {
          tasks.add(operatorApplicationCallable(population, operator, g, mappingCache, fitnessCache, listeners));
        }
      }
      List<Individual<T>> newPopulation = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
      population = newPopulation;
      Utils.broadcast(new GenerationEvent<>(population, g, this), listeners);
    }
    //end
    Utils.broadcast(new EvolutionEndEvent<>(population, configuration.getNumberOfGenerations(), this), listeners);
  }

  private CacheLoader<Genotype, Node<T>> getMappingCacheLoader() {
    return new CacheLoader<Genotype, Node<T>>() {
      @Override
      public Node<T> load(Genotype genotype) throws Exception {
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

  private Callable<List<Individual<T>>> individualFromGenotypeCallable(
          final Genotype genotype,
          final int generation,
          final LoadingCache<Genotype, Node<T>> mappingCache,
          final LoadingCache<Node<T>, Fitness> fitnessCache,
          final List<EvolutionListener<T>> listeners) {
    final Evolver<T> evolver = this;
    return new Callable<List<Individual<T>>>() {
      @Override
      public List<Individual<T>> call() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Node<T> phenotype = mappingCache.getUnchecked(genotype);
        long elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
        Utils.broadcast(new MappingEvent<>(genotype, phenotype, elapsed, generation, evolver), listeners);
        stopwatch.reset().start();
        Fitness fitness = fitnessCache.getUnchecked(phenotype);
        elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
        Individual<T> individual = new Individual<>(genotype, phenotype, fitness);
        Utils.broadcast(new FitnessComputationEvent<>(individual, elapsed, generation, evolver), listeners);
        return Collections.singletonList(individual);
      }
    };
  }

  private Callable<List<Individual<T>>> operatorApplicationCallable(
          final List<Individual<T>> population,
          final Configuration.GeneticOperatorConfiguration operator,
          final int generation,
          final LoadingCache<Genotype, Node<T>> mappingCache,
          final LoadingCache<Node<T>, Fitness> fitnessCache,
          final List<EvolutionListener<T>> listeners
  ) {
    final Evolver<T> evolver = this;
    return new Callable<List<Individual<T>>>() {
      @Override
      public List<Individual<T>> call() throws Exception {
        List<Individual<T>> parents = new ArrayList<>(operator.getOperator().getParentsArity());
        for (int p = 0; p < operator.getOperator().getParentsArity(); p++) {
          parents.add(operator.getSelector().select(population));
        }
        List<Individual<T>> children = new ArrayList<>(operator.getOperator().getChildrenArity());
        List<Genotype> parentGenotypes = new ArrayList<>(operator.getOperator().getParentsArity());
        for (Individual<T> parent : parents) {
          parentGenotypes.add(parent.getGenotype());
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Genotype> childGenotypes = operator.getOperator().apply(parentGenotypes).subList(0, operator.getOperator().getChildrenArity());
        long elapsed = stopwatch.elapsed(TimeUnit.NANOSECONDS);
        for (Genotype childGenotype : childGenotypes) {
          children.addAll(individualFromGenotypeCallable(childGenotype, generation, mappingCache, fitnessCache, listeners).call());
        }
        Utils.broadcast(new OperatorApplicationEvent<>(parents, children, operator.getOperator(), elapsed, generation, evolver), listeners);
        return children;
      }
    };
  }
;

}
