/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.evolver.validator.AnyValidator;
import it.units.malelab.ege.evolver.Configuration;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.StandardEvolver;
import it.units.malelab.ege.evolver.fitness.FitnessComputer;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.listener.AbstractGenerationLogger;
import it.units.malelab.ege.evolver.listener.ScreenGenerationLogger;
import it.units.malelab.ege.evolver.selector.TournamentSelector;
import it.units.malelab.ege.evolver.operator.Copy;
import it.units.malelab.ege.evolver.operator.ProbabilisticMutation;
import it.units.malelab.ege.evolver.operator.TwoPointsCrossover;
import it.units.malelab.ege.evolver.selector.BestSelector;
import it.units.malelab.ege.mapper.BitsSGEMapper;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.StandardGEMapper;
import it.units.malelab.ege.mapper.WeightedHierarchicalMapper;
import it.units.malelab.ege.symbolicregression.MathUtils;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class Main {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, MappingException {
    BenchmarkProblems.Problem problem = BenchmarkProblems.max();
    Configuration<BitsGenotype, String> configuration = defaultConfiguration(problem, 1)
            .mapper(new WeightedHierarchicalMapper<>(10, problem.getGrammar()))
            .mapper(new BitsSGEMapper<>(10, problem.getGrammar()));
    Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(1, configuration);
    List<EvolutionListener<BitsGenotype, String>> listeners = new ArrayList<>();
    listeners.add(new ScreenGenerationLogger<BitsGenotype, String>("%5.1f", 5, problem.getPhenotypePrinter(), problem.getGeneralizationFitnessComputer()));
    evolver.go(listeners);
  }
  
  private String dateForFile() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
    return simpleDateFormat.format(new Date());
  }
  
  private static Configuration<BitsGenotype, String> defaultConfiguration(BenchmarkProblems.Problem problem, long randomSeed) {
    Random random = new Random(randomSeed);
    Configuration<BitsGenotype, String> configuration = new Configuration<>();
    configuration
            .populationSize(500)
            .numberOfGenerations(100)
            .populationInitializer(new RandomInitializer<>(random, new BitsGenotypeFactory(80)))
            .initGenotypeValidator(new AnyValidator<BitsGenotype>())
            .mapper(new StandardGEMapper<>(8, 5, problem.getGrammar()))
            .operators(Arrays.asList(
                            new Configuration.GeneticOperatorConfiguration<>(new Copy<BitsGenotype>(), new BestSelector(), 0.11d),
                            new Configuration.GeneticOperatorConfiguration<>(new TwoPointsCrossover(random), new TournamentSelector(3, random), 0.7d),
                            new Configuration.GeneticOperatorConfiguration<>(new ProbabilisticMutation(random, 0.02), new TournamentSelector(3, random), 0.19d)
                    ))
            .fitnessComputer(problem.getFitnessComputer());
    return configuration;
  }

}
