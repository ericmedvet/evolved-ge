/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.distance.BitsGenotypeEditDistance;
import it.units.malelab.ege.distance.CachedDistance;
import it.units.malelab.ege.distance.Distance;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.validator.AnyValidator;
import it.units.malelab.ege.evolver.Configuration;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.StandardEvolver;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.listener.BestGenosImageGenerator;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.operator.BitsSGECrossover;
import it.units.malelab.ege.evolver.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.evolver.operator.OnePointCrossover;
import it.units.malelab.ege.evolver.selector.TournamentSelector;
import it.units.malelab.ege.evolver.operator.ProbabilisticMutation;
import it.units.malelab.ege.evolver.selector.BestSelector;
import it.units.malelab.ege.evolver.selector.IndividualComparator;
import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.mapper.BitsSGEMapper;
import it.units.malelab.ege.mapper.BreathFirstMapper;
import it.units.malelab.ege.mapper.HierarchicalMapper;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.PiGEMapper;
import it.units.malelab.ege.mapper.StandardGEMapper;
import it.units.malelab.ege.mapper.WeightedHierarchicalMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class MainBestGenosToPNG {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, MappingException {
    //define distances
    Map<String, Distance<BitsGenotype>> genotypeDistances = new LinkedHashMap<>();
    genotypeDistances.put("BitsEdit", new CachedDistance<>(new BitsGenotypeEditDistance()));
    Map<String, BenchmarkProblems.Problem> problems = new LinkedHashMap<>();
    problems.put("harmonic", BenchmarkProblems.harmonicCurveProblem());
    problems.put("poly4", BenchmarkProblems.classic4PolynomialProblem());
    problems.put("max", BenchmarkProblems.max());
    problems.put("santaFe", BenchmarkProblems.santaFe());
    problems.put("text", BenchmarkProblems.text("Hello world!"));
    for (String problemName : problems.keySet()) {
      BenchmarkProblems.Problem problem = problems.get(problemName);
      for (int r = 0; r < 1; r++) {
        Random random = new Random(r);
        for (int m = 0; m < 6; m++) {
          for (int genotypeSize : Arrays.asList(128)) {
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
                configuration.mapper(new WeightedHierarchicalMapper<>(6, grammar));
                break;
              case 4:
                configuration.mapper(new HierarchicalMapper<>(grammar));
                break;
              case 5:
                BitsSGEMapper<String> sgeMapper = new BitsSGEMapper<>(6, grammar);
                configuration.getOperators().clear();
                configuration
                        .mapper(sgeMapper)
                        .operator(new BitsSGECrossover(sgeMapper, random), 0.8d)
                        .operator(new ProbabilisticMutation(random, 0.01), 0.2d);
                break;
            }
            configuration.populationInitializer(new RandomInitializer<>(random, new BitsGenotypeFactory(genotypeSize)));
            Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(Runtime.getRuntime().availableProcessors() - 1, configuration, random);
            Map<String, Object> constants = new LinkedHashMap<>();
            constants.put("problem", problemName);
            constants.put("mapper", configuration.getMapper().getClass().getSimpleName());
            constants.put("initGenoSize", genotypeSize);
            constants.put("run", r);
            EvolutionListener<BitsGenotype, String> geno2png = new BestGenosImageGenerator<>(problem.getGeneralizationFitnessComputer(), constants);
            System.out.println(constants);
            System.out.print("0%");
            for (int i=6;i<configuration.getNumberOfGenerations();i++) {
                System.out.print("-");
            }
            System.out.print("100%");
            System.out.println();
            evolver.go(Arrays.asList(geno2png));
            ((BestGenosImageGenerator)geno2png).close();
            System.out.println();
          }
        }
      }
    }
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
