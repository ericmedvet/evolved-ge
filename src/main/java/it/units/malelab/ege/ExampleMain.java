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
    //solveHarmonicCurve();
    //solveHarmonicCurveDiversity();
    //solveBinaryRegex();
    //solveBinaryRegexDiversity();
    //solveBinaryRegexSAC();
    //solveText();
    //solveTextCfgGp();
    //solveParityCfgGp();
    new KLandscapes(3);
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

  private static void solveText() throws IOException, InterruptedException, ExecutionException {
    Random random = new Random(1l);
    Problem<String, NumericFitness> problem = new Text();
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

  private static void solveTextCfgGp() throws IOException, InterruptedException, ExecutionException {
    Random random = new Random(1l);
    Problem<String, NumericFitness> problem = new Nguyen7(0);
    int maxDepth = 16;
    StandardConfiguration<Node<String>, String, NumericFitness> configuration = new StandardConfiguration<>(
            500,
            50,
            new MultiInitializer<>(new Utils.MapBuilder<PopulationInitializer<Node<String>>, Double>()
                    .put(new RandomInitializer<>(random, new GrowTreeFactory<>(maxDepth, problem.getGrammar())), 0.5)
                    .put(new RandomInitializer<>(random, new FullTreeFactory<>(maxDepth, problem.getGrammar())), 0.5)
                    .build()
            ),
            new Any<Node<String>>(),
            new CfgGpMapper<String>(),
            new Utils.MapBuilder<GeneticOperator<Node<String>>, Double>()
                    .put(new StandardTreeCrossover<String>(maxDepth, random), 0.8d)
                    .put(new StandardTreeMutation<>(maxDepth, problem.getGrammar(), random), 0.2d)
                    .build(),
            new ComparableRanker<>(new IndividualComparator<Node<String>, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
            new Tournament<Individual<Node<String>, String, NumericFitness>>(3, random),
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
    Evolver<Node<String>, String, NumericFitness> evolver = new StandardEvolver<>(
            1, configuration, random, false);
    List<Node<String>> bests = evolver.solve(listeners);
    System.out.printf("Found %d solutions.%n", bests.size());
  }

  private static void solveParityCfgGp() throws IOException, InterruptedException, ExecutionException {
    Random random = new Random(1l);
    Problem<String, NumericFitness> problem = new Parity(5);
    int maxDepth = 16;
    StandardConfiguration<Node<String>, String, NumericFitness> configuration = new StandardConfiguration<>(
            500,
            50,
            new MultiInitializer<>(new Utils.MapBuilder<PopulationInitializer<Node<String>>, Double>()
                    .put(new RandomInitializer<>(random, new GrowTreeFactory<>(maxDepth, problem.getGrammar())), 0.5)
                    .put(new RandomInitializer<>(random, new FullTreeFactory<>(maxDepth, problem.getGrammar())), 0.5)
                    .build()
            ),
            new Any<Node<String>>(),
            new CfgGpMapper<String>(),
            new Utils.MapBuilder<GeneticOperator<Node<String>>, Double>()
                    .put(new StandardTreeCrossover<String>(maxDepth, random), 0.8d)
                    .put(new StandardTreeMutation<>(maxDepth, problem.getGrammar(), random), 0.2d)
                    .build(),
            new ComparableRanker<>(new IndividualComparator<Node<String>, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
            new Tournament<Individual<Node<String>, String, NumericFitness>>(3, random),
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
    Evolver<Node<String>, String, NumericFitness> evolver = new StandardEvolver<>(
            1, configuration, random, false);
    List<Node<String>> bests = evolver.solve(listeners);
    System.out.printf("Found %d solutions.%n", bests.size());
  }

}
