/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.benchmark.BinaryRegex;
import it.units.malelab.ege.benchmark.KLandscapes;
import it.units.malelab.ege.benchmark.symbolicregression.HarmonicCurve;
import it.units.malelab.ege.benchmark.Text;
import it.units.malelab.ege.benchmark.booleanfunction.Parity;
import it.units.malelab.ege.benchmark.symbolicregression.Nguyen7;
import it.units.malelab.ege.cfggp.initializer.FullTreeFactory;
import it.units.malelab.ege.cfggp.initializer.GrowTreeFactory;
import it.units.malelab.ege.cfggp.mapper.CfgGpMapper;
import it.units.malelab.ege.cfggp.operator.StandardTreeCrossover;
import it.units.malelab.ege.cfggp.operator.StandardTreeMutation;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Sequence;
import it.units.malelab.ege.core.evolver.DeterministicCrowdingConfiguration;
import it.units.malelab.ege.core.evolver.DeterministicCrowdingEvolver;
import it.units.malelab.ege.core.evolver.PartitionConfiguration;
import it.units.malelab.ege.core.evolver.PartitionEvolver;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.collector.NumericFirstBest;
import it.units.malelab.ege.core.listener.collector.Population;
import it.units.malelab.ege.core.listener.collector.Diversity;
import it.units.malelab.ege.core.selector.LastWorst;
import it.units.malelab.ege.core.selector.Tournament;
import it.units.malelab.ege.core.selector.IndividualComparator;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.evolver.StandardEvolver;
import it.units.malelab.ege.core.evolver.sepandconq.Joiner;
import it.units.malelab.ege.core.evolver.sepandconq.SACConfiguration;
import it.units.malelab.ege.core.evolver.sepandconq.SACEvolver;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.core.initializer.MultiInitializer;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.core.initializer.RandomInitializer;
import it.units.malelab.ege.core.listener.EvolutionImageSaverListener;
import it.units.malelab.ege.core.listener.collector.BestPrinter;
import it.units.malelab.ege.core.listener.collector.MultiObjectiveFitnessFirstBest;
import it.units.malelab.ege.core.validator.Any;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.ranker.ComparableRanker;
import it.units.malelab.ege.core.ranker.ComparatorChain;
import it.units.malelab.ege.core.ranker.LexicoGraphicalMOComparator;
import it.units.malelab.ege.core.ranker.ParetoRanker;
import it.units.malelab.ege.core.selector.FirstBest;
import it.units.malelab.ege.ge.genotype.SGEGenotype;
import it.units.malelab.ege.ge.genotype.SGEGenotypeFactory;
import it.units.malelab.ege.ge.mapper.SGEMapper;
import it.units.malelab.ege.ge.mapper.WeightedHierarchicalMapper;
import it.units.malelab.ege.ge.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.ge.operator.ProbabilisticMutation;
import it.units.malelab.ege.ge.operator.SGECrossover;
import it.units.malelab.ege.ge.operator.SGEMutation;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.util.distance.Distance;
import it.units.malelab.ege.util.distance.Hamming;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author eric
 */
public class ExampleMain {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    solveKLandscapesCfgGp();
  }

  private static void solveKLandscapesCfgGp() throws IOException, InterruptedException, ExecutionException {
    Random random = new Random(1l);
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
    Problem<String, NumericFitness> problem = new KLandscapes(8);
    int maxDepth = 16;
    StandardConfiguration<Node<String>, String, NumericFitness> configuration = new StandardConfiguration<>(
            500,
            50,
            new MultiInitializer<>(new Utils.MapBuilder<PopulationInitializer<Node<String>>, Double>()
                    .put(new RandomInitializer<>(new GrowTreeFactory<>(maxDepth, problem.getGrammar())), 0.5)
                    .put(new RandomInitializer<>(new FullTreeFactory<>(maxDepth, problem.getGrammar())), 0.5)
                    .build()
            ),
            new Any<Node<String>>(),
            new CfgGpMapper<String>(),
            new Utils.MapBuilder<GeneticOperator<Node<String>>, Double>()
                    .put(new StandardTreeCrossover<String>(maxDepth), 0.8d)
                    .put(new StandardTreeMutation<>(maxDepth, problem.getGrammar()), 0.2d)
                    .build(),
            new ComparableRanker<>(new IndividualComparator<Node<String>, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
            new Tournament<Individual<Node<String>, String, NumericFitness>>(3),
            new LastWorst<Individual<Node<String>, String, NumericFitness>>(),
            500,
            true,
            problem);
    List<EvolverListener<Node<String>, String, NumericFitness>> listeners = new ArrayList<>();
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP, System.out, true, 10, " ", " | ",
            new Population<BitsGenotype, String, NumericFitness>(),
            new NumericFirstBest<BitsGenotype, String>(false, problem.getTestingFitnessComputer(), "%6.2f"),
            new Diversity<BitsGenotype, String, NumericFitness>(),
            new BestPrinter<BitsGenotype, String, NumericFitness>(problem.getPhenotypePrinter(), "%30.30s")
    ));
    Evolver<Node<String>, String, NumericFitness> evolver = new StandardEvolver<>(configuration, false);
    List<Node<String>> bests = evolver.solve(executor, random, listeners);
    System.out.printf("Found %d solutions.%n", bests.size());
  }

}
