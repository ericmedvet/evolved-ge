/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import com.google.common.collect.Lists;
import it.units.malelab.ege.benchmark.KLandscapes;
import it.units.malelab.ege.benchmark.Text;
import it.units.malelab.ege.benchmark.booleanfunction.MultipleOutputParallelMultiplier;
import it.units.malelab.ege.benchmark.booleanfunction.Parity;
import it.units.malelab.ege.benchmark.symbolicregression.HarmonicCurve;
import it.units.malelab.ege.benchmark.symbolicregression.Nguyen7;
import it.units.malelab.ege.benchmark.symbolicregression.Pagie1;
import it.units.malelab.ege.benchmark.symbolicregression.Vladislavleva4;
import it.units.malelab.ege.cfggp.initializer.FullTreeFactory;
import it.units.malelab.ege.cfggp.initializer.GrowTreeFactory;
import it.units.malelab.ege.cfggp.mapper.CfgGpMapper;
import it.units.malelab.ege.cfggp.operator.StandardTreeCrossover;
import it.units.malelab.ege.cfggp.operator.StandardTreeMutation;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.Sequence;
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.evolver.StandardEvolver;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.initializer.MultiInitializer;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.initializer.RandomInitializer;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.PropertiesListener;
import it.units.malelab.ege.core.listener.collector.BestPrinter;
import it.units.malelab.ege.core.listener.collector.CacheStatistics;
import it.units.malelab.ege.core.listener.collector.Diversity;
import it.units.malelab.ege.core.listener.collector.NumericFirstBest;
import it.units.malelab.ege.core.listener.collector.Population;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.operator.AbstractCrossover;
import it.units.malelab.ege.core.operator.AbstractMutation;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.ranker.ComparableRanker;
import it.units.malelab.ege.core.selector.IndividualComparator;
import it.units.malelab.ege.core.selector.LastWorst;
import it.units.malelab.ege.core.selector.Tournament;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.ge.genotype.SGEGenotype;
import it.units.malelab.ege.ge.genotype.SGEGenotypeFactory;
import it.units.malelab.ege.ge.genotype.validator.Any;
import it.units.malelab.ege.ge.mapper.HierarchicalMapper;
import it.units.malelab.ege.ge.mapper.PiGEMapper;
import it.units.malelab.ege.ge.mapper.SGEMapper;
import it.units.malelab.ege.ge.mapper.StandardGEMapper;
import it.units.malelab.ege.ge.mapper.WeightedHierarchicalMapper;
import it.units.malelab.ege.ge.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.ge.operator.ProbabilisticMutation;
import it.units.malelab.ege.ge.operator.SGECrossover;
import it.units.malelab.ege.ge.operator.SGEMutation;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.util.distance.CachedDistance;
import it.units.malelab.ege.util.distance.Distance;
import it.units.malelab.ege.util.distance.Edit;
import it.units.malelab.ege.util.distance.Hamming;
import it.units.malelab.ege.util.distance.LeavesEdit;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class Experimenter {

  private final static int N_THREADS = Runtime.getRuntime().availableProcessors() - 1;

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    Random random = new Random(1);
    final int[] genotypeSizes = new int[]{1024};
    final int populationSize = 500;
    final int generations = 50;
    final int runs = 10;
    //prepare problems and methods
    List<String> problems = Lists.newArrayList(
            "bool-parity5", "bool-mopm3",
            "sr-keijzer6", "sr-nguyen7", "sr-pagie1", "sr-vladislavleva4",
            "other-klandscapes3", "other-klandscapes7", "other-text"
    );
    List<String> methods = Lists.newArrayList("whge-3", "whge-2");
    //methods = Lists.newArrayList("ge-8", "pige-16", "hge", "whge-3");
    PrintStream filePrintStream = null;
    if (args.length > 0) {
      filePrintStream = new PrintStream(args[0]);
    }
    //prepare distances
    Distance<Node<String>> phenotypeDistance = new CachedDistance<>(new LeavesEdit<String>());
    Distance<Sequence<Boolean>> bitsDistance = new CachedDistance<>(new Edit<Boolean>());
    Distance<Sequence<Integer>> sgeDistance = new CachedDistance<>(new Hamming<Integer>());
    //iterate
    boolean header = true;
    for (int run = 0; run < runs; run++) {
      for (String problemName : problems) {
        for (String methodName : methods) {
          for (int genotypeSize : genotypeSizes) {
            Map<String, Object> constants = new LinkedHashMap<>();
            constants.put("problem", problemName);
            constants.put("method", methodName);
            constants.put("run", run);
            constants.put("genotype.size", genotypeSize);
            System.out.printf("%nProblem: %s\tMethod: %s\tRun: %d%n", problemName, methodName, run);
            if (problemName.equals("other-binaryregex")) {
              //manage differently, since it is multi-objective
            } else {
              //build problem
              Problem<String, NumericFitness> problem = null;
              if (problemName.equals("bool-parity5")) {
                problem = new Parity(5);
              } else if (problemName.equals("bool-mopm3")) {
                problem = new MultipleOutputParallelMultiplier(3);
              } else if (problemName.equals("sr-keijzer6")) {
                problem = new HarmonicCurve();
              } else if (problemName.equals("sr-nguyen7")) {
                problem = new Nguyen7(1);
              } else if (problemName.equals("sr-pagie1")) {
                problem = new Pagie1();
              } else if (problemName.equals("sr-vladislavleva4")) {
                problem = new Vladislavleva4(1);
              } else if (problemName.equals("other-klandscapes3")) {
                problem = new KLandscapes(3);
              } else if (problemName.equals("other-klandscapes7")) {
                problem = new KLandscapes(7);
              } else if (problemName.equals("other-text")) {
                problem = new Text();
              }
              //build configuration and evolver
              Evolver evolver = null;
              PropertiesListener propertiesListener = null;
              if (methodName.startsWith("ge-")) {
                int codonSize = Integer.parseInt(methodName.replace("ge-", ""));
                StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
                        populationSize, generations,
                        new RandomInitializer<>(random, new BitsGenotypeFactory(genotypeSize)),
                        new Any<BitsGenotype>(),
                        new StandardGEMapper<>(codonSize, 1, problem.getGrammar()),
                        new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
                                .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
                                .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
                        new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
                        new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3, random),
                        new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(), populationSize,
                        true,
                        problem);
                evolver = new StandardEvolver<>(configuration, N_THREADS, random, false);
                propertiesListener = new PropertiesListener(
                        NumericFitness.comparator(),
                        bitsDistance,
                        phenotypeDistance,
                        new Utils.MapBuilder<Class<? extends GeneticOperator>, String>()
                                .put(AbstractCrossover.class, "crossover")
                                .put(AbstractMutation.class, "mutation").build()
                );
              } else if (methodName.startsWith("pige-")) {
                int codonSize = Integer.parseInt(methodName.replace("pige-", ""));
                StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
                        populationSize, generations,
                        new RandomInitializer<>(random, new BitsGenotypeFactory(genotypeSize)),
                        new Any<BitsGenotype>(),
                        new PiGEMapper<>(codonSize, 1, problem.getGrammar()),
                        new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
                                .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
                                .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
                        new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
                        new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3, random),
                        new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(), populationSize,
                        true,
                        problem);
                evolver = new StandardEvolver<>(configuration, N_THREADS, random, false);
                propertiesListener = new PropertiesListener(
                        NumericFitness.comparator(),
                        bitsDistance,
                        phenotypeDistance,
                        new Utils.MapBuilder<Class<? extends GeneticOperator>, String>()
                                .put(AbstractCrossover.class, "crossover")
                                .put(AbstractMutation.class, "mutation").build()
                );
              } else if (methodName.startsWith("sge-")) {
                int depth = Integer.parseInt(methodName.replace("sge-", ""));
                Mapper<SGEGenotype<String>, String> mapper = new SGEMapper<>(depth, problem.getGrammar());
                StandardConfiguration<SGEGenotype<String>, String, NumericFitness> configuration = new StandardConfiguration<>(
                        populationSize, generations,
                        new RandomInitializer<>(random, new SGEGenotypeFactory<>((SGEMapper<String>) mapper)),
                        new Any<SGEGenotype<String>>(),
                        mapper,
                        new Utils.MapBuilder<GeneticOperator<SGEGenotype<String>>, Double>()
                                .put(new SGECrossover<String>(random), 0.8d)
                                .put(new SGEMutation<>(0.01, (SGEMapper<String>) mapper, random), 0.2d).build(),
                        new ComparableRanker<>(new IndividualComparator<SGEGenotype<String>, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
                        new Tournament<Individual<SGEGenotype<String>, String, NumericFitness>>(3, random),
                        new LastWorst<Individual<SGEGenotype<String>, String, NumericFitness>>(), populationSize,
                        true,
                        problem);
                evolver = new StandardEvolver<>(configuration, N_THREADS, random, false);
                propertiesListener = new PropertiesListener(
                        NumericFitness.comparator(),
                        sgeDistance,
                        phenotypeDistance,
                        new Utils.MapBuilder<Class<? extends GeneticOperator>, String>()
                                .put(AbstractCrossover.class, "crossover")
                                .put(AbstractMutation.class, "mutation").build()
                );
              } else if (methodName.equals("hge")) {
                StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
                        populationSize, generations,
                        new RandomInitializer<>(random, new BitsGenotypeFactory(genotypeSize)),
                        new Any<BitsGenotype>(),
                        new HierarchicalMapper<>(problem.getGrammar()),
                        new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
                                .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
                                .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
                        new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
                        new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3, random),
                        new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(), populationSize,
                        true,
                        problem);
                evolver = new StandardEvolver<>(configuration, N_THREADS, random, false);
                propertiesListener = new PropertiesListener(
                        NumericFitness.comparator(),
                        bitsDistance,
                        phenotypeDistance,
                        new Utils.MapBuilder<Class<? extends GeneticOperator>, String>()
                                .put(AbstractCrossover.class, "crossover")
                                .put(AbstractMutation.class, "mutation").build()
                );
              } else if (methodName.startsWith("whge-")) {
                int depth = Integer.parseInt(methodName.replace("whge-", ""));
                StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
                        populationSize, generations,
                        new RandomInitializer<>(random, new BitsGenotypeFactory(genotypeSize)),
                        new Any<BitsGenotype>(),
                        new WeightedHierarchicalMapper<>(depth, true, true, problem.getGrammar()),
                        new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
                                .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
                                .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
                        new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
                        new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3, random),
                        new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(), populationSize,
                        true,
                        problem);
                evolver = new StandardEvolver<>(configuration, N_THREADS, random, false);
                propertiesListener = new PropertiesListener(
                        NumericFitness.comparator(),
                        bitsDistance,
                        phenotypeDistance,
                        new Utils.MapBuilder<Class<? extends GeneticOperator>, String>()
                                .put(AbstractCrossover.class, "crossover")
                                .put(AbstractMutation.class, "mutation").build()
                );
              } else if (methodName.startsWith("cfggp-")) {
                int maxDepth = Integer.parseInt(methodName.replace("cfggp-", ""));
                StandardConfiguration<Node<String>, String, NumericFitness> configuration = new StandardConfiguration<>(
                        populationSize, generations,
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
                        new LastWorst<Individual<Node<String>, String, NumericFitness>>(), populationSize,
                        true,
                        problem);
                evolver = new StandardEvolver<>(configuration, N_THREADS, random, false);
                propertiesListener = new PropertiesListener(
                        NumericFitness.comparator(),
                        phenotypeDistance,
                        phenotypeDistance,
                        new Utils.MapBuilder<Class<? extends GeneticOperator>, String>()
                                .put(AbstractCrossover.class, "crossover")
                                .put(AbstractMutation.class, "mutation").build()
                );
              }
              //go
              //prepare listeners
              List<EvolverListener> listeners = new ArrayList<>();
              listeners.add(new CollectorGenerationLogger<>(
                      constants, System.out, true, 10, " ", " | ",
                      new NumericFirstBest(false, problem.getTestingFitnessComputer(), "%6.2f"),
                      new Diversity(),
                      //propertiesListener,
                      new BestPrinter(problem.getPhenotypePrinter(), "%30.30s")
              ));
              listeners.add(propertiesListener);
              if (filePrintStream != null) {
                listeners.add(new CollectorGenerationLogger<>(
                        constants, filePrintStream, false, header ? 0 : -1, ";", ";",
                        new Population(),
                        new NumericFirstBest(false, problem.getTestingFitnessComputer(), "%6.2f"),
                        new Diversity(),
                        //propertiesListener,
                        new CacheStatistics()
                ));
              }
              evolver.solve(listeners);
              header = false;
            }
          }
        }
      }
    }
    if (filePrintStream != null) {
      filePrintStream.close();
    }
  }

}
