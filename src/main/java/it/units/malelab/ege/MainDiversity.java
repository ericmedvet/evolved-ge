/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.StandardConfiguration;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.PartitionConfiguration;
import it.units.malelab.ege.evolver.PartitionEvolver;
import it.units.malelab.ege.evolver.StandardEvolver;
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
public class MainDiversity {

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
      for (int r = 0; r < 1; r++) {
        Random random = new Random(r);
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("problem", problemName);
        constants.put("run", r);
        for (int m = 0; m < 2; m++) {
          Evolver<BitsGenotype, String> evolver = null;
          switch (m) {
            case 0:
              evolver = new PartitionEvolver<>(Runtime.getRuntime().availableProcessors() - 1,
                      (PartitionConfiguration) PartitionConfiguration.createDefault(problem, random)
                      .numberOfGenerations(15)
                      .mapper(new StandardGEMapper<>(8, 5, problems.get(problemName).getGrammar()))
                      .offspringSize(400), random, false);
              constants.put("variant", "GE-8-5");
              constants.put("strategy", "over-0.8");
              constants.put("diversity", "on");
              break;
            case 1:
              evolver = new StandardEvolver<>(Runtime.getRuntime().availableProcessors() - 1,
                      StandardConfiguration.createDefault(problem, random)
                      .mapper(new StandardGEMapper<>(8, 5, problems.get(problemName).getGrammar()))
                      .numberOfGenerations(15)
                      .offspringSize(400), random, false);
              constants.put("variant", "GE-8-5");
              constants.put("strategy", "over-0.8");
              constants.put("diversity", "off");
              break;
          }
          List<EvolutionListener<BitsGenotype, String>> listeners = new ArrayList<>();
          listeners.add(new ScreenGenerationLogger<BitsGenotype, String>("%8.1f", 8, problem.getPhenotypePrinter(), problem.getGeneralizationFitnessComputer(), constants));
          listeners.add(new StreamGenerationLogger<BitsGenotype, String>(generationFilePS, null, constants, writeHeader));
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
