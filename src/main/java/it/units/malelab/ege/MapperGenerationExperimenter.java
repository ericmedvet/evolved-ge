/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.benchmark.KLandscapes;
import it.units.malelab.ege.benchmark.Text;
import it.units.malelab.ege.benchmark.mapper.MapperGeneration;
import it.units.malelab.ege.benchmark.mapper.MapperUtils;
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
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.evolver.PartitionConfiguration;
import it.units.malelab.ege.core.evolver.PartitionEvolver;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.evolver.StandardEvolver;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.initializer.MultiInitializer;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.initializer.RandomInitializer;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.collector.BestPrinter;
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
import it.units.malelab.ege.ge.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.ge.operator.ProbabilisticMutation;
import it.units.malelab.ege.util.Pair;
import it.units.malelab.ege.util.Utils;
import static it.units.malelab.ege.util.Utils.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author eric
 */
public class MapperGenerationExperimenter {
  
  private final static int N_THREADS = Runtime.getRuntime().availableProcessors() - 1;

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    //params
    int maxDepth = 16;
    int popSize = 500;
    int nGenotypes = 100;
    int expressivenessDepth = 2;
    int runs = 10;
    int learningGenotypeSize = 256;
    int learningMaxMappingDepth = 9;
    int validationGenotypeSize = 1024;
    int validationMaxMappingDepth = 9;
    int validationGenerations = 30;
    int validationPopSize = 500;
    int validationRuns = 10;
    Map<String, Problem<String, NumericFitness>> validationProblems = new LinkedHashMap<>();
    validationProblems.put("Pagie1", new Pagie1());
    validationProblems.put("KLand-5", new KLandscapes(5));
    validationProblems.put("Text", new Text());
    List<Problem<String, NumericFitness>> learningProblems = new ArrayList<>();
    learningProblems.add(new Pagie1());
    PrintStream mainFilePrintStream = null;
    PrintStream validationFilePrintStream = null;
    Set<MappingPropertiesFitness.Property> propertySet = new LinkedHashSet<>();
    //read args
    if (args.length > 0) {
      mainFilePrintStream = new PrintStream(args[0]);
      if (args.length > 1) {
        validationFilePrintStream = new PrintStream(args[1]);
      }
      if (args.length > 2) {
        String[] pieces = args[2].split("\\|");
        for (String piece : pieces) {
          for (MappingPropertiesFitness.Property property : MappingPropertiesFitness.Property.values()) {
            if (property.name().equalsIgnoreCase(piece)) {
              propertySet.add(property);
              break;
            }
          }
        }
      }
    }
    if (propertySet.isEmpty()) {
      propertySet.addAll(Arrays.asList(MappingPropertiesFitness.Property.values()));
    }
    //prepare things
    MappingPropertiesFitness.Property[] properties = propertySet.toArray(new MappingPropertiesFitness.Property[0]);
    Random random = new Random(1l);
    ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
    Problem<String, MultiObjectiveFitness> problem = new MapperGeneration(
            learningGenotypeSize,
            nGenotypes,
            learningMaxMappingDepth,
            random,
            learningProblems,
            properties
    );
    //print summary
    System.out.println("Problems: " + validationProblems.keySet());
    System.out.println("Properties: " + Arrays.toString(properties));
    //print baselines
    Map<String, Node<String>> baselines = new LinkedHashMap<>();
    baselines.put("GE", getGERawTree());
    baselines.put("HGE", getHGERawTree());
    baselines.put("WGE", getWHGERawTree());
    //baselines.clear(); //TODO remove to show baselines performance
    boolean validationHeader = true;
    boolean mainHeader = true;
    for (String baselineName : baselines.keySet()) {
      MultiObjectiveFitness moF = problem.getLearningFitnessComputer().compute(baselines.get(baselineName));
      System.out.printf("%5.5s fitness:\t" + join(rep("%4.2f", properties.length), " ") + "%n",
              join(a(baselineName), moF.getValue())
      );
      for (String validationProblemName : validationProblems.keySet()) {
        for (int r = 0; r < validationRuns; r++) {
          Pair<Node<String>, NumericFitness> best = applyMapperOnProblem(
                  baselines.get(baselineName),
                  validationProblems.get(validationProblemName),
                  validationPopSize, validationGenotypeSize, validationGenerations, validationMaxMappingDepth, expressivenessDepth,
                  baselineName, r, validationProblemName,
                  validationHeader, validationFilePrintStream
          );
          System.out.printf("\t%10.10s %2d bf=%6.3f depth=%3d size=%4d %30.30s%n",
                  validationProblemName, r, best.getSecond().getValue(),
                  best.getFirst().depth(), best.getFirst().nodeSize(),
                  validationProblems.get(validationProblemName).getPhenotypePrinter().toString(best.getFirst())
          );
          validationHeader = false;
        }
      }
    }
    //prepare configuration
    for (int s = 0; s < runs; s++) {
      Random innerRandom = new Random(1l);
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
      List<EvolverListener<Node<String>, String, MultiObjectiveFitness>> listeners = new ArrayList<>();
      listeners.add(new CollectorGenerationLogger<>(
              (Map) Collections.singletonMap("run", s), System.out, true, 10, " ", " | ",
              new Population<Node<String>, String, MultiObjectiveFitness>(),
              new MultiObjectiveFitnessFirstBest<Node<String>, String>(false, problem.getTestingFitnessComputer(), rep("%4.2f", properties.length)),
              new Diversity<Node<String>, String, MultiObjectiveFitness>(),
              new BestPrinter<Node<String>, String, MultiObjectiveFitness>(problem.getPhenotypePrinter(), "%30.30s")
      ));
      if (mainFilePrintStream != null) {
        listeners.add(new CollectorGenerationLogger<>(
                (Map) Collections.singletonMap("run", s), mainFilePrintStream, false, mainHeader ? 0 : -1, ";", ";",
                new Population<Node<String>, String, MultiObjectiveFitness>(),
                new MultiObjectiveFitnessFirstBest<Node<String>, String>(false, problem.getTestingFitnessComputer(), rep("%4.2f", properties.length)),
                new Diversity<Node<String>, String, MultiObjectiveFitness>(),
                new BestPrinter<Node<String>, String, MultiObjectiveFitness>(problem.getPhenotypePrinter(), "%30.30s")
        ));
      }
      mainHeader = false;
      Evolver<Node<String>, String, MultiObjectiveFitness> evolver = new PartitionEvolver<>(configuration, false);
      List<Node<String>> bests = evolver.solve(executor, random, listeners);
      System.out.printf("Found %d solutions.%n", bests.size());
      String mapperName = "best";
      for (String validationProblemName : validationProblems.keySet()) {
        for (int r = 0; r < validationRuns; r++) {
          Pair<Node<String>, NumericFitness> best = applyMapperOnProblem(
                  bests.get(0),
                  validationProblems.get(validationProblemName),
                  validationPopSize, validationGenotypeSize, validationGenerations, validationMaxMappingDepth, expressivenessDepth,
                  mapperName, r, validationProblemName,
                  validationHeader, validationFilePrintStream
          );
          System.out.printf("\t%10.10s %2d bf=%6.3f depth=%3d size=%4d %30.30s%n",
                  validationProblemName, r, best.getSecond().getValue(),
                  best.getFirst().depth(), best.getFirst().nodeSize(),
                  validationProblems.get(validationProblemName).getPhenotypePrinter().toString(best.getFirst())
          );
          validationHeader = false;
        }
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

  private static Pair<Node<String>, NumericFitness> applyMapperOnProblem(
          Node<String> rawMapper,
          Problem<String, NumericFitness> problem,
          int popSize, int genotypeSize, int generations, int maxMappingDepth, int expressivenessDepth,
          String mapperName, int run, String problemName,
          boolean header, PrintStream ps) throws InterruptedException, ExecutionException {
    Random random = new Random(run);
    ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
    StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
            popSize,
            generations,
            new RandomInitializer<>(new BitsGenotypeFactory(genotypeSize)),
            new Any<BitsGenotype>(),
            new RecursiveMapper<>(rawMapper, maxMappingDepth, expressivenessDepth, problem.getGrammar()),
            new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
                    .put(new LengthPreservingTwoPointsCrossover(), 0.8d)
                    .put(new ProbabilisticMutation(0.01), 0.2d).build(),
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
            new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3),
            new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(),
            popSize,
            true,
            problem);
    List<EvolverListener<BitsGenotype, String, NumericFitness>> listeners = new ArrayList<>();
    if (ps != null) {
      listeners.add(new CollectorGenerationLogger<>(
              new Utils.MapBuilder<String, Object>()
                      .put("mapper", mapperName)
                      .put("problem", problemName)
                      .put("run", run).build(),
              ps, false, header ? 0 : -1, ";", ";",
              new Population<BitsGenotype, String, NumericFitness>(),
              new NumericFirstBest<BitsGenotype, String>(false, problem.getTestingFitnessComputer(), "%6.2f"),
              new Diversity<BitsGenotype, String, NumericFitness>())
      );
    }
    Evolver<BitsGenotype, String, NumericFitness> evolver = new StandardEvolver<>(configuration, false);
    List<Node<String>> bests = evolver.solve(executor, random, listeners);
    return new Pair<>(bests.get(0), problem.getLearningFitnessComputer().compute(bests.get(0)));
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
