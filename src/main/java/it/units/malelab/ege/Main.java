/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.distance.BitsGenotypeEditDistance;
import it.units.malelab.ege.distance.CachedDistance;
import it.units.malelab.ege.distance.Distance;
import it.units.malelab.ege.distance.EditDistance;
import it.units.malelab.ege.distance.SGEGenotypeHammingDistance;
import it.units.malelab.ege.distance.TreeEditDistance;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.validator.AnyValidator;
import it.units.malelab.ege.evolver.Configuration;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.StandardEvolver;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.genotype.SGEGenotype;
import it.units.malelab.ege.evolver.genotype.SGEGenotypeFactory;
import it.units.malelab.ege.evolver.listener.DynamicLocalityAnalysisLogger;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.listener.ScreenGenerationLogger;
import it.units.malelab.ege.evolver.listener.StreamGenerationLogger;
import it.units.malelab.ege.evolver.operator.BitsSGECrossover;
import it.units.malelab.ege.evolver.operator.LengthChanger;
import it.units.malelab.ege.evolver.operator.LengthPreservingOnePointCrossover;
import it.units.malelab.ege.evolver.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.evolver.operator.LocalizedTwoPointsCrossover;
import it.units.malelab.ege.evolver.operator.OnePointCrossover;
import it.units.malelab.ege.evolver.selector.TournamentSelector;
import it.units.malelab.ege.evolver.operator.ProbabilisticMutation;
import it.units.malelab.ege.evolver.operator.SGECrossover;
import it.units.malelab.ege.evolver.operator.SGEMutation;
import it.units.malelab.ege.evolver.selector.BestSelector;
import it.units.malelab.ege.evolver.selector.IndividualComparator;
import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.mapper.BitsSGEMapper;
import it.units.malelab.ege.mapper.BreathFirstMapper;
import it.units.malelab.ege.mapper.HierarchicalMapper;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.PiGEMapper;
import it.units.malelab.ege.mapper.SGEMapper;
import it.units.malelab.ege.mapper.StandardGEMapper;
import it.units.malelab.ege.mapper.WeightedHierarchicalMapper;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
public class Main {
  
  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, MappingException {
    mainComparison(args);
  }

  public static void mainBits(String[] args) throws IOException, ExecutionException, InterruptedException, MappingException {
    //define distances
    Map<String, Distance<BitsGenotype>> genotypeDistances = new LinkedHashMap<>();
    genotypeDistances.put("BitsEdit", new CachedDistance<>(new BitsGenotypeEditDistance()));
    Map<String, Distance<Node<String>>> phenotypeDistances = new LinkedHashMap<>();
    final EditDistance<String> editDistance = new EditDistance<>();
    phenotypeDistances.put("LeavesEdit", new CachedDistance<>(new Distance<Node<String>>() {
      @Override
      public double d(Node<String> t1, Node<String> t2) {
        List<String> s1 = Node.EMPTY_TREE.equals(t1) ? Collections.EMPTY_LIST : Utils.contents(t1.leaves());
        List<String> s2 = Node.EMPTY_TREE.equals(t2) ? Collections.EMPTY_LIST : Utils.contents(t2.leaves());
        return editDistance.d(s1, s2);
      }
    }));
    phenotypeDistances.put("TreeEdit", new CachedDistance<>(new TreeEditDistance<String>()));
    //prepare file
    PrintStream generationFilePS = new PrintStream(args[0].replace("DATE", dateForFile()));
    PrintStream distancesFilePS = new PrintStream(args[1].replace("DATE", dateForFile()));
    //prepare problems
    Map<String, BenchmarkProblems.Problem> problems = new LinkedHashMap<>();
    problems.put("harmonic", BenchmarkProblems.harmonicCurveProblem());
    problems.put("poly4", BenchmarkProblems.classic4PolynomialProblem());
    //problems.put("max", BenchmarkProblems.max());
    problems.put("santaFe", BenchmarkProblems.santaFe());
    problems.put("text", BenchmarkProblems.text("Hello world!"));
    boolean writeHeader = true;
    for (String problemName : problems.keySet()) {
      BenchmarkProblems.Problem problem = problems.get(problemName);
      for (int r = 0; r < 30; r++) {
        Random random = new Random(r);
        for (int m = 0; m < 4; m++) {
          for (int genotypeSize : Arrays.asList(1024)) {
            Configuration<BitsGenotype, String> configuration = defaultConfiguration(problem, random);
            Grammar<String> grammar = problems.get(problemName).getGrammar();
            switch (m) {
              case 0:
                configuration.mapper(new StandardGEMapper<>(8, 5, grammar));
                break;
              case 1:
                configuration.mapper(new BreathFirstMapper<>(8, 5, grammar));
                break;
              case 2:
                configuration.mapper(new PiGEMapper<>(16, 5, grammar));
                break;
              case 3:
                BitsSGEMapper<String> sgeMapper = new BitsSGEMapper<>(6, grammar);
                configuration
                        .mapper(sgeMapper)
                        .operator(new BitsSGECrossover(sgeMapper, random), 0.8d)
                        .operator(new ProbabilisticMutation(random, 0.01), 0.2d);
                break;
              case 4:
                configuration.mapper(new HierarchicalMapper<>(grammar));
                break;
              case 5:
                configuration.mapper(new WeightedHierarchicalMapper<>(6, grammar));
                break;
            }
            configuration.populationInitializer(new RandomInitializer<>(random, new BitsGenotypeFactory(genotypeSize)));
            Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(Runtime.getRuntime().availableProcessors() - 1, configuration, random);
            Map<String, Object> constants = new LinkedHashMap<>();
            constants.put("problem", problemName);
            constants.put("mapper", configuration.getMapper().getClass().getSimpleName());
            constants.put("initGenoSize", genotypeSize);
            constants.put("run", r);
            List<EvolutionListener<BitsGenotype, String>> listeners = new ArrayList<>();
            listeners.add(new ScreenGenerationLogger<BitsGenotype, String>("%8.1f", 8, problem.getPhenotypePrinter(), problem.getGeneralizationFitnessComputer(), constants));
            listeners.add(new StreamGenerationLogger<BitsGenotype, String>(generationFilePS, null, constants, writeHeader));
            listeners.add(new DynamicLocalityAnalysisLogger<>(distancesFilePS, genotypeDistances, phenotypeDistances, constants, writeHeader));
            writeHeader = false;
            System.out.println(constants);
            evolver.go(listeners);
            System.out.println();
          }
        }
      }
    }
    generationFilePS.close();
    phenotypeDistances.clear();
  }

  public static void mainWithDistances(String[] args) throws IOException, ExecutionException, InterruptedException, MappingException {
    //define distances
    Map<String, Distance<SGEGenotype<String>>> genotypeDistances = new LinkedHashMap<>();
    genotypeDistances.put("BitsEdit", new CachedDistance<>(new SGEGenotypeHammingDistance<String>()));
    Map<String, Distance<Node<String>>> phenotypeDistances = new LinkedHashMap<>();
    final EditDistance<String> editDistance = new EditDistance<>();
    phenotypeDistances.put("LeavesEdit", new CachedDistance<>(new Distance<Node<String>>() {
      @Override
      public double d(Node<String> t1, Node<String> t2) {
        List<String> s1 = Node.EMPTY_TREE.equals(t1) ? Collections.EMPTY_LIST : Utils.contents(t1.leaves());
        List<String> s2 = Node.EMPTY_TREE.equals(t2) ? Collections.EMPTY_LIST : Utils.contents(t2.leaves());
        return editDistance.d(s1, s2);
      }
    }));
    phenotypeDistances.put("TreeEdit", new CachedDistance<>(new TreeEditDistance<String>()));
    //prepare file
    PrintStream generationFilePS = new PrintStream(args[0].replace("DATE", dateForFile()));
    PrintStream distancesFilePS = new PrintStream(args[1].replace("DATE", dateForFile()));
    //prepare problems
    Map<String, BenchmarkProblems.Problem> problems = new LinkedHashMap<>();
    problems.put("harmonic", BenchmarkProblems.harmonicCurveProblem());
    problems.put("poly4", BenchmarkProblems.classic4PolynomialProblem());
    //problems.put("max", BenchmarkProblems.max());
    problems.put("santaFe", BenchmarkProblems.santaFe());
    problems.put("text", BenchmarkProblems.text("Hello world!"));
    boolean writeHeader = true;
    for (String problemName : problems.keySet()) {
      BenchmarkProblems.Problem problem = problems.get(problemName);
      for (int d : new int[]{6, 10}) {
        for (int r = 0; r < 30; r++) {
          Random random = new Random(r);
          SGEMapper<String> mapper = new SGEMapper<>(d, problem.getGrammar());
          Configuration<SGEGenotype<String>, String> configuration = new Configuration<>();
          configuration
                  .populationSize(500)
                  .numberOfGenerations(50)
                  .mapper(mapper)
                  .populationInitializer(new RandomInitializer<>(random, new SGEGenotypeFactory<>(mapper)))
                  .initGenotypeValidator(new AnyValidator<SGEGenotype<String>>())
                  .operator(new SGECrossover<String>(random), 0.8d)
                  .operator(new SGEMutation<>(0.02d, mapper, random), 0.8d)
                  .fitnessComputer(problem.getFitnessComputer());
          Evolver<SGEGenotype<String>, String> evolver = new StandardEvolver<>(Runtime.getRuntime().availableProcessors() - 1, configuration, random);
          Map<String, Object> constants = new LinkedHashMap<>();
          constants.put("problem", problemName);
          constants.put("mapper", configuration.getMapper().getClass().getSimpleName()+"-"+d);
          constants.put("initGenoSize", 0);
          constants.put("run", r);
          List<EvolutionListener<SGEGenotype<String>, String>> listeners = new ArrayList<>();
          listeners.add(new ScreenGenerationLogger<SGEGenotype<String>, String>("%8.1f", 8, problem.getPhenotypePrinter(), problem.getGeneralizationFitnessComputer(), constants));
          //listeners.add(new StreamGenerationLogger<SGEGenotype<String>, String>(generationFilePS, null, constants, writeHeader));
          //listeners.add(new DynamicLocalityAnalysisLogger<>(distancesFilePS, genotypeDistances, phenotypeDistances, constants, writeHeader));
          writeHeader = false;
          System.out.println(constants);
          evolver.go(listeners);
          System.out.println();
        }
      }
    }
    generationFilePS.close();
    phenotypeDistances.clear();
  }

  public static void mainComparison(String[] args) throws IOException, ExecutionException, InterruptedException, MappingException {
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
        for (int m = 0; m < 10; m++) {
          Configuration<BitsGenotype, String> configuration = defaultConfiguration(problem, random);
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
          }
          Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(Runtime.getRuntime().availableProcessors() - 1, configuration, random);
          //Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(1, configuration, random);
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
    //generationFilePS.close();
  }

  private static String dateForFile() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
    return simpleDateFormat.format(new Date());
  }

  private static Configuration<BitsGenotype, String> defaultConfiguration(BenchmarkProblems.Problem problem, Random random) {
    Configuration<BitsGenotype, String> configuration = new Configuration<>();
    configuration
            .populationSize(500)
            .offspringSize(1)
            .overlapping(true)
            .numberOfGenerations(50)
            .populationInitializer(new RandomInitializer<>(random, new BitsGenotypeFactory(1024)))
            .initGenotypeValidator(new AnyValidator<BitsGenotype>())
            .mapper(new StandardGEMapper<>(8, 5, problem.getGrammar()))
            .parentSelector(new TournamentSelector(5, random, new IndividualComparator(0)))
            .survivalSelector(new BestSelector(new IndividualComparator(0)))
            .operator(new LengthPreservingTwoPointsCrossover(random), 0.8d)
            .operator(new ProbabilisticMutation(random, 0.01), 0.2d)
            .fitnessComputer(problem.getFitnessComputer());
    return configuration;
  }

}
