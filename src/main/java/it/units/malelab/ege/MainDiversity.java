/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.distance.Distance;
import it.units.malelab.ege.distance.EditDistance;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.StandardConfiguration;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.PartitionConfiguration;
import it.units.malelab.ege.evolver.PartitionEvolver;
import it.units.malelab.ege.evolver.StandardEvolver;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.listener.ScreenGenerationLogger;
import it.units.malelab.ege.evolver.listener.StreamGenerationLogger;
import it.units.malelab.ege.evolver.selector.Best;
import it.units.malelab.ege.evolver.selector.IndividualComparator;
import it.units.malelab.ege.evolver.selector.Selector;
import it.units.malelab.ege.evolver.selector.Uniform;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.StandardGEMapper;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
    Map<String, IndividualComparator.Attribute> diversities = new LinkedHashMap<>();
    //diversities.put("genotype", IndividualComparator.Attribute.GENO);
    diversities.put("phenotype", IndividualComparator.Attribute.PHENO);
    //diversities.put("fitness", IndividualComparator.Attribute.FITNESS);
    //diversities.put("off", null);
    List<String> representerSelectors = new ArrayList<>();
    //representerSelectors.add("uniform");
    //representerSelectors.add("youngest");
    //representerSelectors.add("oldest");
    representerSelectors.add("smallest");
    representerSelectors.add("largest");
    representerSelectors.add("off");
    Map<String, Integer> strategies = new LinkedHashMap<>();
    strategies.put("steady-state", 1);
    //strategies.put("over-0.8", 400);
    boolean writeHeader = true;
    for (String problemName : problems.keySet()) {
      BenchmarkProblems.Problem problem = problems.get(problemName);
      for (int r = 0; r < 30; r++) {
        Random random = new Random(r);
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("problem", problemName);
        constants.put("run", r);
        constants.put("variant", "GE-8-5");
        for (String strategyString : strategies.keySet()) {
          constants.put("strategy", strategyString);
          for (String diversityString : diversities.keySet()) {
            constants.put("diversity", diversityString);
            for (String representerSelectorString : representerSelectors) {
              constants.put("selector", representerSelectorString);
              Evolver<BitsGenotype, String> evolver = null;
              if (diversityString.equals("off")) {
                if (!representerSelectorString.equals("off")) {
                  continue;
                }
                evolver = new StandardEvolver<>(Runtime.getRuntime().availableProcessors() - 1,
                        StandardConfiguration.createDefault(problem, random)
                        .mapper(new StandardGEMapper<>(8, 5, problems.get(problemName).getGrammar()))
                        .offspringSize(strategies.get(strategyString)), random, false);
              } else {
                if (representerSelectorString.equals("off")) {
                  continue;
                }
                Selector<Individual<BitsGenotype, String>> selector = null;
                if (representerSelectorString.equals("uniform")) {
                  selector = new Uniform<>(random);
                } else if (representerSelectorString.equals("youngest")) {
                  selector = (Selector) new Best<>(new IndividualComparator(IndividualComparator.Attribute.AGE));
                } else if (representerSelectorString.equals("oldest")) {
                  selector = (Selector) new Best<>(new IndividualComparator(Collections.singletonMap(IndividualComparator.Attribute.AGE, true)));
                } else if (representerSelectorString.equals("smallest")) {
                  selector = (Selector) new Best<>(new IndividualComparator(IndividualComparator.Attribute.PHENO_SIZE));
                } else if (representerSelectorString.equals("largest")) {
                  selector = (Selector) new Best<>(new IndividualComparator(Collections.singletonMap(IndividualComparator.Attribute.PHENO_SIZE, true)));
                }
                evolver = new PartitionEvolver<>(Runtime.getRuntime().availableProcessors() - 1,
                        (PartitionConfiguration) PartitionConfiguration.createDefault(problem, random)
                        .partitionerComparator((Comparator) (new IndividualComparator(diversities.get(diversityString))))
                        .mapper(new StandardGEMapper<>(8, 5, problems.get(problemName).getGrammar()))
                        .parentSelector(selector)
                        .populationInitializer(new RandomInitializer<>(random, new BitsGenotypeFactory(256)))
                        .offspringSize(strategies.get(strategyString)), random, false);
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

      }

    }
    generationFilePS.close();
  }

  private static String dateForFile() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
    return simpleDateFormat.format(new Date());
  }

}
