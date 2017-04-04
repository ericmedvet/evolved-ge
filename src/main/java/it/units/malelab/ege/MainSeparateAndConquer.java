/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.StandardConfiguration;
import it.units.malelab.ege.evolver.StandardEvolver;
import it.units.malelab.ege.evolver.fitness.RegexMatch;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.listener.CollectorGenerationLogger;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.listener.collector.Best;
import it.units.malelab.ege.evolver.listener.collector.Diversity;
import it.units.malelab.ege.evolver.listener.collector.MultiMapperInfo;
import it.units.malelab.ege.evolver.listener.collector.Population;
import it.units.malelab.ege.evolver.operator.LocalizedTwoPointsCrossover;
import it.units.malelab.ege.evolver.operator.ProbabilisticMutation;
import it.units.malelab.ege.evolver.selector.IndividualComparator;
import it.units.malelab.ege.evolver.selector.Tournament;
import it.units.malelab.ege.evolver.validator.AnyValidator;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.WeightedHierarchicalMapper;
import it.units.malelab.ege.util.Utils;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class MainSeparateAndConquer {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, MappingException {
    Random random = new Random(1);
    List<EvolutionListener<BitsGenotype, String>> listeners = new ArrayList<>();
    /*PrintStream generationFilePS = new PrintStream(args[0] + File.separator + dateForFile() + "-sc-generation.csv");
    listeners.add(new CollectorGenerationLogger<>(
            (Map) Utils.sameValueMap("", "key", "problem", "run", "initGenotypeSize", "variant", "pop"),
            generationFilePS, false, 0, ";", ";",
            new Population<>("%5.2f"),
            new Best<>("%5.2f"),
            new Diversity<>(),
            new MultiMapperInfo<>(4)
    ));*/
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP,
            System.out, true, 10, " ", " | ",
            new Best<>("%10.10s"),
            new Diversity<>()
    ));
    StandardConfiguration<BitsGenotype, String> configuration = new StandardConfiguration<>();
    configuration
            .fitnessComputer(new RegexMatch("01", 20, 100, random, "0+1?0+", "1010.+0101", "111.+", "1?0.+01?"))
            .mapper(new WeightedHierarchicalMapper<>(3, Utils.parseFromFile(new File("grammars/binary-regex.bnf"))))
            .populationSize(500)
            .offspringSize(500)
            .overlapping(true)
            .numberOfGenerations(100)
            .parentSelector(new Tournament(3, random, new IndividualComparator(IndividualComparator.Attribute.FITNESS)))
            .unsurvivalSelector(new it.units.malelab.ege.evolver.selector.Best(Collections.reverseOrder(new IndividualComparator(IndividualComparator.Attribute.FITNESS))))
            .populationInitializer(new RandomInitializer<>(random, new BitsGenotypeFactory(256)))
            .initGenotypeValidator(new AnyValidator())
            .operator(new LocalizedTwoPointsCrossover(random), 0.8d)
            .operator(new ProbabilisticMutation(random, 0.01), 0.2d);
    //Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(Runtime.getRuntime().availableProcessors() - 1, configuration, random, false);
    Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(1, configuration, random, false);
    evolver.go(listeners);
    System.out.println();
    //generationFilePS.close();
  }

  private static String dateForFile() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
    return simpleDateFormat.format(new Date());
  }

}
