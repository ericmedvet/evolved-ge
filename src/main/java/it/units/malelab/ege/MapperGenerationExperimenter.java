/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.benchmark.Text;
import it.units.malelab.ege.benchmark.booleanfunction.Parity;
import it.units.malelab.ege.benchmark.mapper.MapperGeneration;
import it.units.malelab.ege.benchmark.symbolicregression.Nguyen7;
import it.units.malelab.ege.cfggp.initializer.FullTreeFactory;
import it.units.malelab.ege.cfggp.initializer.GrowTreeFactory;
import it.units.malelab.ege.cfggp.mapper.CfgGpMapper;
import it.units.malelab.ege.cfggp.operator.StandardTreeCrossover;
import it.units.malelab.ege.cfggp.operator.StandardTreeMutation;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.evolver.PartitionConfiguration;
import it.units.malelab.ege.core.evolver.PartitionEvolver;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.core.initializer.MultiInitializer;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.initializer.RandomInitializer;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.collector.BestPrinter;
import it.units.malelab.ege.core.listener.collector.Diversity;
import it.units.malelab.ege.core.listener.collector.MultiObjectiveFitnessFirstBest;
import it.units.malelab.ege.core.listener.collector.Population;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.ranker.ComparableRanker;
import it.units.malelab.ege.core.ranker.ParetoRanker;
import it.units.malelab.ege.core.selector.FirstBest;
import it.units.malelab.ege.core.selector.IndividualComparator;
import it.units.malelab.ege.core.selector.LastWorst;
import it.units.malelab.ege.core.selector.Tournament;
import it.units.malelab.ege.ge.genotype.validator.Any;
import it.units.malelab.ege.util.Utils;
import static it.units.malelab.ege.util.Utils.*;
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
public class MapperGenerationExperimenter {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    int maxDepth = 16;
    Random random = new Random(1l);
    Problem<String, MultiObjectiveFitness> problem = new MapperGeneration(256, 50, random,
            new Nguyen7(0),
            new Parity(8),
            new Text()
    );
    //print baseline
    MultiObjectiveFitness geF = problem.getLearningFitnessComputer().compute(getGERawTree());
    System.out.printf("GE fitness:\t%4.2f %4.2f %4.2f%n",
            geF.getValue()[0], geF.getValue()[1], geF.getValue()[2]
    );
    PartitionConfiguration<Node<String>, String, MultiObjectiveFitness> configuration = new PartitionConfiguration<>(
            new IndividualComparator<Node<String>, String, MultiObjectiveFitness>(IndividualComparator.Attribute.PHENO),
            10,
            new ComparableRanker<>(new IndividualComparator<Node<String>, String, MultiObjectiveFitness>(IndividualComparator.Attribute.AGE)),
            new FirstBest<Individual<Node<String>, String, MultiObjectiveFitness>>(),
            new ComparableRanker<>(new IndividualComparator<Node<String>, String, MultiObjectiveFitness>(IndividualComparator.Attribute.AGE)),
            new LastWorst<Individual<Node<String>, String, MultiObjectiveFitness>>(),
            50,
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
            new ParetoRanker<Node<String>, String, MultiObjectiveFitness>(),
            new Tournament<Individual<Node<String>, String, MultiObjectiveFitness>>(3, random),
            new LastWorst<Individual<Node<String>, String, MultiObjectiveFitness>>(),
            500,
            true,
            problem);
    List<EvolverListener<Node<String>, String, MultiObjectiveFitness>> listeners = new ArrayList<>();
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP, System.out, true, 10, " ", " | ",
            new Population<Node<String>, String, MultiObjectiveFitness>(),
            new MultiObjectiveFitnessFirstBest<Node<String>, String>(false, problem.getTestingFitnessComputer(), "%4.2f", "%4.2f", "%+4.2f"),
            new Diversity<Node<String>, String, MultiObjectiveFitness>(),
            new BestPrinter<Node<String>, String, MultiObjectiveFitness>(problem.getPhenotypePrinter(), "%30.30s")
    ));
    Evolver<Node<String>, String, MultiObjectiveFitness> evolver = new PartitionEvolver<>(
            //configuration, 1, random, false);
            configuration, Runtime.getRuntime().availableProcessors() - 1, random, false);
    List<Node<String>> bests = evolver.solve(listeners);
    System.out.printf("Found %d solutions.%n", bests.size());
  }

  public static Node<String> getGERawTree() {
    return node("<mapper>",
            node("<n>",
                    node("<fun_n_g>",
                            node("int")
                    ),
                    node("("),
                    node("<g>",
                            node("<fun_g_g,n>",
                                    node("substring")
                            ),
                            node("("),
                            node("<g>",
                                    node("<fun_g_g,n>",
                                            node("rotate_sx")
                                    ),
                                    node("("),
                                    node("<g>",
                                            node("<var_g>",
                                                    node("g")
                                            )
                                    ),
                                    node(","),
                                    node("<n>",
                                            node("<fun_n_n,n>",
                                                    node("*")
                                            ),
                                            node("("),
                                            node("<n>",
                                                    node("<var_n>",
                                                            node("g_count_rw")
                                                    )),
                                            node(","),
                                            node("<n>",
                                                    node("<const_n>",
                                                            node("8")
                                                    )),
                                            node(")")
                                    ),
                                    node(")")
                            ),
                            node(","),
                            node("<n>",
                                    node("<const_n>",
                                            node("8")
                                    )
                            ),
                            node(")")
                    ),
                    node(")")
            ),
            node("<lg>",
                    node("<fun_lg_g,n>",
                            node("repeat")
                    ),
                    node("("),
                    node("<g>",
                            node("<var_g>",
                                    node("g")
                            )
                    ),
                    node(","),
                    node("<n>",
                            node("<fun_n_ln>",
                                    node("length")
                            ),
                            node("("),
                            node("<ln>",
                                    node("<var_ln>",
                                            node("ln")
                                    )
                            ),
                            node(")")
                    ),
                    node(")")
            )
    );
  }

}
