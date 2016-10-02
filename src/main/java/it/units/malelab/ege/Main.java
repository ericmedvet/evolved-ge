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
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.validator.AnyValidator;
import it.units.malelab.ege.evolver.Configuration;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.StandardEvolver;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.listener.DynamicLocalityAnalysisLogger;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.listener.ScreenGenerationLogger;
import it.units.malelab.ege.evolver.selector.TournamentSelector;
import it.units.malelab.ege.evolver.operator.ProbabilisticMutation;
import it.units.malelab.ege.evolver.operator.TwoPointsCrossover;
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
    PrintStream filePS = new PrintStream(args[0].replace("DATE", dateForFile()));
    final EditDistance<String> editDistance = new EditDistance<>();
    Map<String, BenchmarkProblems.Problem> problems = new LinkedHashMap<>();
    problems.put("harmonic", BenchmarkProblems.harmonicCurveProblem());
    problems.put("poly4", BenchmarkProblems.classic4PolynomialProblem());
    problems.put("max", BenchmarkProblems.max());
    problems.put("text", BenchmarkProblems.text("Hello world!"));
    for (String problemName : problems.keySet()) {
      BenchmarkProblems.Problem problem = problems.get(problemName);
      for (int r = 0; r < 30; r++) {
        Random random = new Random(r);
        for (int m = 0; m < 5; m++) {
          for (int genotypeSize : Arrays.asList(128, 256, 512, 1024, 2048)) {
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
                configuration.mapper(new BitsSGEMapper<>(6, grammar));
                break;
              case 4:
                configuration.mapper(new HierarchicalMapper<>(grammar));
                break;
              case 5:
                configuration.mapper(new WeightedHierarchicalMapper<>(6, grammar));
                break;
            }
            configuration.populationInitializer(new RandomInitializer<>(random, new BitsGenotypeFactory(1024)));
            Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(Runtime.getRuntime().availableProcessors()-1, configuration);
            Map<String, Object> constants = new LinkedHashMap<>();
            constants.put("problem", problemName);
            constants.put("mapper", configuration.getMapper().getClass().getSimpleName());
            constants.put("initGenoSize", genotypeSize);
            constants.put("run", r);
            List<EvolutionListener<BitsGenotype, String>> listeners = new ArrayList<>();
            listeners.add(new ScreenGenerationLogger<BitsGenotype, String>("%8.1f", 8, problem.getPhenotypePrinter(), problem.getGeneralizationFitnessComputer(), constants));
            listeners.add(new DynamicLocalityAnalysisLogger<>(filePS, new CachedDistance<>(new BitsGenotypeEditDistance()), new CachedDistance<>(new Distance<Node<String>>() {
              @Override
              public double d(Node<String> t1, Node<String> t2) {
                List<String> s1 = Node.EMPTY_TREE.equals(t1) ? Collections.EMPTY_LIST : Utils.contents(t1.leaves());
                List<String> s2 = Node.EMPTY_TREE.equals(t2) ? Collections.EMPTY_LIST : Utils.contents(t2.leaves());
                return editDistance.d(s1, s2);
              }
            }), constants));
            System.out.println(constants);
            evolver.go(listeners);
            System.out.println();
          }
        }
      }
    }
    filePS.close();
  }

  private static String dateForFile() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
    return simpleDateFormat.format(new Date());
  }

  private static Configuration<BitsGenotype, String> defaultConfiguration(BenchmarkProblems.Problem problem, Random random) {
    Configuration<BitsGenotype, String> configuration = new Configuration<>();
    configuration
            .populationSize(500)
            .numberOfGenerations(100)
            .populationInitializer(new RandomInitializer<>(random, new BitsGenotypeFactory(1024)))
            .initGenotypeValidator(new AnyValidator<BitsGenotype>())
            .mapper(new StandardGEMapper<>(8, 5, problem.getGrammar()))
            .operators(Arrays.asList(
                            new Configuration.GeneticOperatorConfiguration<>(new TwoPointsCrossover(random), new TournamentSelector(5, random), 0.8d),
                            new Configuration.GeneticOperatorConfiguration<>(new ProbabilisticMutation(random, 0.01), new TournamentSelector(5, random), 0.2d)
                    ))
            .fitnessComputer(problem.getFitnessComputer())
            .generationStrategy(Configuration.GenerationStrategy.ADD_OLD_FIRST);
    return configuration;
  }

}
