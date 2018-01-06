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
import it.units.malelab.ege.cfggp.initializer.FullTreeFactory;
import it.units.malelab.ege.cfggp.initializer.GrowTreeFactory;
import it.units.malelab.ege.cfggp.mapper.CfgGpMapper;
import it.units.malelab.ege.cfggp.operator.StandardTreeCrossover;
import it.units.malelab.ege.cfggp.operator.StandardTreeMutation;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.Sequence;
import it.units.malelab.ege.core.evolver.DeterministicCrowdingConfiguration;
import it.units.malelab.ege.core.evolver.DeterministicCrowdingEvolver;
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.evolver.PartitionConfiguration;
import it.units.malelab.ege.core.evolver.PartitionEvolver;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.evolver.StandardEvolver;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.initializer.MultiInitializer;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.initializer.RandomInitializer;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.collector.BestPrinter;
import it.units.malelab.ege.core.listener.collector.CacheStatistics;
import it.units.malelab.ege.core.listener.collector.Diversity;
import it.units.malelab.ege.core.listener.collector.NumericFirstBest;
import it.units.malelab.ege.core.listener.collector.Population;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.ranker.ComparableRanker;
import it.units.malelab.ege.core.ranker.RandomizerRanker;
import it.units.malelab.ege.core.ranker.Ranker;
import it.units.malelab.ege.core.selector.FirstBest;
import it.units.malelab.ege.core.selector.IndividualComparator;
import it.units.malelab.ege.core.selector.LastWorst;
import it.units.malelab.ege.core.selector.Tournament;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.ge.genotype.SGEGenotypeFactory;
import it.units.malelab.ege.core.validator.Any;
import it.units.malelab.ege.ge.mapper.BitsSGEMapper;
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
import it.units.malelab.ege.util.distance.Hamming;
import it.units.malelab.ege.util.distance.LeavesEdit;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
public class DeepExperimenter {

  private final static int N_THREADS = Runtime.getRuntime().availableProcessors() - 1;

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
    Random random = new Random(1);
    final int populationSize = 500;
    final int generations = 50;
    final int runs = 10;
    final int tournamentSize = 5;
    //define problems, methods, mappers
    List<String> problems = Lists.newArrayList(
            "bool-parity-5", "bool-parity-8", "bool-mopm-3",
            "sr-keijzer6", "sr-nguyen7", "sr-pagie1",
            "syn-klandscapes-7", "syn-text"
    );
    List<String> methods = Lists.newArrayList(
            "standard",
            "dc-g", "dc-p", "dc-f",
            "p-g-u-20", "p-p-a<-20", "p-p-a>-20", "p-p-u-20", "p-f-a<-20", "p-f-a>-20", "p-f-u-20", "p-f-l<-20", "p-f-l>-20");
    List<String> mappers = Lists.newArrayList(
            "ge-8-5-1024",
            "whge-3-1024",
            "sge-6",
            "cfggp-12");
    PrintStream filePrintStream = null;
    if (args.length > 0) {
      filePrintStream = new PrintStream(args[0]);
    }
    int startingRunIndex = 0;
    if (args.length > 1) {
      startingRunIndex = Integer.parseInt(args[1]);
    }
    //prepare distances
    final Distance<Node<String>> phenotypeDistance = new CachedDistance<>(new LeavesEdit<String>());
    Distance<Sequence<Boolean>> bitsGenotypeDistance = new CachedDistance<>(new Hamming<Boolean>());
    Distance<Sequence<Integer>> sgeGenotypeDistance = new CachedDistance<>(new Hamming<Integer>());
    final Distance fitnessDistance = new Distance<NumericFitness>() {
      @Override
      public double d(NumericFitness f1, NumericFitness f2) {
        return Math.abs(f1.getValue() - f2.getValue());
      }
    };
    //iterate
    boolean header = true;
    for (int run = startingRunIndex; run < startingRunIndex + runs; run++) {
      for (String pr : problems) {
        for (String me : methods) {
          for (String ma : mappers) {
            Map<String, Object> constants = new LinkedHashMap<>();
            constants.put("problem", pr);
            constants.put("mapping", ma);
            constants.put("method", me);
            constants.put("run", run);
            System.out.printf("%nProblem: %s\tMethod: %s\tMapping: %s\tRun: %d%n", pr, me, ma, run);
            //build problem
            Problem<String, NumericFitness> problem = null;
            if (p(pr, 1).equals("parity")) {
              problem = new Parity(i(p(pr, 2)));
            } else if (p(pr, 1).equals("mopm")) {
              problem = new MultipleOutputParallelMultiplier(i(p(pr, 2)));
            } else if (p(pr, 1).equals("keijzer6")) {
              problem = new HarmonicCurve();
            } else if (p(pr, 1).equals("nguyen7")) {
              problem = new Nguyen7(0);
            } else if (p(pr, 1).equals("pagie1")) {
              problem = new Pagie1();
            } else if (p(pr, 1).equals("klandscapes")) {
              problem = new KLandscapes(i(p(pr, 2)));
            } else if (p(pr, 1).equals("text")) {
              problem = new Text();
            }
            //build mapper, operators, initializer, genotype distance
            Mapper mapper = null;
            Map operators = new Utils.MapBuilder<>()
                    .put(new LengthPreservingTwoPointsCrossover(), 0.8d)
                    .put(new ProbabilisticMutation(0.01), 0.2d).build();
            PopulationInitializer populationInitializer = null;
            Distance genotypeDistance = bitsGenotypeDistance;
            if (p(ma, 0).equals("ge")) {
              mapper = new StandardGEMapper(i(p(ma, 1)), i(p(ma, 2)), problem.getGrammar());
              populationInitializer = new RandomInitializer<>(new BitsGenotypeFactory(i(p(ma, 3))));
            } else if (p(ma, 0).equals("pige")) {
              mapper = new PiGEMapper(i(p(ma, 1)), i(p(ma, 2)), problem.getGrammar());
              populationInitializer = new RandomInitializer<>(new BitsGenotypeFactory(i(p(ma, 3))));
            } else if (p(ma, 0).equals("hge")) {
              mapper = new HierarchicalMapper(problem.getGrammar());
              populationInitializer = new RandomInitializer<>(new BitsGenotypeFactory(i(p(ma, 1))));
            } else if (p(ma, 0).equals("whge")) {
              mapper = new WeightedHierarchicalMapper(i(p(ma, 1)), problem.getGrammar());
              populationInitializer = new RandomInitializer<>(new BitsGenotypeFactory(i(p(ma, 2))));
            } else if (p(ma, 0).equals("bitsge")) {
              mapper = new BitsSGEMapper(i(p(ma, 1)), problem.getGrammar());
              populationInitializer = new RandomInitializer<>(new BitsGenotypeFactory(i(p(ma, 2))));
            } else if (p(ma, 0).equals("sge")) {
              mapper = new SGEMapper(i(p(ma, 1)), problem.getGrammar());
              operators = new Utils.MapBuilder<>()
                      .put(new SGECrossover<>(), 0.8d)
                      .put(new SGEMutation<>(0.01, (SGEMapper<String>) mapper), 0.2d).build();
              populationInitializer = new RandomInitializer<>(new SGEGenotypeFactory<>((SGEMapper<String>) mapper));
              genotypeDistance = sgeGenotypeDistance;
            } else if (p(ma, 0).equals("cfggp")) {
              mapper = new CfgGpMapper();
              operators = new Utils.MapBuilder<>()
                      .put(new StandardTreeCrossover<>(i(p(ma, 1))), 0.8d)
                      .put(new StandardTreeMutation<>(i(p(ma, 1)), problem.getGrammar()), 0.2d).build();
              populationInitializer = new MultiInitializer<>(new Utils.MapBuilder<PopulationInitializer<Node<String>>, Double>()
                      .put(new RandomInitializer<>(new GrowTreeFactory<>(i(p(ma, 1)), problem.getGrammar())), 0.5)
                      .put(new RandomInitializer<>(new FullTreeFactory<>(i(p(ma, 1)), problem.getGrammar())), 0.5)
                      .build());
              genotypeDistance = phenotypeDistance;
            }
            //build configuration and evolver            
            Evolver evolver = null;
            if (p(me, 0).equals("standard")) {
              StandardConfiguration configuration = new StandardConfiguration(
                      populationSize, generations,
                      populationInitializer,
                      new Any(),
                      mapper,
                      operators,
                      new ComparableRanker(new IndividualComparator(IndividualComparator.Attribute.FITNESS)),
                      new Tournament(tournamentSize),
                      new LastWorst(),
                      1,
                      true,
                      problem);
              evolver = new StandardEvolver(configuration, true, false);
            } else if (p(me, 0).equals("dc")) {
              final Distance localGenotypeDistance = genotypeDistance;
              Distance distance = null;
              if (p(me, 1).equals("g")) {
                distance = new Distance<Individual>() {
                  @Override
                  public double d(Individual i1, Individual i2) {
                    return localGenotypeDistance.d(i1.getGenotype(), i2.getGenotype());
                  }
                };
              } else if (p(me, 1).equals("p")) {
                distance = new Distance<Individual>() {
                  @Override
                  public double d(Individual i1, Individual i2) {
                    return phenotypeDistance.d(i1.getPhenotype(), i2.getPhenotype());
                  }
                };
              } else if (p(me, 1).equals("f")) {
                distance = new Distance<Individual>() {
                  @Override
                  public double d(Individual i1, Individual i2) {
                    return fitnessDistance.d(i1.getFitness(), i2.getFitness());
                  }
                };
              }
              DeterministicCrowdingConfiguration configuration = new DeterministicCrowdingConfiguration(
                      distance,
                      populationSize,
                      generations,
                      populationInitializer,
                      new Any(),
                      mapper,
                      operators,
                      new ComparableRanker(new IndividualComparator(IndividualComparator.Attribute.FITNESS)),
                      new Tournament(tournamentSize),
                      problem);
              evolver = new DeterministicCrowdingEvolver(configuration, true, false);
            } else if (p(me, 0).equals("p")) {
              Ranker<Individual> parentInPartitionRanker = null;
              Comparator<Individual> partitionerComparator = null;
              if (p(me, 1).equals("g")) {
                partitionerComparator = new IndividualComparator(IndividualComparator.Attribute.GENO);
              } else if (p(me, 1).equals("p")) {
                partitionerComparator = new IndividualComparator(IndividualComparator.Attribute.PHENO);
              } else if (p(me, 1).equals("f")) {
                partitionerComparator = new IndividualComparator(IndividualComparator.Attribute.FITNESS);
              }
              if (p(me, 2).equals("u")) {
                parentInPartitionRanker = new RandomizerRanker();
              } else if (p(me, 2).equals("a<")) {
                parentInPartitionRanker = new ComparableRanker(new IndividualComparator(IndividualComparator.Attribute.AGE));
              } else if (p(me, 2).equals("a>")) {
                parentInPartitionRanker = new ComparableRanker(Collections.reverseOrder(new IndividualComparator(IndividualComparator.Attribute.AGE)));
              } else if (p(me, 2).equals("l<")) {
                parentInPartitionRanker = new ComparableRanker(new IndividualComparator(IndividualComparator.Attribute.PHENO_SIZE));
              } else if (p(me, 2).equals("l>")) {
                parentInPartitionRanker = new ComparableRanker(Collections.reverseOrder(new IndividualComparator(IndividualComparator.Attribute.PHENO_SIZE)));
              }
              PartitionConfiguration configuration = new PartitionConfiguration(
                      partitionerComparator,
                      i(p(me, 3)),
                      parentInPartitionRanker,
                      new FirstBest(),
                      parentInPartitionRanker,
                      new LastWorst(),
                      populationSize,
                      generations,
                      populationInitializer,
                      new Any(),
                      mapper,
                      operators,
                      new ComparableRanker(new IndividualComparator(IndividualComparator.Attribute.FITNESS)),
                      new Tournament(tournamentSize),
                      new LastWorst(),
                      1,
                      true,
                      problem);
              evolver = new PartitionEvolver(configuration, true, false);
            }
            //prepare listeners and go
            List<EvolverListener> listeners = new ArrayList<>();
            listeners.add(new CollectorGenerationLogger<>(
                    constants, System.out, true, 10, " ", " | ",
                    new Population(),
                    new NumericFirstBest(false, problem.getTestingFitnessComputer(), "%6.2f"),
                    new Diversity(),
                    new BestPrinter(problem.getPhenotypePrinter(), "%30.30s")
            ));
            if (filePrintStream != null) {
              listeners.add(new CollectorGenerationLogger<>(
                      constants, filePrintStream, false, header ? 0 : -1, ";", ";",
                      new Population(),
                      new NumericFirstBest(false, problem.getTestingFitnessComputer(), "%6.2f"),
                      new Diversity(),
                      new CacheStatistics()
              ));
            }
            evolver.solve(executor, random, listeners);
            header = false;
          }
        }

      }
    }
    if (filePrintStream != null) {
      filePrintStream.close();
    }
  }

  private static String p(String s, int n) {
    String[] pieces = s.split("-");
    if (n < pieces.length) {
      return pieces[n];
    }
    return null;
  }

  private static int i(String s) {
    return Integer.parseInt(s);
  }

}
