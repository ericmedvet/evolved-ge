/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.StandardConfiguration;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.StandardEvolver;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.listener.ScreenGenerationLogger;
import it.units.malelab.ege.evolver.listener.StreamGenerationLogger;
import it.units.malelab.ege.evolver.operator.LengthChanger;
import it.units.malelab.ege.evolver.operator.LengthPreservingOnePointCrossover;
import it.units.malelab.ege.evolver.operator.LocalizedTwoPointsCrossover;
import it.units.malelab.ege.evolver.operator.ProbabilisticMutation;
import it.units.malelab.ege.evolver.selector.IndividualComparator;
import it.units.malelab.ege.evolver.selector.Tournament;
import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.mapper.HierarchicalMapper;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.PiGEMapper;
import it.units.malelab.ege.mapper.StandardGEMapper;
import it.units.malelab.ege.mapper.DHierarchicalMapper;
import it.units.malelab.ege.mapper.MultiMapper;
import it.units.malelab.ege.mapper.WeightedHierarchicalMapper;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class TestComparison1 {
  
  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, MappingException {
    //prepare file
    PrintStream generationFilePS = new PrintStream("comparison"+dateForFile()+".csv");
    //prepare problems
    Map<String, BenchmarkProblems.Problem> problems = new LinkedHashMap<>();
    problems.put("harmonic", BenchmarkProblems.harmonicCurveProblem());
    problems.put("poly4", BenchmarkProblems.classic4PolynomialProblem());
    //problems.put("max", BenchmarkProblems.max());
    problems.put("santaFe", BenchmarkProblems.santaFe());
    problems.put("text", BenchmarkProblems.text("Hello world!"));
    boolean writeHeader = true;
    for (int initGenoSize : new int[]{1024}) {
      for (String problemName : problems.keySet()) {
        BenchmarkProblems.Problem problem = problems.get(problemName);
        for (int r = 0; r < 30; r++) {
          Random random = new Random(r);
          Map<String, Object> constants = new LinkedHashMap<>();
          constants.put("problem", problemName);
          constants.put("run", r);
          constants.put("strategy", "steady-state");
          constants.put("initGenotypeSize", initGenoSize);
          for (int m : new int[]{6}) {
            StandardConfiguration<BitsGenotype, String> configuration = StandardConfiguration.createDefault(problem, random);
            configuration.getOperators().clear();
            configuration
                    .parentSelector(new Tournament(3, random, new IndividualComparator(IndividualComparator.Attribute.FITNESS)))
                    .populationInitializer(new RandomInitializer<>(random, new BitsGenotypeFactory(initGenoSize)))
                    .operator(new LengthPreservingOnePointCrossover(random), 0.8d)
                    .operator(new ProbabilisticMutation(random, 0.01), 0.2d);
            Grammar<String> grammar = problems.get(problemName).getGrammar();
            switch (m) {
              case 0:
                configuration
                        .mapper(new StandardGEMapper<>(8, 5, grammar));
                constants.put("variant", "GE-8-5");
                break;
              case 1:
                configuration.mapper(new PiGEMapper<>(16, 5, grammar));
                constants.put("variant", "piGE-16-5");
                break;
              case 3:
                configuration.getOperators().clear();
                configuration
                        .mapper(new HierarchicalMapper<>(grammar))
                        .operator(new LocalizedTwoPointsCrossover(random), 0.8d)
                        .operator(new ProbabilisticMutation(random, 0.01), 0.2d);
                constants.put("variant", "HiGE");
                break;
              case 4:
                configuration.getOperators().clear();
                configuration
                        .mapper(new WeightedHierarchicalMapper<>(6, grammar))
                        .operator(new LocalizedTwoPointsCrossover(random), 0.8d)
                        .operator(new ProbabilisticMutation(random, 0.01), 0.2d);
                constants.put("variant", "WHiGE-6");
                break;
              case 5:
                configuration.mapper(new MultiMapper<>(
                        new StandardGEMapper<>(8, 5, grammar),
                        new PiGEMapper<>(16, 5, grammar),
                        new HierarchicalMapper<>(grammar),
                        new WeightedHierarchicalMapper<>(6, grammar)
                ));
                constants.put("variant", "MuMapper-4");
                break;
            }
            Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(Runtime.getRuntime().availableProcessors() - 1, configuration, random, false);
            //Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(1, configuration, random, false);
            List<EvolutionListener<BitsGenotype, String>> listeners = new ArrayList<>();
            listeners.add(new ScreenGenerationLogger<BitsGenotype, String>("%8.1f", 8, problem.getPhenotypePrinter(), problem.getGeneralizationFitnessComputer(), constants));
            listeners.add(new StreamGenerationLogger<BitsGenotype, String>(generationFilePS, problem.getGeneralizationFitnessComputer(), constants, writeHeader));
            writeHeader = false;
            System.out.println(constants);
            evolver.go(listeners);
            System.out.println();
          }
        }
      }
    }
    generationFilePS.close();
  }

  private static String dateForFile() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
    return simpleDateFormat.format(new Date());
  }

}
