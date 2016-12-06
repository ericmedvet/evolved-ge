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
import it.units.malelab.ege.evolver.initializer.QuantizedBitsInitializer;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.listener.ScreenGenerationLogger;
import it.units.malelab.ege.evolver.listener.StreamGenerationLogger;
import it.units.malelab.ege.evolver.operator.LengthChanger;
import it.units.malelab.ege.evolver.operator.LocalizedTwoPointsCrossover;
import it.units.malelab.ege.evolver.operator.ProbabilisticMutation;
import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.mapper.HierarchicalMapper;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.PiGEMapper;
import it.units.malelab.ege.mapper.StandardGEMapper;
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
public class MainComparison {
  
  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, MappingException {
    //prepare file
    PrintStream generationFilePS = new PrintStream(args[0].replace("DATE", dateForFile()));
    //prepare problems
    Map<String, BenchmarkProblems.Problem> problems = new LinkedHashMap<>();
    problems.put("harmonic", BenchmarkProblems.harmonicCurveProblem());
    problems.put("poly4", BenchmarkProblems.classic4PolynomialProblem());
    problems.put("max", BenchmarkProblems.max());
    problems.put("santaFe", BenchmarkProblems.santaFe());
    problems.put("text", BenchmarkProblems.text("Hello world!"));
    boolean writeHeader = true;
    for (String problemName : problems.keySet()) {
      BenchmarkProblems.Problem problem = problems.get(problemName);
      for (int r = 0; r < 30; r++) {
        Random random = new Random(r);
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("problem", problemName);
        constants.put("run", r);
        //for (int m = 0; m < 10; m++) {
        for (int m : new int[]{10,11}) {
          StandardConfiguration<BitsGenotype, String> configuration = StandardConfiguration.createDefault(problem, random);
          Grammar<String> grammar = problems.get(problemName).getGrammar();
          switch (m) {
            case 0:
              configuration.mapper(new StandardGEMapper<>(8, 5, grammar));
              constants.put("variant", "GE-8-5");
              constants.put("strategy", "steady-state");
              break;
            case 1:
              configuration
                      .mapper(new StandardGEMapper<>(8, 5, grammar))
                      .offspringSize(400);
              constants.put("variant", "GE-8-5");
              constants.put("strategy", "over-0.8");
              break;
            case 2:
              configuration.mapper(new PiGEMapper<>(16, 5, grammar));
              constants.put("variant", "piGE-16-5");
              constants.put("strategy", "steady-state");
              break;
            case 3:
              configuration
                      .mapper(new PiGEMapper<>(16, 5, grammar))
                      .offspringSize(400);
              constants.put("variant", "piGE-16-5");
              constants.put("strategy", "over-0.8");
              break;
            case 4:
              configuration.mapper(new HierarchicalMapper<>(grammar));
              constants.put("variant", "HiGE");
              constants.put("strategy", "steady-state");
              break;
            case 5:
              configuration
                      .mapper(new HierarchicalMapper<>(grammar))
                      .offspringSize(400);
              constants.put("variant", "HiGE");
              constants.put("strategy", "over-0.8");
              break;
            case 6:
              configuration.getOperators().clear();
              configuration
                      .mapper(new HierarchicalMapper<>(grammar))
                      .offspringSize(400)
                      .operator(new LocalizedTwoPointsCrossover(random), 0.8d)
                      .operator(new ProbabilisticMutation(random, 0.01), 0.1d)
                      .operator(new LengthChanger(random, 0.1), 0.1d);
              constants.put("variant", "HiGE-ope");
              constants.put("strategy", "over-0.8");
              break;
            case 7:
              configuration.mapper(new WeightedHierarchicalMapper<>(6, grammar));
              constants.put("variant", "WHiGE-6");
              constants.put("strategy", "steady-state");
              break;
            case 8:
              configuration
                      .mapper(new WeightedHierarchicalMapper<>(6, grammar))
                      .offspringSize(400);
              constants.put("variant", "WHiGE-6");
              constants.put("strategy", "over-0.8");
              break;
            case 9:
              configuration
                      .mapper(new WeightedHierarchicalMapper<>(6, grammar))
                      .offspringSize(400)
                      .operator(new LocalizedTwoPointsCrossover(random), 0.8d)
                      .operator(new ProbabilisticMutation(random, 0.01), 0.1d)
                      .operator(new LengthChanger(random, 0.1), 0.1d);
              constants.put("variant", "WHiGE-6-ope");
              constants.put("strategy", "over-0.8");
              break;
            case 10:
              configuration
                      .mapper(new HierarchicalMapper<>(grammar))
                      .offspringSize(1)
                      .operator(new LocalizedTwoPointsCrossover(random), 0.8d)
                      .operator(new ProbabilisticMutation(random, 0.01), 0.1d)
                      .operator(new LengthChanger(random, 0.1), 0.1d)
                      .populationInitializer(new QuantizedBitsInitializer(1024));
              constants.put("variant", "HiGE-ope-qi");
              constants.put("strategy", "steady-state");
              break;
            case 11:
              configuration
                      .mapper(new WeightedHierarchicalMapper<>(6, grammar))
                      .offspringSize(1)
                      .operator(new LocalizedTwoPointsCrossover(random), 0.8d)
                      .operator(new ProbabilisticMutation(random, 0.01), 0.1d)
                      .operator(new LengthChanger(random, 0.1), 0.1d)
                      .populationInitializer(new QuantizedBitsInitializer(1024));
              constants.put("variant", "WHiGE-6-ope-qi");
              constants.put("strategy", "steady-state");
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
    generationFilePS.close();
  }

  private static String dateForFile() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
    return simpleDateFormat.format(new Date());
  }

}
