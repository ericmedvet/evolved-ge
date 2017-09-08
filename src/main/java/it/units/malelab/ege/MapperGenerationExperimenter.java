/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.benchmark.KLandscapes;
import it.units.malelab.ege.benchmark.Text;
import it.units.malelab.ege.benchmark.booleanfunction.Parity;
import it.units.malelab.ege.benchmark.mapper.MapperGeneration;
import it.units.malelab.ege.benchmark.mapper.RecursiveMapper;
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
import it.units.malelab.ege.core.listener.collector.CacheStatistics;
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
import it.units.malelab.ege.ge.genotype.validator.Any;
import it.units.malelab.ege.ge.mapper.WeightedHierarchicalMapper;
import it.units.malelab.ege.ge.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.ge.operator.ProbabilisticMutation;
import it.units.malelab.ege.util.Utils;
import static it.units.malelab.ege.util.Utils.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class MapperGenerationExperimenter {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    int maxDepth = 16;
    int popSize = 500;
    int genotypeSize = 256;
    int nGenotypes = 100;
    int expressivenessDepth = 2;
    int runs = 10;
    int innerMaxMappingDepth = 8;
    int innerGenerations = 30;
    int innerPopSize = 250;
    int innerRuns = 3;
    PrintStream mainFilePrintStream = new PrintStream(args[0]);
    PrintStream innerFilePrintStream = new PrintStream(args[1]);
    Random random = new Random(1l);
    Map<String, Problem<String, NumericFitness>> innerProblems = new LinkedHashMap<>();
    innerProblems.put("Pagie1", new Pagie1());
    innerProblems.put("KLandscapes-5", new KLandscapes(5));
    innerProblems.put("Text", new Text());
    Problem<String, MultiObjectiveFitness> problem = new MapperGeneration(genotypeSize, nGenotypes, innerMaxMappingDepth, random, new ArrayList<>(innerProblems.values()));
    //print baselines
    Map<String, Node<String>> baselines = new LinkedHashMap<>();
    baselines.put("GE", getGERawTree());
    baselines.put("HGE", getHGERawTree());
    baselines.put("WGE", getWHGERawTree());
    //baselines.clear(); //TODO remove to show baselines performance
    boolean innerHeader = true;
    boolean mainHeader = true;
    for (String baselineName : baselines.keySet()) {
      MultiObjectiveFitness moF = problem.getLearningFitnessComputer().compute(baselines.get(baselineName));
      System.out.printf("%5.5s fitness:\t%4.2f %4.2f %4.2f%n",
              baselineName, moF.getValue()[0], moF.getValue()[1], moF.getValue()[2]
      );
      for (String innerProblemName : innerProblems.keySet()) {
        for (int r = 0; r < innerRuns; r++) {
          NumericFitness numF = applyMapperOnProblem(
                  baselines.get(baselineName),
                  innerProblems.get(innerProblemName),
                  innerPopSize, genotypeSize, innerGenerations, innerMaxMappingDepth, expressivenessDepth,
                  baselineName, r, innerProblemName,
                  innerHeader, innerFilePrintStream
          );
          System.out.printf("\t%10.10s %2d bf=%6.3f%n", innerProblemName, r, numF.getValue());
          innerHeader = false;
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
                      .put(new RandomInitializer<>(innerRandom, new GrowTreeFactory<>(maxDepth, problem.getGrammar())), 0.5)
                      .put(new RandomInitializer<>(innerRandom, new FullTreeFactory<>(maxDepth, problem.getGrammar())), 0.5)
                      .build()
              ),
              new Any<Node<String>>(),
              new CfgGpMapper<String>(),
              new Utils.MapBuilder<GeneticOperator<Node<String>>, Double>()
              .put(new StandardTreeCrossover<String>(maxDepth, innerRandom), 0.8d)
              .put(new StandardTreeMutation<>(maxDepth, problem.getGrammar(), innerRandom), 0.2d)
              .build(),
              new ParetoRanker<Node<String>, String, MultiObjectiveFitness>(),
              new Tournament<Individual<Node<String>, String, MultiObjectiveFitness>>(3, innerRandom),
              new LastWorst<Individual<Node<String>, String, MultiObjectiveFitness>>(),
              popSize,
              true,
              problem);
      List<EvolverListener<Node<String>, String, MultiObjectiveFitness>> listeners = new ArrayList<>();
      listeners.add(new CollectorGenerationLogger<>(
              (Map) Collections.singletonMap("run", s), System.out, true, 10, " ", " | ",
              new Population<Node<String>, String, MultiObjectiveFitness>(),
              new MultiObjectiveFitnessFirstBest<Node<String>, String>(false, problem.getTestingFitnessComputer(), "%4.2f", "%4.2f", "%4.2f"),
              new Diversity<Node<String>, String, MultiObjectiveFitness>(),
              new BestPrinter<Node<String>, String, MultiObjectiveFitness>(problem.getPhenotypePrinter(), "%30.30s")
      ));
      listeners.add(new CollectorGenerationLogger<>(
              (Map) Collections.singletonMap("run", s), mainFilePrintStream, false, mainHeader ? 0 : -1, ";", ";",
              new Population<Node<String>, String, MultiObjectiveFitness>(),
              new MultiObjectiveFitnessFirstBest<Node<String>, String>(false, problem.getTestingFitnessComputer(), "%4.2f", "%4.2f", "%4.2f"),
              new Diversity<Node<String>, String, MultiObjectiveFitness>(),
              new BestPrinter<Node<String>, String, MultiObjectiveFitness>(problem.getPhenotypePrinter(), "%30.30s")
      ));
      mainHeader = false;
      Evolver<Node<String>, String, MultiObjectiveFitness> evolver = new PartitionEvolver<>(
              //configuration, 1, random, false);
              configuration, Runtime.getRuntime().availableProcessors() - 1, innerRandom, false);
      List<Node<String>> bests = evolver.solve(listeners);
      System.out.printf("Found %d solutions.%n", bests.size());
      String mapperName = "best-" + s;
      for (String innerProblemName : innerProblems.keySet()) {
        for (int r = 0; r < innerRuns; r++) {
          NumericFitness numF = applyMapperOnProblem(
                  bests.get(0),
                  innerProblems.get(innerProblemName),
                  innerPopSize, genotypeSize, innerGenerations, innerMaxMappingDepth, expressivenessDepth,
                  mapperName, r, innerProblemName,
                  innerHeader, innerFilePrintStream
          );
          System.out.printf("\t%10.10s %2d bf=%6.3f%n", innerProblemName, r, numF.getValue());
          innerHeader = false;
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

  private static NumericFitness applyMapperOnProblem(
          Node<String> rawMapper,
          Problem<String, NumericFitness> problem,
          int popSize, int genotypeSize, int generations, int maxMappingDepth, int expressivenessDepth,
          String mapperName, int run, String innerProblemName,
          boolean header, PrintStream ps) throws InterruptedException, ExecutionException {
    Random random = new Random(run);
    StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
            popSize,
            generations,
            new RandomInitializer<>(random, new BitsGenotypeFactory(genotypeSize)),
            new Any<BitsGenotype>(),
            new RecursiveMapper<>(rawMapper, maxMappingDepth, expressivenessDepth, problem.getGrammar()),
            new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
            .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
            .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
            new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3, random),
            new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(),
            popSize,
            true,
            problem);
    List<EvolverListener<BitsGenotype, String, NumericFitness>> listeners = new ArrayList<>();
    listeners.add(new CollectorGenerationLogger<>(
            new Utils.MapBuilder<String, Object>()
            .put("mapper", mapperName)
            .put("problem", innerProblemName)
            .put("run", run).build(),
            ps, false, header ? 0 : -1, ";", ";",
            new Population<BitsGenotype, String, NumericFitness>(),
            new NumericFirstBest<BitsGenotype, String>(false, problem.getTestingFitnessComputer(), "%6.2f"),
            new Diversity<BitsGenotype, String, NumericFitness>())
    );
    Evolver<BitsGenotype, String, NumericFitness> evolver = new StandardEvolver<>(configuration, Runtime.getRuntime().availableProcessors() - 1, random, false);
    List<Node<String>> bests = evolver.solve(listeners);
    return problem.getLearningFitnessComputer().compute(bests.get(0));
  }

}
