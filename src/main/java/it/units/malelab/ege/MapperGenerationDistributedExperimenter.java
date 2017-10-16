/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.benchmark.KLandscapes;
import it.units.malelab.ege.benchmark.Text;
import it.units.malelab.ege.benchmark.mapper.MapperGeneration;
import it.units.malelab.ege.benchmark.mapper.MappingPropertiesFitness;
import it.units.malelab.ege.benchmark.mapper.RecursiveMapper;
import it.units.malelab.ege.benchmark.symbolicregression.Pagie1;
import it.units.malelab.ege.cfggp.initializer.FullTreeFactory;
import it.units.malelab.ege.cfggp.initializer.GrowTreeFactory;
import it.units.malelab.ege.cfggp.mapper.CfgGpMapper;
import it.units.malelab.ege.cfggp.operator.StandardTreeCrossover;
import it.units.malelab.ege.cfggp.operator.StandardTreeMutation;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.evolver.PartitionConfiguration;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.initializer.MultiInitializer;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.initializer.RandomInitializer;
import it.units.malelab.ege.core.listener.collector.BestPrinter;
import it.units.malelab.ege.core.listener.collector.Collector;
import it.units.malelab.ege.core.listener.collector.Diversity;
import it.units.malelab.ege.core.listener.collector.MultiObjectiveFitnessFirstBest;
import it.units.malelab.ege.core.listener.collector.NumericFirstBest;
import it.units.malelab.ege.core.listener.collector.Population;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.ranker.ComparableRanker;
import it.units.malelab.ege.core.ranker.ParetoRanker;
import it.units.malelab.ege.core.selector.FirstBest;
import it.units.malelab.ege.core.selector.IndividualComparator;
import it.units.malelab.ege.core.selector.LastWorst;
import it.units.malelab.ege.core.selector.Tournament;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.core.validator.Any;
import it.units.malelab.ege.distributed.DistributedUtils;
import it.units.malelab.ege.distributed.Job;
import it.units.malelab.ege.distributed.master.Master;
import it.units.malelab.ege.ge.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.ge.operator.ProbabilisticMutation;
import it.units.malelab.ege.util.Pair;
import it.units.malelab.ege.util.Utils;
import static it.units.malelab.ege.util.Utils.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class MapperGenerationDistributedExperimenter {

  private final static Logger L = Logger.getLogger(MapperGenerationDistributedExperimenter.class.getName());
  
  private final int maxDepth = 16;
  private final int popSize = 500;
  private final int nGenotypes = 100;
  private final int expressivenessDepth = 2;
  private final int runs = 10;
  private final int learningGenotypeSize = 256;
  private final int learningMaxMappingDepth = 9;
  private final int validationGenotypeSize = 1024;
  private final int validationMaxMappingDepth = 9;
  private final int validationGenerations = 30;
  private final int validationPopSize = 500;
  private final int validationRuns = 10;
  private final Map<String, Problem<String, NumericFitness>> validationProblems = new LinkedHashMap<>();
  private final List<Problem<String, NumericFitness>> learningProblems = new ArrayList<>();
  private final Map<String, Node<String>> baselines = new LinkedHashMap<>();
  private final Master master;

  public MapperGenerationDistributedExperimenter(String keyPhrase, int port, String baseResultDirName, String baseResultFileName) throws IOException {
    validationProblems.put("Pagie1", new Pagie1());
    validationProblems.put("KLand-5", new KLandscapes(5));
    validationProblems.put("Text", new Text());
    learningProblems.add(new Pagie1());
    baselines.put("GE", getGERawTree());
    baselines.put("HGE", getHGERawTree());
    baselines.put("WGE", getWHGERawTree());
    master = new Master(keyPhrase, port, baseResultDirName, baseResultFileName);
    master.start();
  }

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    //prepare master
    String keyPhrase = args[0];
    int port = Integer.parseInt(args[1]);
    String baseResultDirName = args[2];
    String baseResultFileName = args[3];
    MapperGenerationDistributedExperimenter experimenter = new MapperGenerationDistributedExperimenter(keyPhrase, port, baseResultDirName, baseResultFileName);
  }

  public void start() throws IOException {
    //fitnessis
    MappingPropertiesFitness.Property[][] propertiesArrays = new MappingPropertiesFitness.Property[][]{
      new MappingPropertiesFitness.Property[]{MappingPropertiesFitness.Property.REDUNDANCY},
      new MappingPropertiesFitness.Property[]{MappingPropertiesFitness.Property.REDUNDANCY, MappingPropertiesFitness.Property.NON_LOCALITY},
      new MappingPropertiesFitness.Property[]{MappingPropertiesFitness.Property.REDUNDANCY, MappingPropertiesFitness.Property.NON_LOCALITY, MappingPropertiesFitness.Property.NON_UNIFORMITY},};
    //baseline jobs
    for (Map.Entry<String, Node<String>> baselineEntry : baselines.entrySet()) {
      String mapperName = baselineEntry.getKey();
      Node<String> mapper = baselineEntry.getValue();
      submitValidationJobs(mapper, mapperName, 0, 0);
    }
    //results (futures)
    Map<Job, Future<List<Node>>> resultsMap = new HashMap<>();
    for (MappingPropertiesFitness.Property[] properties : propertiesArrays) {
      Random random = new Random(1l);
      Problem<String, MultiObjectiveFitness> problem = new MapperGeneration(
              learningGenotypeSize,
              nGenotypes,
              learningMaxMappingDepth,
              random,
              learningProblems,
              properties
      );
      String propertiesString = "";
      for (MappingPropertiesFitness.Property property : properties) {
        propertiesString = propertiesString + property.toString().substring(0, 1).toUpperCase()+property.toString().substring(1).toLowerCase();
      }
      List<Collector<Node<String>, String, MultiObjectiveFitness>> collectors = Arrays.asList(
              new Population<Node<String>, String, MultiObjectiveFitness>(),
              new MultiObjectiveFitnessFirstBest<Node<String>, String>(false, problem.getTestingFitnessComputer(), rep("%4.2f", properties.length)),
              new Diversity<Node<String>, String, MultiObjectiveFitness>(),
              new BestPrinter<Node<String>, String, MultiObjectiveFitness>(problem.getPhenotypePrinter(), "%30.30s"));
      for (int r = 0; r < runs; r++) {
        Job job = buildLearningJob(problem, propertiesString, r, (List) collectors);
        resultsMap.put(job, master.submit(job));
      }
    }
    //wait for results
    Pair<Job, List<Node>> resultsPair;
    while ((resultsPair = DistributedUtils.getAny(resultsMap, 250))!=null) {
      Job job = resultsPair.getFirst();
      List<Node> results = resultsPair.getSecond();
      for (Node result : results) {
        //run validation jobs
      }
    }
  }
  
  private void submitValidationJobs(Node<String> mapper, String mapperName, int outerRun, int inRankIndex) {
    L.info(String.format("Submitting validation jobs for mapper %s:%d:%d", mapperName, outerRun, inRankIndex));
    for (int r = 0; r < validationRuns; r++) {
      for (Map.Entry<String, Problem<String, NumericFitness>> problemEntry : validationProblems.entrySet()) {
        List<Collector<BitsGenotype, String, NumericFitness>> collectors = Arrays.asList(
                new Population<BitsGenotype, String, NumericFitness>(),
                new NumericFirstBest<BitsGenotype, String>(false, problemEntry.getValue().getTestingFitnessComputer(), "%6.2f"),
                new Diversity<BitsGenotype, String, NumericFitness>());
        master.submit(buildValidationJob(mapper, mapperName, outerRun, inRankIndex, problemEntry.getValue(), problemEntry.getKey(), r, (List)collectors));
      }
    }
  }

  public static Node<String> getGERawTree() {
    return node("<mapper>",
            node("<n>",
                    node("<fun_n_g>",
                            node("int")
                    ),
                    node("("),
                    node("<g>",
                            node("<fun_g_g,n>",
                                    node("substring")
                            ),
                            node("("),
                            node("<g>",
                                    node("<fun_g_g,n>",
                                            node("rotate_sx")
                                    ),
                                    node("("),
                                    node("<g>",
                                            node("<var_g>",
                                                    node("g")
                                            )
                                    ),
                                    node(","),
                                    node("<n>",
                                            node("<fun_n_n,n>",
                                                    node("*")
                                            ),
                                            node("("),
                                            node("<n>",
                                                    node("<var_n>",
                                                            node("g_count_rw")
                                                    )),
                                            node(","),
                                            node("<n>",
                                                    node("<const_n>",
                                                            node("8")
                                                    )),
                                            node(")")
                                    ),
                                    node(")")
                            ),
                            node(","),
                            node("<n>",
                                    node("<const_n>",
                                            node("8")
                                    )
                            ),
                            node(")")
                    ),
                    node(")")
            ),
            node("<lg>",
                    node("<fun_lg_g,n>",
                            node("repeat")
                    ),
                    node("("),
                    node("<g>",
                            node("<var_g>",
                                    node("g")
                            )
                    ),
                    node(","),
                    node("<n>",
                            node("<fun_n_ln>",
                                    node("length")
                            ),
                            node("("),
                            node("<ln>",
                                    node("<var_ln>",
                                            node("ln")
                                    )
                            ),
                            node(")")
                    ),
                    node(")")
            )
    );
  }

  public static Node<String> getWHGERawTree() {
    return node("<mapper>",
            node("<n>",
                    node("<fun_n_ln>",
                            node("max_index")
                    ),
                    node("("),
                    node("<ln>",
                            node("apply"),
                            node("("),
                            node("<fun_n_g>",
                                    node("weight_r")),
                            node(","),
                            node("<lg>",
                                    node("<fun_lg_g,n>",
                                            node("split")
                                    ),
                                    node("("),
                                    node("<g>",
                                            node("<var_g>",
                                                    node("g")
                                            )
                                    ),
                                    node(","),
                                    node("<n>",
                                            node("<fun_n_ln>",
                                                    node("length")
                                            ),
                                            node("("),
                                            node("<ln>",
                                                    node("<var_ln>",
                                                            node("ln")
                                                    )
                                            ),
                                            node(")")
                                    ),
                                    node(")")
                            ),
                            node(")")
                    ),
                    node(")")
            ),
            node("<lg>",
                    node("<fun_lg_g,ln>",
                            node("split_w")
                    ),
                    node("("),
                    node("<g>",
                            node("<var_g>",
                                    node("g")
                            )
                    ),
                    node(","),
                    node("<ln>",
                            node("<var_ln>",
                                    node("ln")
                            )
                    ),
                    node(")")
            )
    );
  }

  public static Node<String> getHGERawTree() {
    return node("<mapper>",
            node("<n>",
                    node("<fun_n_ln>",
                            node("max_index")
                    ),
                    node("("),
                    node("<ln>",
                            node("apply"),
                            node("("),
                            node("<fun_n_g>",
                                    node("weight_r")),
                            node(","),
                            node("<lg>",
                                    node("<fun_lg_g,n>",
                                            node("split")
                                    ),
                                    node("("),
                                    node("<g>",
                                            node("<var_g>",
                                                    node("g")
                                            )
                                    ),
                                    node(","),
                                    node("<n>",
                                            node("<fun_n_ln>",
                                                    node("length")
                                            ),
                                            node("("),
                                            node("<ln>",
                                                    node("<var_ln>",
                                                            node("ln")
                                                    )
                                            ),
                                            node(")")
                                    ),
                                    node(")")
                            ),
                            node(")")
                    ),
                    node(")")
            ),
            node("<lg>",
                    node("<fun_lg_g,n>",
                            node("split")
                    ),
                    node("("),
                    node("<g>",
                            node("<var_g>",
                                    node("g")
                            )
                    ),
                    node(","),
                    node("<n>",
                            node("<fun_n_ln>",
                                    node("length")
                            ),
                            node("("),
                            node("<ln>",
                                    node("<var_ln>",
                                            node("ln")
                                    )
                            ),
                            node(")")
                    ),
                    node(")")
            )
    );
  }

  private Job buildValidationJob(
          Node<String> rawMapper, String mapperName, int outerRun, int inRankIndex,
          Problem<String, NumericFitness> problem, String problemName,
          int run,
          List<Collector> collectors) {
    StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
            validationPopSize,
            validationGenerations,
            new RandomInitializer<>(new BitsGenotypeFactory(validationGenotypeSize)),
            new Any<BitsGenotype>(),
            new RecursiveMapper<>(rawMapper, validationMaxMappingDepth, expressivenessDepth, problem.getGrammar()),
            new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
                    .put(new LengthPreservingTwoPointsCrossover(), 0.8d)
                    .put(new ProbabilisticMutation(0.01), 0.2d).build(),
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
            new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3),
            new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(),
            validationPopSize,
            true,
            problem);
    Map<String, Object> keys = new LinkedHashMap<>();
    keys.put("problem", problemName);
    keys.put("mapper", mapperName);
    keys.put("outer.run", outerRun);
    keys.put("in.rank.index", inRankIndex);
    keys.put(Master.RANDOM_SEED_NAME, run);
    return new Job(configuration, collectors, keys, validationPopSize);
  }

  private Job buildLearningJob(Problem<String, MultiObjectiveFitness> problem, String fitnessName, int run, List<Collector> collectors) {
    PartitionConfiguration<Node<String>, String, MultiObjectiveFitness> configuration = new PartitionConfiguration<>(
            new IndividualComparator<Node<String>, String, MultiObjectiveFitness>(IndividualComparator.Attribute.PHENO),
            10,
            new ComparableRanker<>(new IndividualComparator<Node<String>, String, MultiObjectiveFitness>(IndividualComparator.Attribute.AGE)),
            new FirstBest<Individual<Node<String>, String, MultiObjectiveFitness>>(),
            new ComparableRanker<>(new IndividualComparator<Node<String>, String, MultiObjectiveFitness>(IndividualComparator.Attribute.AGE)),
            new LastWorst<Individual<Node<String>, String, MultiObjectiveFitness>>(),
            popSize,
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
            new ParetoRanker<Node<String>, String, MultiObjectiveFitness>(),
            new Tournament<Individual<Node<String>, String, MultiObjectiveFitness>>(3),
            new LastWorst<Individual<Node<String>, String, MultiObjectiveFitness>>(),
            popSize,
            true,
            problem);
    Map<String, Object> keys = new LinkedHashMap<>();
    keys.put("fitness", fitnessName);
    keys.put(Master.RANDOM_SEED_NAME, run);
    return new Job(configuration, collectors, keys, popSize);
  }

  private static String[] rep(String s, int n) {
    String[] ss = new String[n];
    for (int i = 0; i < n; i++) {
      ss[i] = s;
    }
    return ss;
  }

  private static String join(String[] ss, String joiner) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ss.length; i++) {
      sb.append(ss[i]);
      if (i < ss.length - 1) {
        sb.append(joiner);
      }
    }
    return sb.toString();
  }

  private static Object[] join(Object[]... as) {
    int s = 0;
    for (Object[] a : as) {
      s = s + a.length;
    }
    Object[] result = new Object[s];
    int c = 0;
    for (Object[] a : as) {
      System.arraycopy(a, 0, result, c, a.length);
      c = c + a.length;
    }
    return result;
  }

  private static Object[] a(Object o) {
    return new Object[]{o};
  }

}
