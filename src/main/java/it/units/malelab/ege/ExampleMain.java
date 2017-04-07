/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.benchmark.HarmonicCurve;
import it.units.malelab.ege.core.Evolver;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.grammar.Node;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.collector.NumericFirstBest;
import it.units.malelab.ege.core.listener.collector.Population;
import it.units.malelab.ege.core.selector.LastWorst;
import it.units.malelab.ege.core.selector.Tournament;
import it.units.malelab.ege.ge.GEEvolver;
import it.units.malelab.ege.ge.GEIndividual;
import it.units.malelab.ege.ge.evolver.StandardConfiguration;
import it.units.malelab.ege.ge.evolver.StandardEvolver;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.ge.genotype.initializer.RandomInitializer;
import it.units.malelab.ege.ge.genotype.validator.AnyValidator;
import it.units.malelab.ege.ge.listener.collector.GEDiversity;
import it.units.malelab.ege.ge.mapper.StandardGEMapper;
import it.units.malelab.ege.ge.operator.GeneticOperator;
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
            new RandomInitializer<BitsGenotype>(random, new BitsGenotypeFactory(256)),
            new AnyValidator<BitsGenotype>(),
            new StandardGEMapper<>(8, 5, problem.getGrammar()),
            new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
                    .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
                    .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
            new Tournament<GEIndividual<BitsGenotype, String, NumericFitness>>(3, random),
            new LastWorst<GEIndividual<BitsGenotype, String, NumericFitness>>(),
            500,
            true,
            problem);
    List<EvolverListener<String, NumericFitness>> listeners = new ArrayList<>();
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP, System.out, true, 10, " ", " | ",
            new Population<String, NumericFitness>("%7.2f"),
            new NumericFirstBest<String>(false, "%6.2f"),
            new GEDiversity<String, NumericFitness>()
    ));
    Evolver<String, NumericFitness> evolver = new StandardEvolver<BitsGenotype, String, NumericFitness>(
            1, configuration, random, false);
    List<Node<String>> bests = evolver.solve(listeners);
    System.out.printf("Found %d solutions.", bests.size());
  }

}
