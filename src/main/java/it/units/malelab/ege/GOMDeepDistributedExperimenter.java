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
import it.units.malelab.ege.core.evolver.PartitionConfiguration;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.evolver.geneoptimalmixing.GOMConfiguration;
import it.units.malelab.ege.core.evolver.geneoptimalmixing.fos.FOSBuilder;
import it.units.malelab.ege.core.evolver.geneoptimalmixing.fos.Grouped;
import it.units.malelab.ege.core.evolver.geneoptimalmixing.fos.RandomTree;
import it.units.malelab.ege.core.evolver.geneoptimalmixing.fos.SGEGeneBounds;
import it.units.malelab.ege.core.evolver.geneoptimalmixing.fos.UPGMAMutualInformationTree;
import it.units.malelab.ege.core.evolver.geneoptimalmixing.fos.Univariate;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.initializer.MultiInitializer;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.initializer.RandomInitializer;
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
import it.units.malelab.ege.distributed.Job;
import it.units.malelab.ege.distributed.master.Master;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class GOMDeepDistributedExperimenter {

  private final static Logger L = Logger.getLogger(GOMDeepDistributedExperimenter.class.getName());

  //java -cp EvolvedGrammaticalEvolution-1.0-SNAPSHOT.jar:. it.units.malelab.ege.DeepDistributedExperimenter hi 9000 diversities
  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    //prepare master
    String keyPhrase = args[0];
    int port = Integer.parseInt(args[1]);
    String baseResultDirName = args[2];
    String baseResultFileName = args[3];
    Master master = new Master(keyPhrase, port, baseResultDirName, baseResultFileName);
    master.start();
    List<Future<List<Node>>> results = new ArrayList<>();
    //prepare things
    int populationSize = 500;
    int generations = 50;
    int tournamentSize = 3;
    //define problems, methods, mappers
    String textProblemTargetString = "Hello World! Many things to you! Dear friend, ciao!";
    int[] runs = new int[]{0, 1}; //, 2, 3, 4, 5, 6, 7, 8, 9};
    List<String> problems = Lists.newArrayList(
            "syn-klandscapes-5"//, "syn-text-12", "bool-parity-5" 
    );
    List<String> methods = Lists.newArrayList(
            //"standard",
            "gom-u", "gom-nat", "gom-rt", "gom-lt"
    );
    List<String> mappers = Lists.newArrayList(
            "ge-8-5-256",
            "whge-3-256",
            "sge-6"
    );
    //iterate
    for (int run : runs) {
      for (String pr : problems) {
        for (String me : methods) {
          for (String ma : mappers) {
            Map<String, Object> keys = new LinkedHashMap<>();
            keys.put("problem", pr);
            keys.put("mapping", ma);
            keys.put("method", me);
            keys.put(Master.RANDOM_SEED_NAME, run);
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
              problem = new Text(textProblemTargetString.substring(0, i(p(pr, 2))));
            }
            //build mapper, operators, initializer, genotype distance
            Mapper mapper = null;
            Map operators = new Utils.MapBuilder<>()
                    .put(new LengthPreservingTwoPointsCrossover(), 0.8d)
                    .put(new ProbabilisticMutation(0.01), 0.2d).build();
            PopulationInitializer populationInitializer = null;
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
            }
            //build configuration
            StandardConfiguration configuration = null;
            if (p(me, 0).equals("standard")) {
              configuration = new StandardConfiguration(
                      populationSize,
                      generations,
                      populationInitializer,
                      new Any(),
                      mapper,
                      operators,
                      new ComparableRanker(new IndividualComparator(IndividualComparator.Attribute.FITNESS)),
                      new Tournament(tournamentSize),
                      new LastWorst(),
                      populationSize,
                      true,
                      problem);
            } else if (p(me, 0).equals("gom")) {
              FOSBuilder fosBuilder = null;
              if (p(me, 1).equals("u")) {
                fosBuilder = new Univariate();
              } else if (p(me, 1).equals("nat")) {
                if (p(ma, 0).equals("ge")||p(ma, 0).equals("pige")) {
                  fosBuilder = new Grouped(i(p(ma, 1)));
                } else if (p(ma, 0).equals("sge")) {
                  fosBuilder = new SGEGeneBounds((SGEMapper)mapper);
                } else {
                  continue;
                }
              } else if (p(me, 1).equals("rt")) {
                fosBuilder = new RandomTree(1, 0);
              } else if (p(me, 1).equals("lt")) {
                fosBuilder = new UPGMAMutualInformationTree(1, 0);
              }
              configuration = new GOMConfiguration(
                      fosBuilder,
                      null,
                      populationSize,
                      generations,
                      populationInitializer,
                      new Any(),
                      mapper,
                      new ComparableRanker(new IndividualComparator(IndividualComparator.Attribute.FITNESS)),
                      problem);
            }
            Job job = new Job(
                    configuration,
                    Arrays.asList(new Population(),
                            new NumericFirstBest(false, problem.getTestingFitnessComputer(), "%6.2f"),
                            new Diversity(),
                            new CacheStatistics(),
                            new BestPrinter(problem.getPhenotypePrinter(), "%30.30s")),
                    keys,
                    configuration.getOffspringSize(),
                    true
            );
            L.info(String.format("Submitting job: %s%n", job));
            results.add(master.submit(job));
          }
        }
      }
    }
    L.info(String.format("%d job submitted.%n", results.size()));
    for (Future<List<Node>> result : results) {
      L.info(String.format("Got %d solutions%n", result.get().size()));
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
