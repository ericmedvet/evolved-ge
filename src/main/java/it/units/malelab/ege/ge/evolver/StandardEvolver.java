/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.evolver;

import it.units.malelab.ege.core.fitness.Fitness;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.units.malelab.ege.core.Configuration;
import it.units.malelab.ege.core.grammar.Node;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.event.BirthEvent;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import it.units.malelab.ege.core.listener.event.EvolutionEndEvent;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.ge.genotype.Genotype;
import it.units.malelab.ege.ge.operator.GeneticOperator;
import it.units.malelab.ege.ge.GEConfiguration;
import it.units.malelab.ege.ge.GEEvolver;
import it.units.malelab.ege.ge.GEIndividual;
import it.units.malelab.ege.ge.listener.event.MappingEvent;
import it.units.malelab.ege.ge.listener.event.OperatorApplicationEvent;
import it.units.malelab.ege.ge.mapper.MappingException;
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
public class StandardEvolver<G extends Genotype, T, F extends Fitness> implements GEEvolver<G, T, F> {

  protected static final int CACHE_SIZE = 10000;

  private final StandardConfiguration<G, T, F> configuration;
  protected final ExecutorService executor;
  protected final Random random;
  private final boolean saveAncestry;

  public StandardEvolver(int numberOfThreads, StandardConfiguration<G, T, F> configuration, Random random, boolean saveAncestry) {
    this.configuration = configuration;
    executor = Executors.newFixedThreadPool(numberOfThreads);
    this.random = random;
    this.saveAncestry = saveAncestry;
  }


  @Override
  public GEConfiguration<G, T, F> getGEConfiguration() {
    return configuration;
  }

  @Override
  public Configuration<T, F> getConfiguration() {
    return configuration;
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
    List<GEIndividual<G, T, F>> population = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
    int lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
    Utils.broadcast(new GenerationEvent<>((List)population, lastBroadcastGeneration, this, null), (List)listeners);
    //iterate
    while (Math.round(births / configuration.getPopulationSize()) < configuration.getNumberOfGenerations()) {
      int currentGeneration = (int) Math.floor(births / configuration.getPopulationSize());
      tasks.clear();
      //produce offsprings
      int i = 0;
      while (i < configuration.getOffspringSize()) {
        GeneticOperator<G> operator = Utils.selectRandom(configuration.getOperators(), random);
        List<GEIndividual<G, T, F>> parents = new ArrayList<>(operator.getParentsArity());
        for (int j = 0; j < operator.getParentsArity(); j++) {
          parents.add(configuration.getParentSelector().select(population));
        }
        tasks.add(operatorApplicationCallable(operator, parents, currentGeneration, mappingCache, fitnessCache, listeners));
        i = i + operator.getChildrenArity();
      }
      List<GEIndividual<G, T, F>> newPopulation = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
      births = births + newPopulation.size();
      //build new population
      if (configuration.isOverlapping()) {
        population.addAll(newPopulation);
      } else {
        if (newPopulation.size() >= configuration.getPopulationSize()) {
          population = newPopulation;
        } else {
          int targetSize = population.size() - newPopulation.size();
          while (population.size() > targetSize) {
            GEIndividual<G, T, F> individual = configuration.getUnsurvivalSelector().select(population);
            population.remove(individual);
          }
          population.addAll(newPopulation);
        }
      }
      //re-rank
      configuration.getProblem().getIndividualRanker().rank((List)population);
      //select survivals
      while (population.size() > configuration.getPopulationSize()) {
        GEIndividual<G, T, F> individual = configuration.getUnsurvivalSelector().select(population);
        population.remove(individual);
      }
      if ((int) Math.floor(births / configuration.getPopulationSize()) > lastBroadcastGeneration) {
        lastBroadcastGeneration = (int) Math.floor(births / configuration.getPopulationSize());
        Utils.broadcast(new GenerationEvent<>((List)population, lastBroadcastGeneration, this, null), (List)listeners);
      }
    }
    //end
    Utils.broadcast(new EvolutionEndEvent<>((List)population, configuration.getNumberOfGenerations(), this, null), (List)listeners);
    executor.shutdown();
    
    return null;
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

  protected Callable<List<GEIndividual<G, T, F>>> individualFromGenotypeCallable(
          final G genotype,
          final int generation,
          final LoadingCache<G, Pair<Node<T>, Map<String, Object>>> mappingCache,
          final LoadingCache<Node<T>, F> fitnessCache,
          final List<EvolverListener<T, F>> listeners,
          final GeneticOperator<G> operator,
          final List<GEIndividual<G, T, F>> parents) {
    final GEEvolver<G, T, F> evolver = this;
    return new Callable<List<GEIndividual<G, T, F>>>() {
      @Override
      public List<GEIndividual<G, T, F>> call() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Pair<Node<T>, Map<String, Object>> mappingOutcome = mappingCache.getUnchecked(genotype);
        Node<T> phenotype = mappingOutcome.getFirst();
        long elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
        Utils.broadcast(new MappingEvent<>(genotype, phenotype, elapsed, generation, evolver, null), (List)listeners);
        stopwatch.reset().start();
        F fitness = fitnessCache.getUnchecked(phenotype);
        elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
        GEIndividual<G, T, F> individual = new GEIndividual<>(genotype, phenotype, fitness, generation, saveAncestry ? (List)parents : null, mappingOutcome.getSecond());
        Utils.broadcast(new BirthEvent<>(individual, elapsed, generation, evolver, null), (List)listeners);
        return Collections.singletonList(individual);
      }
    };
  }

  protected Callable<List<GEIndividual<G, T, F>>> operatorApplicationCallable(
          final GeneticOperator<G> operator,
          final List<GEIndividual<G, T, F>> parents,
          final int generation,
          final LoadingCache<G, Pair<Node<T>, Map<String, Object>>> mappingCache,
          final LoadingCache<Node<T>, F> fitnessCache,
          final List<EvolverListener<T, F>> listeners
  ) {
    final GEEvolver<G, T, F> evolver = this;
    return new Callable<List<GEIndividual<G, T, F>>>() {
      @Override
      public List<GEIndividual<G, T, F>> call() throws Exception {
        List<GEIndividual<G, T, F>> children = new ArrayList<>(operator.getChildrenArity());
        List<G> parentGenotypes = new ArrayList<>(operator.getParentsArity());
        for (GEIndividual<G, T, F> parent : parents) {
          parentGenotypes.add(parent.getGenotype());
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<G> childGenotypes = operator.apply(parentGenotypes).subList(0, operator.getChildrenArity());
        long elapsed = stopwatch.elapsed(TimeUnit.NANOSECONDS);
        for (G childGenotype : childGenotypes) {
          children.addAll(individualFromGenotypeCallable(childGenotype, generation, mappingCache, fitnessCache, listeners, operator, parents).call());
        }
        Utils.broadcast(new OperatorApplicationEvent<>(parents, children, operator, elapsed, generation, evolver, null), (List)listeners);
        return children;
      }
    };
  }
}
