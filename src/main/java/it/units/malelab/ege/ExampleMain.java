/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.benchmark.BinaryRegex;
import it.units.malelab.ege.benchmark.HarmonicCurve;
import it.units.malelab.ege.cfggp.initializer.GrowTreeFactory;
import it.units.malelab.ege.cfggp.operator.StandardCrossover;
import it.units.malelab.ege.core.Grammar;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.Node;
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
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.core.initializer.RandomInitializer;
import it.units.malelab.ege.core.listener.collector.BestPrinter;
import it.units.malelab.ege.core.listener.collector.MultiObjectiveFitnessFirstBest;
import it.units.malelab.ege.ge.genotype.validator.Any;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.ranker.ComparableRanker;
import it.units.malelab.ege.core.ranker.ComparatorChain;
import it.units.malelab.ege.core.ranker.LexicoGraphicalMOComparator;
import it.units.malelab.ege.core.ranker.ParetoRanker;
import it.units.malelab.ege.core.selector.FirstBest;
import it.units.malelab.ege.ge.mapper.WeightedHierarchicalMapper;
import it.units.malelab.ege.ge.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.ge.operator.ProbabilisticMutation;
import it.units.malelab.ege.util.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class ExampleMain {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    Random random = new Random(1);
    Grammar<String> g = Utils.parseFromFile(new File("grammars/max-grammar.bnf"));
    int maxDepth = 5;
    GrowTreeFactory<String> f = new GrowTreeFactory<>(maxDepth, g);
    for (int i = 0; i < 100; i++) {
      Node<String> tree = f.build(random);

      System.out.printf("%d -> %s%n", tree.depth(), tree);

    }

    //solveHarmonicCurve();
    //solveHarmonicCurveDiversity();
    //solveBinaryRegex();
    //solveBinaryRegexDiversity();
    //solveBinaryRegexSAC();
  }

  private static void solveHarmonicCurve() throws IOException, InterruptedException, ExecutionException {
    Random random = new Random(1l);
    Problem<String, NumericFitness> problem = new HarmonicCurve();
    StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
            500,
            50,
            new RandomInitializer<>(random, new BitsGenotypeFactory(256)),
            new Any<BitsGenotype>(),
            //new StandardGEMapper<>(8, 5, problem.getGrammar()),
            new WeightedHierarchicalMapper<>(3, problem.getGrammar()),
            new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
            .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
            .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
            new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3, random),
            new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(),
            500,
            true,
            problem);
    List<EvolverListener<BitsGenotype, String, NumericFitness>> listeners = new ArrayList<>();
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP, System.out, true, 10, " ", " | ",
            new Population<BitsGenotype, String, NumericFitness>(),
            new NumericFirstBest<BitsGenotype, String>(false, problem.getTestingFitnessComputer(), "%6.2f"),
            new Diversity<BitsGenotype, String, NumericFitness>(),
            new BestPrinter<BitsGenotype, String, NumericFitness>(problem.getPhenotypePrinter(), "%30.30s")
    ));
    Evolver<BitsGenotype, String, NumericFitness> evolver = new StandardEvolver<>(
            1, configuration, random, false);
    List<Node<String>> bests = evolver.solve(listeners);
    System.out.printf("Found %d solutions.%n", bests.size());
  }

  private static void solveHarmonicCurveDiversity() throws IOException, InterruptedException, ExecutionException {
    Random random = new Random(1l);
    Problem<String, NumericFitness> problem = new HarmonicCurve();
    PartitionConfiguration<BitsGenotype, String, NumericFitness> configuration = new PartitionConfiguration<>(
            new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.PHENO),
            10,
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.AGE)),
            new FirstBest<Individual<BitsGenotype, String, NumericFitness>>(),
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.AGE)),
            new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(),
            500,
            50,
            new RandomInitializer<>(random, new BitsGenotypeFactory(256)),
            new Any<BitsGenotype>(),
            //new StandardGEMapper<>(8, 5, problem.getGrammar()),
            new WeightedHierarchicalMapper<>(3, problem.getGrammar()),
            new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
            .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
            .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
            new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3, random),
            new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(),
            500,
            true,
            problem
    );
    List<EvolverListener<BitsGenotype, String, NumericFitness>> listeners = new ArrayList<>();
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP, System.out, true, 10, " ", " | ",
            new Population<BitsGenotype, String, NumericFitness>(),
            new NumericFirstBest<BitsGenotype, String>(false, problem.getTestingFitnessComputer(), "%6.2f"),
            new Diversity<BitsGenotype, String, NumericFitness>(),
            new BestPrinter<BitsGenotype, String, NumericFitness>(problem.getPhenotypePrinter(), "%30.30s")
    ));
    Evolver<BitsGenotype, String, NumericFitness> evolver = new PartitionEvolver<>(
            1, configuration, random, false);
    List<Node<String>> bests = evolver.solve(listeners);
    System.out.printf("Found %d solutions.%n", bests.size());
  }

  private static void solveBinaryRegex() throws IOException, InterruptedException, ExecutionException {
    Random random = new Random(1l);
    Problem<String, MultiObjectiveFitness> problem = new BinaryRegex();
    StandardConfiguration<BitsGenotype, String, MultiObjectiveFitness> configuration = new StandardConfiguration<>(
            500,
            50,
            new RandomInitializer<>(random, new BitsGenotypeFactory(256)),
            new Any<BitsGenotype>(),
            new WeightedHierarchicalMapper<>(3, problem.getGrammar()),
            new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
            .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
            .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
            new ParetoRanker<BitsGenotype, String, MultiObjectiveFitness>(),
            new Tournament<Individual<BitsGenotype, String, MultiObjectiveFitness>>(3, random),
            new LastWorst<Individual<BitsGenotype, String, MultiObjectiveFitness>>(),
            500,
            true,
            problem);
    List<EvolverListener<BitsGenotype, String, MultiObjectiveFitness>> listeners = new ArrayList<>();
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP, System.out, true, 10, " ", " | ",
            new Population<BitsGenotype, String, MultiObjectiveFitness>(),
            new MultiObjectiveFitnessFirstBest<BitsGenotype, String>(false, problem.getTestingFitnessComputer(), "%4.2f", "%4.2f"),
            new Diversity<BitsGenotype, String, MultiObjectiveFitness>(),
            new BestPrinter<BitsGenotype, String, MultiObjectiveFitness>(problem.getPhenotypePrinter(), "%30.30s")
    ));
    Evolver<BitsGenotype, String, MultiObjectiveFitness> evolver = new StandardEvolver<>(
            1, configuration, random, false);
    List<Node<String>> bests = evolver.solve(listeners);
    System.out.printf("Found %d solutions.%n", bests.size());
  }

  private static void solveBinaryRegexDiversity() throws IOException, InterruptedException, ExecutionException {
    Random random = new Random(1l);
    Problem<String, MultiObjectiveFitness> problem = new BinaryRegex();
    PartitionConfiguration<BitsGenotype, String, MultiObjectiveFitness> configuration = new PartitionConfiguration<>(
            new IndividualComparator<BitsGenotype, String, MultiObjectiveFitness>(IndividualComparator.Attribute.PHENO),
            10,
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, MultiObjectiveFitness>(IndividualComparator.Attribute.AGE)),
            new FirstBest<Individual<BitsGenotype, String, MultiObjectiveFitness>>(),
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, MultiObjectiveFitness>(IndividualComparator.Attribute.AGE)),
            new LastWorst<Individual<BitsGenotype, String, MultiObjectiveFitness>>(),
            500,
            50,
            new RandomInitializer<>(random, new BitsGenotypeFactory(256)),
            new Any<BitsGenotype>(),
            new WeightedHierarchicalMapper<>(3, problem.getGrammar()),
            new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
            .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
            .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
            new ParetoRanker<BitsGenotype, String, MultiObjectiveFitness>(),
            new Tournament<Individual<BitsGenotype, String, MultiObjectiveFitness>>(3, random),
            new LastWorst<Individual<BitsGenotype, String, MultiObjectiveFitness>>(),
            500,
            true,
            problem);
    List<EvolverListener<BitsGenotype, String, MultiObjectiveFitness>> listeners = new ArrayList<>();
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP, System.out, true, 10, " ", " | ",
            new Population<BitsGenotype, String, MultiObjectiveFitness>(),
            new MultiObjectiveFitnessFirstBest<BitsGenotype, String>(false, problem.getTestingFitnessComputer(), "%4.2f", "%4.2f"),
            new Diversity<BitsGenotype, String, MultiObjectiveFitness>(),
            new BestPrinter<BitsGenotype, String, MultiObjectiveFitness>(problem.getPhenotypePrinter(), "%30.30s")
    ));
    Evolver<BitsGenotype, String, MultiObjectiveFitness> evolver = new PartitionEvolver<>(
            1, configuration, random, false);
    List<Node<String>> bests = evolver.solve(listeners);
    System.out.printf("Found %d solutions.%n", bests.size());
  }

  private static void solveBinaryRegexSAC() throws IOException, InterruptedException, ExecutionException {
    Random random = new Random(1l);
    Problem<String, MultiObjectiveFitness> problem = new BinaryRegex();
    final LexicoGraphicalMOComparator lgmoc = new LexicoGraphicalMOComparator(0, 1);
    SACConfiguration<String, BitsGenotype, String, MultiObjectiveFitness> configuration = new SACConfiguration<>(
            new Joiner<String>() {
              @Override
              public Node<String> join(Node<String>... pieces) {
                Node<String> orNode = new Node<>("<or>");
                for (int i = 0; i < pieces.length; i++) {
                  if (i > 0) {
                    orNode.getChildren().add(new Node<>("|"));
                  }
                  orNode.getChildren().add(new Node<>("("));
                  orNode.getChildren().add(pieces[i]);
                  orNode.getChildren().add(new Node<>(")"));
                }
                return orNode;
              }
            },
            new IndividualComparator<BitsGenotype, String, MultiObjectiveFitness>(IndividualComparator.Attribute.PHENO),
            1,
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, MultiObjectiveFitness>(IndividualComparator.Attribute.AGE)),
            new FirstBest<Individual<BitsGenotype, String, MultiObjectiveFitness>>(),
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, MultiObjectiveFitness>(IndividualComparator.Attribute.AGE)),
            new LastWorst<Individual<BitsGenotype, String, MultiObjectiveFitness>>(),
            500,
            25,
            new RandomInitializer<>(random, new BitsGenotypeFactory(256)),
            new Any<BitsGenotype>(),
            new WeightedHierarchicalMapper<>(3, problem.getGrammar()),
            new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
            .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
            .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
            new ComparableRanker<>(new ComparatorChain<Individual<BitsGenotype, String, MultiObjectiveFitness>>(
                            new Comparator<Individual<BitsGenotype, String, MultiObjectiveFitness>>() {
                              @Override
                              public int compare(Individual<BitsGenotype, String, MultiObjectiveFitness> i1, Individual<BitsGenotype, String, MultiObjectiveFitness> i2) {
                                return lgmoc.compare(i1.getFitness(), i2.getFitness());
                              }
                            },
                            new IndividualComparator<BitsGenotype, String, MultiObjectiveFitness>(IndividualComparator.Attribute.PHENO_SIZE)
                    )),
            new Tournament<Individual<BitsGenotype, String, MultiObjectiveFitness>>(3, random),
            new LastWorst<Individual<BitsGenotype, String, MultiObjectiveFitness>>(),
            500,
            true,
            problem);
    List<EvolverListener<BitsGenotype, String, MultiObjectiveFitness>> listeners = new ArrayList<>();
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP, System.out, true, 10, " ", " | ",
            new Population<BitsGenotype, String, MultiObjectiveFitness>(),
            new MultiObjectiveFitnessFirstBest<BitsGenotype, String>(false, problem.getTestingFitnessComputer(), "%4.2f", "%4.2f"),
            new Diversity<BitsGenotype, String, MultiObjectiveFitness>(),
            new BestPrinter<BitsGenotype, String, MultiObjectiveFitness>(problem.getPhenotypePrinter(), "%30.30s")
    ));
    Evolver<BitsGenotype, String, MultiObjectiveFitness> evolver = new SACEvolver<>(
            1, configuration, random, false);
    List<Node<String>> bests = evolver.solve(listeners);
    System.out.printf("Found %d solutions.%n", bests.size());
  }

}
