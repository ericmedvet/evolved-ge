/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.problem.BenchmarkProblems;
import it.units.malelab.ege.core.grammar.Node;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.util.distance.BitsGenotypeEditDistance;
import it.units.malelab.ege.util.distance.CachedDistance;
import it.units.malelab.ege.util.distance.Distance;
import it.units.malelab.ege.util.distance.EditDistance;
import it.units.malelab.ege.util.distance.SGEGenotypeHammingDistance;
import it.units.malelab.ege.util.distance.TreeEditDistance;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.validator.AnyValidator;
import it.units.malelab.ege.ge.evolver.StandardConfiguration;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.ge.genotype.initializer.RandomInitializer;
import it.units.malelab.ege.ge.evolver.StandardEvolver;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.ge.genotype.SGEGenotype;
import it.units.malelab.ege.ge.genotype.SGEGenotypeFactory;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.evolver.listener.DynamicLocalityAnalysisLogger;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.listener.collector.Best;
import it.units.malelab.ege.evolver.listener.collector.Diversity;
import it.units.malelab.ege.ge.operator.BitsSGECrossover;
import it.units.malelab.ege.ge.operator.ProbabilisticMutation;
import it.units.malelab.ege.ge.operator.SGECrossover;
import it.units.malelab.ege.ge.operator.SGEMutation;
import it.units.malelab.ege.core.grammar.Grammar;
import it.units.malelab.ege.ge.mapper.BitsSGEMapper;
import it.units.malelab.ege.ge.mapper.BreathFirstMapper;
import it.units.malelab.ege.ge.mapper.HierarchicalMapper;
import it.units.malelab.ege.ge.mapper.MappingException;
import it.units.malelab.ege.ge.mapper.PiGEMapper;
import it.units.malelab.ege.ge.mapper.SGEMapper;
import it.units.malelab.ege.ge.mapper.StandardGEMapper;
import it.units.malelab.ege.ge.mapper.WeightedHierarchicalMapper;
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
public class MainDistances {
  
  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, MappingException {
    mainBits(args);
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
    List<EvolutionListener<BitsGenotype, String>> listeners = new ArrayList<>();
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP,
            System.out, true, 10, " ", " | ",
            new Best<>("%5.2f"),
            new Diversity<>()
    ));
    listeners.add(new CollectorGenerationLogger<>(
            (Map)Utils.sameValueMap(null, "problem", "run", "initGenotypeSize", "mapper"),
            generationFilePS, false, 0, ";", ";",
            new Best<>("%5.2f"),
            new Diversity<>()
    ));
    boolean writeHeader = true;
    for (String problemName : problems.keySet()) {
      BenchmarkProblems.Problem problem = problems.get(problemName);
      for (int r = 0; r < 30; r++) {
        Random random = new Random(r);
        for (int m = 0; m < 4; m++) {
          for (int genotypeSize : Arrays.asList(1024)) {
            StandardConfiguration<BitsGenotype, String> configuration = StandardConfiguration.createDefault(problem, random);
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
            Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(Runtime.getRuntime().availableProcessors() - 1, configuration, random, false);
            Map<String, Object> constants = new LinkedHashMap<>();
            constants.put("problem", problemName);
            constants.put("mapper", configuration.getMapper().getClass().getSimpleName());
            constants.put("initGenoSize", genotypeSize);
            constants.put("run", r);
            List<EvolutionListener<BitsGenotype, String>> localListeners = new ArrayList<>(listeners);
            localListeners.add(new DynamicLocalityAnalysisLogger<>(distancesFilePS, genotypeDistances, phenotypeDistances, constants, writeHeader));
            writeHeader = false;
            System.out.println(constants);
            for (EvolutionListener listener : localListeners) {
              if (listener instanceof CollectorGenerationLogger) {
                ((CollectorGenerationLogger)listener).updateConstants(constants);
              }
            }
            evolver.go(localListeners);
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
          StandardConfiguration<SGEGenotype<String>, String> configuration = new StandardConfiguration<>();
          configuration
                  .populationSize(500)
                  .numberOfGenerations(50)
                  .mapper(mapper)
                  .populationInitializer(new RandomInitializer<>(random, new SGEGenotypeFactory<>(mapper)))
                  .initGenotypeValidator(new AnyValidator<SGEGenotype<String>>())
                  .operator(new SGECrossover<String>(random), 0.8d)
                  .operator(new SGEMutation<>(0.02d, mapper, random), 0.8d)
                  .fitnessComputer(problem.getFitnessComputer());
          Evolver<SGEGenotype<String>, String> evolver = new StandardEvolver<>(Runtime.getRuntime().availableProcessors() - 1, configuration, random, false);
          Map<String, Object> constants = new LinkedHashMap<>();
          constants.put("problem", problemName);
          constants.put("mapper", configuration.getMapper().getClass().getSimpleName()+"-"+d);
          constants.put("initGenoSize", 0);
          constants.put("run", r);
          List<EvolutionListener<SGEGenotype<String>, String>> listeners = new ArrayList<>();
          //listeners.add(new ScreenGenerationLogger<SGEGenotype<String>, String>("%8.1f", 8, problem.getPhenotypePrinter(), problem.getGeneralizationFitnessComputer(), constants));
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

  private static String dateForFile() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
    return simpleDateFormat.format(new Date());
  }

}
