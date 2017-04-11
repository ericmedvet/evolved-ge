/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.benchmark.HarmonicCurve;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.collector.NumericFirstBest;
import it.units.malelab.ege.core.listener.collector.Population;
import it.units.malelab.ege.core.listener.collector.Diversity;
import it.units.malelab.ege.core.selector.LastWorst;
import it.units.malelab.ege.core.selector.Tournament;
import it.units.malelab.ege.core.selector.IndividualComparator;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.evolver.StandardEvolver;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.core.initializer.RandomInitializer;
import it.units.malelab.ege.ge.genotype.validator.Any;
import it.units.malelab.ege.ge.mapper.StandardGEMapper;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.ranker.ComparableRanker;
import it.units.malelab.ege.ge.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.ge.operator.ProbabilisticMutation;
import it.units.malelab.ege.util.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class ExampleMain {

  public final static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    Random random = new Random(1l);
    Problem<String, NumericFitness> problem = new HarmonicCurve();
    StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
            500,
            50,
            new RandomInitializer<>(random, new BitsGenotypeFactory(256)),
            new Any<BitsGenotype>(),
            new StandardGEMapper<>(8, 5, problem.getGrammar()),
            new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
                    .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
                    .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
            new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3, random),
            new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(),
            500,
            true,
            problem);
    List<EvolverListener<BitsGenotype, String, NumericFitness>> listeners = new ArrayList<>();
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP, System.out, true, 10, " ", " | ",
            new Population<BitsGenotype, String, NumericFitness>("%7.2f"),
            new NumericFirstBest<BitsGenotype, String>("%6.2f", false),
            new Diversity<BitsGenotype, String, NumericFitness>()
    ));
    Evolver<BitsGenotype, String, NumericFitness> evolver = new StandardEvolver<>(
            1, configuration, random, false);
    List<Node<String>> bests = evolver.solve(listeners);
    System.out.printf("Found %d solutions.%n", bests.size());
  }

}
