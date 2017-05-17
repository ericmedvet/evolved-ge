/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import com.google.common.collect.Lists;
import it.units.malelab.ege.benchmark.KLandscapes;
import it.units.malelab.ege.benchmark.Text;
import it.units.malelab.ege.benchmark.booleanfunction.MultipleOutputParallelMultiplier;
import it.units.malelab.ege.benchmark.booleanfunction.Parity;
import it.units.malelab.ege.benchmark.symbolicregression.HarmonicCurve;
import it.units.malelab.ege.benchmark.symbolicregression.Nguyen7;
import it.units.malelab.ege.benchmark.symbolicregression.Pagie1;
import it.units.malelab.ege.benchmark.symbolicregression.Vladislavleva4;
import it.units.malelab.ege.cfggp.initializer.FullTreeFactory;
import it.units.malelab.ege.cfggp.initializer.GrowTreeFactory;
import it.units.malelab.ege.cfggp.mapper.CfgGpMapper;
import it.units.malelab.ege.cfggp.operator.StandardTreeCrossover;
import it.units.malelab.ege.cfggp.operator.StandardTreeMutation;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.evolver.StandardEvolver;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.initializer.MultiInitializer;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.initializer.RandomInitializer;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.collector.BestPrinter;
import it.units.malelab.ege.core.listener.collector.Diversity;
import it.units.malelab.ege.core.listener.collector.NumericFirstBest;
import it.units.malelab.ege.core.listener.collector.Population;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.ranker.ComparableRanker;
import it.units.malelab.ege.core.selector.IndividualComparator;
import it.units.malelab.ege.core.selector.LastWorst;
import it.units.malelab.ege.core.selector.Tournament;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.ge.genotype.SGEGenotype;
import it.units.malelab.ege.ge.genotype.SGEGenotypeFactory;
import it.units.malelab.ege.ge.genotype.validator.Any;
import it.units.malelab.ege.ge.mapper.HierarchicalMapper;
import it.units.malelab.ege.ge.mapper.PiGEMapper;
import it.units.malelab.ege.ge.mapper.SGEMapper;
import it.units.malelab.ege.ge.mapper.StandardGEMapper;
import it.units.malelab.ege.ge.mapper.WeightedHierarchicalMapper;
import it.units.malelab.ege.ge.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.ge.operator.ProbabilisticMutation;
import it.units.malelab.ege.ge.operator.SGECrossover;
import it.units.malelab.ege.ge.operator.SGEMutation;
import it.units.malelab.ege.util.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class Experimenter {

  private final static int N_THREADS = 1;

  public static void main(String[] args) throws IOException {
    Random random = new Random(1);
    int genotypeSize = 1024;
    //prepare problems and methods
    List<String> problems = Lists.newArrayList(
            "bool-parity5", "bool-mopm3",
            "sr-keijzer6", "sr-nguyen7", "sr-pagie1", "sr-vladislavleva4",
            "other-klandscapes3", "other-klandscapes7", "other-text"
    );
    List<String> methods = Lists.newArrayList("ge-8", "pige-16", "sge-6", "hge", "whge-3", "cfggp-16");
    //iterate
    for (String problemName : problems) {
      for (String methodName : methods) {
        if (!problemName.equals("other-binaryregex")) {
          //manage differently, since it is multi-objective
        } else {
          //build problem
          Problem<String, NumericFitness> problem = null;
          if (problemName.equals("bool-parity5")) {
            problem = new Parity(5);
          } else if (problemName.equals("bool-mopm3")) {
            problem = new MultipleOutputParallelMultiplier(3);
          } else if (problemName.equals("sr-keijzer6")) {
            problem = new HarmonicCurve();
          } else if (problemName.equals("sr-nguyen7")) {
            problem = new Nguyen7(1);
          } else if (problemName.equals("sr-pagie1")) {
            problem = new Pagie1();
          } else if (problemName.equals("sr-vladislavleva4")) {
            problem = new Vladislavleva4(1);
          } else if (problemName.equals("other-klandscapes3")) {
            problem = new KLandscapes(3);
          } else if (problemName.equals("other-klandscapes7")) {
            problem = new KLandscapes(7);
          } else if (problemName.equals("other-text")) {
            problem = new Text();
          }
          //build configuration and evolver
          Evolver<?, String, NumericFitness> evolver = null;
          if (methodName.equals("ge-8")) {
            StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
                    500,
                    50,
                    new RandomInitializer<>(random, new BitsGenotypeFactory(genotypeSize)),
                    new Any<BitsGenotype>(),
                    new StandardGEMapper<>(8, 1, problem.getGrammar()),
                    new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
                    .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
                    .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
                    new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
                    new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3, random),
                    new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(),
                    500,
                    true,
                    problem);
            evolver = new StandardEvolver<>(N_THREADS, configuration, random, false);
          } else if (methodName.equals("pige-16")) {
            StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
                    500,
                    50,
                    new RandomInitializer<>(random, new BitsGenotypeFactory(genotypeSize)),
                    new Any<BitsGenotype>(),
                    new PiGEMapper<>(8, 1, problem.getGrammar()),
                    new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
                    .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
                    .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
                    new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
                    new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3, random),
                    new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(),
                    500,
                    true,
                    problem);
            evolver = new StandardEvolver<>(N_THREADS, configuration, random, false);
          } else if (methodName.equals("sge-6")) {
            Mapper<SGEGenotype<String>, String> mapper = new SGEMapper<>(6, problem.getGrammar());
            StandardConfiguration<SGEGenotype<String>, String, NumericFitness> configuration = new StandardConfiguration<>(
                    500,
                    50,
                    new RandomInitializer<>(random, new SGEGenotypeFactory<>((SGEMapper<String>) mapper)),
                    new Any<SGEGenotype<String>>(),
                    mapper,
                    new Utils.MapBuilder<GeneticOperator<SGEGenotype<String>>, Double>()
                    .put(new SGECrossover<String>(random), 0.8d)
                    .put(new SGEMutation<>(0.01, (SGEMapper<String>) mapper, random), 0.2d).build(),
                    new ComparableRanker<>(new IndividualComparator<SGEGenotype<String>, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
                    new Tournament<Individual<SGEGenotype<String>, String, NumericFitness>>(3, random),
                    new LastWorst<Individual<SGEGenotype<String>, String, NumericFitness>>(),
                    500,
                    true,
                    problem);
            evolver = new StandardEvolver<>(N_THREADS, configuration, random, false);
          } else if (methodName.equals("hge")) {
            StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
                    500,
                    50,
                    new RandomInitializer<>(random, new BitsGenotypeFactory(genotypeSize)),
                    new Any<BitsGenotype>(),
                    new HierarchicalMapper<>(problem.getGrammar()),
                    new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
                    .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
                    .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
                    new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
                    new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3, random),
                    new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(),
                    500,
                    true,
                    problem);
            evolver = new StandardEvolver<>(N_THREADS, configuration, random, false);
          } else if (methodName.equals("whge-3")) {
            StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
                    500,
                    50,
                    new RandomInitializer<>(random, new BitsGenotypeFactory(genotypeSize)),
                    new Any<BitsGenotype>(),
                    new WeightedHierarchicalMapper<>(3, problem.getGrammar()),
                    new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
                    .put(new LengthPreservingTwoPointsCrossover(random), 0.8d)
                    .put(new ProbabilisticMutation(random, 0.01), 0.2d).build(),
                    new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
                    new Tournament<Individual<BitsGenotype, String, NumericFitness>>(3, random),
                    new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(),
                    500,
                    true,
                    problem);
            evolver = new StandardEvolver<>(N_THREADS, configuration, random, false);
          } else if (methodName.equals("cfggp-16")) {
            int maxDepth = 16;
            StandardConfiguration<Node<String>, String, NumericFitness> configuration = new StandardConfiguration<>(
                    500,
                    50,
                    new MultiInitializer<>(new Utils.MapBuilder<PopulationInitializer<Node<String>>, Double>()
                            .put(new RandomInitializer<>(random, new GrowTreeFactory<>(maxDepth, problem.getGrammar())), 0.5)
                            .put(new RandomInitializer<>(random, new FullTreeFactory<>(maxDepth, problem.getGrammar())), 0.5)
                            .build()
                    ),
                    new Any<Node<String>>(),
                    new CfgGpMapper<String>(),
                    new Utils.MapBuilder<GeneticOperator<Node<String>>, Double>()
                    .put(new StandardTreeCrossover<String>(maxDepth, random), 0.8d)
                    .put(new StandardTreeMutation<>(maxDepth, problem.getGrammar(), random), 0.2d)
                    .build(),
                    new ComparableRanker<>(new IndividualComparator<Node<String>, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
                    new Tournament<Individual<Node<String>, String, NumericFitness>>(3, random),
                    new LastWorst<Individual<Node<String>, String, NumericFitness>>(),
                    500,
                    true,
                    problem);
            evolver = new StandardEvolver<>(N_THREADS, configuration, random, false);
          }
          //go
          //List<Node<String>> bests = evolver.solve(listeners);
          //System.out.printf("Found %d solutions.%n", bests.size());
        }
      }
    }

  }

}
