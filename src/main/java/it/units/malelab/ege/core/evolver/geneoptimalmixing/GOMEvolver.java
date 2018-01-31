/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver.geneoptimalmixing;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import it.units.malelab.ege.core.ConstrainedSequence;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.evolver.StandardEvolver;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.event.BirthEvent;
import it.units.malelab.ege.core.listener.event.EvolutionEndEvent;
import it.units.malelab.ege.core.listener.event.EvolutionStartEvent;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import it.units.malelab.ege.core.listener.event.MappingEvent;
import it.units.malelab.ege.core.operator.AbstractMutation;
import it.units.malelab.ege.core.ranker.Ranker;
import it.units.malelab.ege.util.Pair;
import it.units.malelab.ege.util.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author eric
 */
public class GOMEvolver<G extends ConstrainedSequence, T, F extends Fitness> extends StandardEvolver<G, T, F> {

  private final GOMConfiguration<G, T, F> configuration;

  public GOMEvolver(GOMConfiguration<G, T, F> configuration, boolean saveAncestry) {
    super(configuration, saveAncestry);
    this.configuration = configuration;
  }

  @Override
  public List<Node<T>> solve(
          ExecutorService executor,
          Random random,
          List<EvolverListener<G, T, F>> listeners
  ) throws InterruptedException, ExecutionException {
    LoadingCache<G, Pair<Node<T>, Map<String, Object>>> mappingCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).recordStats().build(getMappingCacheLoader());
    LoadingCache<Node<T>, F> fitnessCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).recordStats().build(getFitnessCacheLoader());
    Stopwatch stopwatch = Stopwatch.createStarted();
    //initialize population
    int births = 0;
    List<Callable<List<Individual<G, T, F>>>> tasks = new ArrayList<>();
    for (G genotype : configuration.getPopulationInitializer().build(configuration.getPopulationSize(), configuration.getInitGenotypeValidator(), random)) {
      tasks.add(individualFromGenotypeCallable(genotype, 0, mappingCache, fitnessCache, listeners, null, null, executor));
      births = births + 1;
    }
    List<Individual<G, T, F>> population = new ArrayList<>(Utils.getAll(executor.invokeAll(tasks)));
    Utils.broadcast(new EvolutionStartEvent<>(this, cacheStats(mappingCache, fitnessCache)), listeners, executor);
    Utils.broadcast(new GenerationEvent<>(configuration.getRanker().rank(population, random), (int) Math.floor(actualBirths(births, fitnessCache) / configuration.getPopulationSize()), this, cacheStats(mappingCache, fitnessCache)), listeners, executor);
    Set<Individual<G, T, F>> bests = new LinkedHashSet<>();    
    //iterate
    while (Math.round(actualBirths(births, fitnessCache) / configuration.getPopulationSize()) < configuration.getNumberOfGenerations()) {
      //learn fos
      List<ConstrainedSequence> genotypes = new ArrayList<>(population.size());
      for (Individual<G, T, F> individual : population) {
        genotypes.add(individual.getGenotype());
      }
      Set<Set<Integer>> fos = configuration.getFosBuilder().build(genotypes, random);
      //apply gom
      int lastIterationActualBirths = actualBirths(births, fitnessCache);
      tasks.clear();
      for (Individual<G, T, F> individual : population) {
        tasks.add(gomCallable(
                population,
                individual,
                fos,
                random,
                (int) Math.floor(actualBirths(births, fitnessCache) / configuration.getPopulationSize()),
                configuration.getRanker(),
                configuration.getMutationOperator(),
                mappingCache,
                fitnessCache,
                population.size()*configuration.getNumberOfGenerations(),
                listeners,
                executor));
      }
      List<Individual<G, T, F>> newPopulation = new ArrayList<>();
      for (Future<List<Individual<G, T, F>>> result : executor.invokeAll(tasks)) {
        List<Individual<G, T, F>> newIndividuals = result.get();
        newPopulation.add(newIndividuals.get(0));
        births = births + fos.size();
        if (Math.round(actualBirths(births, fitnessCache) / configuration.getPopulationSize()) >= configuration.getNumberOfGenerations()) {
          break;
        }
      }
      for (int i = 0; i < newPopulation.size(); i++) {
        population.set(i, newPopulation.get(i));
      }
      //update best rank
      List<Individual<G, T, F>> populationWithBests = new ArrayList<>(population);
      populationWithBests.addAll(bests);
      List<List<Individual<G, T, F>>> rankedPopulationWithBests = configuration.getRanker().rank(populationWithBests, random);
      bests.clear();
      for (Individual<G, T, F> individual : rankedPopulationWithBests.get(0)) {
        bests.add(individual);
        if (bests.size()>=configuration.getPopulationSize()) {
          break;
        }
      }
      Utils.broadcast(new GenerationEvent<>(rankedPopulationWithBests, (int) Math.floor(actualBirths(births, fitnessCache) / configuration.getPopulationSize()), this, cacheStats(mappingCache, fitnessCache)), listeners, executor);
      if (configuration.getMaxRelativeElapsed()>0) {
        //check if relative elapsed time exceeded
        double avgFitnessComputationNanos = fitnessCache.stats().averageLoadPenalty();
        double elapsedNanos = stopwatch.elapsed(TimeUnit.NANOSECONDS);
        if (elapsedNanos/avgFitnessComputationNanos>configuration.getMaxRelativeElapsed()) {
          break;
        }
      }
      if (configuration.getMaxElapsed()>0) {
        //check if elapsed time exceeded
        if (stopwatch.elapsed(TimeUnit.SECONDS)>configuration.getMaxElapsed()) {
          break;
        }
      }
      if (configuration.getProblem().getLearningFitnessComputer().bestValue()!=null) {
        //check if optimal solution found
        if (rankedPopulationWithBests.get(0).get(0).getFitness().equals(configuration.getProblem().getLearningFitnessComputer().bestValue())) {
          break;
        }
      }
    }
    //end
    List<Node<T>> bestPhenotypes = new ArrayList<>();
    List<Individual<G, T, F>> populationWithBests = new ArrayList<>(population);
    populationWithBests.addAll(bests);
    List<List<Individual<G, T, F>>> rankedPopulationWithBests = configuration.getRanker().rank(populationWithBests, random);
    Utils.broadcast(new EvolutionEndEvent<>((List) rankedPopulationWithBests, (int) Math.floor(births / configuration.getPopulationSize()), this, cacheStats(mappingCache, fitnessCache)), listeners, executor);
    for (Individual<G, T, F> individual : rankedPopulationWithBests.get(0)) {
      bestPhenotypes.add(individual.getPhenotype());
    }
    return bestPhenotypes;
  }

  protected Callable<List<Individual<G, T, F>>> gomCallable(
          final List<Individual<G, T, F>> population,
          final Individual<G, T, F> parent,
          final Set<Set<Integer>> fos,
          final Random random,
          final int generation,
          final Ranker<Individual<G, T, F>> ranker,
          final AbstractMutation<G> mutationOperator,
          final LoadingCache<G, Pair<Node<T>, Map<String, Object>>> mappingCache,
          final LoadingCache<Node<T>, F> fitnessCache,
          final int maxEvaluations,
          final List<EvolverListener<G, T, F>> listeners,
          final ExecutorService executor
  ) {
    final Evolver<G, T, F> evolver = this;
    return new Callable<List<Individual<G, T, F>>>() {
      @Override
      public List<Individual<G, T, F>> call() throws Exception {
        try{
        //randomize fos
        List<Set<Integer>> randomizedFos = new ArrayList<>(fos);
        Collections.shuffle(randomizedFos, random);
        //iterate
        Individual<G, T, F> child = parent;
        for (Set<Integer> subset : fos) {
          //check evaluations
          if (actualBirths(0, fitnessCache) > maxEvaluations) {
            break;
          }
          //mix genes
          Individual<G, T, F> donor = population.get(random.nextInt(population.size()));
          G donorGenotype = donor.getGenotype();
          G childGenotype = (G) child.getGenotype().clone();
          for (Integer locus : subset) {
            childGenotype.set(locus, donorGenotype.get(locus));
          }
          //map
          Stopwatch stopwatch = Stopwatch.createStarted();
          Pair<Node<T>, Map<String, Object>> mappingOutcome = mappingCache.getUnchecked(childGenotype);
          Node<T> phenotype = mappingOutcome.getFirst();
          long elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
          Utils.broadcast(new MappingEvent<>(childGenotype, phenotype, elapsed, generation, evolver, null), listeners, executor);
          //compute fitness
          stopwatch.reset().start();
          F fitness = fitnessCache.getUnchecked(phenotype);
          elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
          Individual<G, T, F> individual = new Individual<>(childGenotype, phenotype, fitness, generation, saveAncestry ? Arrays.asList(child, donor) : null, mappingOutcome.getSecond());
          Utils.broadcast(new BirthEvent<>(individual, elapsed, generation, evolver, null), listeners, executor);
          //rank
          List<List<Individual<G, T, F>>> ranked = ranker.rank(Arrays.asList(child, individual), random);
          child = ranked.get(0).get(0);
        }
        if (mutationOperator != null) {
          while (child.getPhenotype().equals(parent.getPhenotype())) {
            //check evaluations
            if (actualBirths(0, fitnessCache) > maxEvaluations) {
              break;
            }
            //mutate
            G childGenotype = mutationOperator.apply(Collections.singletonList(child.getGenotype()), random).get(0);
            //map
            Stopwatch stopwatch = Stopwatch.createStarted();
            Pair<Node<T>, Map<String, Object>> mappingOutcome = mappingCache.getUnchecked(childGenotype);
            Node<T> phenotype = mappingOutcome.getFirst();
            long elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
            Utils.broadcast(new MappingEvent<>(childGenotype, phenotype, elapsed, generation, evolver, null), listeners, executor);
            //compute fitness
            stopwatch.reset().start();
            F fitness = fitnessCache.getUnchecked(phenotype);
            elapsed = stopwatch.stop().elapsed(TimeUnit.NANOSECONDS);
            Individual<G, T, F> individual = new Individual<>(childGenotype, phenotype, fitness, generation, saveAncestry ? Arrays.asList(child) : null, mappingOutcome.getSecond());
            Utils.broadcast(new BirthEvent<>(individual, elapsed, generation, evolver, null), listeners, executor);
            child = individual;
          }
        }
        return Collections.singletonList(child);
        } catch (Throwable t) {
          t.printStackTrace();
          System.exit(-1);
          return null;
        }        
      }
    };
  }

}
