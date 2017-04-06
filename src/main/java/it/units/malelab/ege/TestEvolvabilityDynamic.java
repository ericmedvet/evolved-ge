/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.problem.BenchmarkProblems;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.evolver.StandardConfiguration;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.ge.evolver.StandardEvolver;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.ge.genotype.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.listener.EvolvabilityFitnessLogger;
import it.units.malelab.ege.ge.operator.LengthChanger;
import it.units.malelab.ege.ge.operator.LengthPreservingOnePointCrossover;
import it.units.malelab.ege.ge.operator.LocalizedTwoPointsCrossover;
import it.units.malelab.ege.ge.operator.OnePointCrossover;
import it.units.malelab.ege.ge.operator.ProbabilisticMutation;
import it.units.malelab.ege.core.selector.IndividualComparator;
import it.units.malelab.ege.evolver.selector.Tournament;
import it.units.malelab.ege.core.grammar.Grammar;
import it.units.malelab.ege.ge.mapper.BreathFirstMapper;
import it.units.malelab.ege.ge.mapper.HierarchicalMapper;
import it.units.malelab.ege.ge.mapper.MappingException;
import it.units.malelab.ege.ge.mapper.PiGEMapper;
import it.units.malelab.ege.ge.mapper.StandardGEMapper;
import it.units.malelab.ege.ge.mapper.DHierarchicalMapper;
import it.units.malelab.ege.ge.mapper.MultiMapper;
import it.units.malelab.ege.ge.mapper.WeightedHierarchicalMapper;
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
public class TestEvolvabilityDynamic {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, MappingException {
    //prepare file
    PrintStream generationFilePS = new PrintStream("EvolvabilityDynamic_" + dateForFile() + ".csv");
    //prepare problems
    generationFilePS.println("Problem; Mapper; GenoSize; Run; Generation; FitnessChild; FitnessP1; FitnessP2");
    Map<String, BenchmarkProblems.Problem> problems = new LinkedHashMap<>();
    problems.put("harmonic", BenchmarkProblems.harmonicCurveProblem());
    problems.put("poly4", BenchmarkProblems.classic4PolynomialProblem());
    //problems.put("max", BenchmarkProblems.max());
    problems.put("santaFe", BenchmarkProblems.santaFe());
    problems.put("text", BenchmarkProblems.text("Hello world!"));
    boolean writeHeader = true;
    for (int initGenoSize : new int[]{512}) {
      for (String problemName : problems.keySet()) {
        BenchmarkProblems.Problem problem = problems.get(problemName);
        for (int r = 0; r < 30; r++) {
          Random random = new Random(r);
          Map<String, Object> constants = new LinkedHashMap<>();
          constants.put("Problem", problemName);
          constants.put("Run", r);
          //constants.put("Strategy", "steady-state");
          constants.put("initGenoSize", initGenoSize);
          for (int m : new int[]{3}) {
            StandardConfiguration<BitsGenotype, String> configuration = StandardConfiguration.createDefault(problem, random);
            configuration.getOperators().clear();
            configuration
                    .parentSelector(new Tournament(3, random, new IndividualComparator(IndividualComparator.Attribute.FITNESS)))
                    .populationInitializer(new RandomInitializer<>(random, new BitsGenotypeFactory(initGenoSize)))
                    .operator(new OnePointCrossover(random), 0.8d)
                    .operator(new ProbabilisticMutation(random, 0.01), 0.2d);
            Grammar<String> grammar = problems.get(problemName).getGrammar();
            switch (m) {
              case 0:
                configuration.mapper(new StandardGEMapper<>(8, 5, grammar));
                constants.put("Mapper", "StdGE-8-5");
                break;
              case 1:
                configuration.mapper(new PiGEMapper<>(16, 5, grammar));
                constants.put("Mapper", "piGE-16-5");
                break;
              case 2:
                configuration.mapper(new BreathFirstMapper<>(8, 5, grammar));
                constants.put("Mapper", "Breath-8-5");
                break;
              case 3:
                configuration.mapper(new DHierarchicalMapper<>(grammar));
                constants.put("Mapper", "DGE");
                break;
            }
            Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(Runtime.getRuntime().availableProcessors() - 1, configuration, random, true);
            //Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(1, configuration, random, false);
            List<EvolutionListener<BitsGenotype, String>> listeners = new ArrayList<>();
            //listeners.add(new ScreenGenerationLogger<BitsGenotype, String>("%8.1f", 8, problem.getPhenotypePrinter(), problem.getGeneralizationFitnessComputer(), constants));
            listeners.add(new EvolvabilityFitnessLogger<BitsGenotype, String>(generationFilePS, problem.getGeneralizationFitnessComputer(), constants, writeHeader));
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
