/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver.geneoptimalmixing;

import it.units.malelab.ege.core.ConstrainedSequence;
import it.units.malelab.ege.core.evolver.geneoptimalmixing.fos.FOSBuilder;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.validator.Validator;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.ranker.Ranker;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.operator.AbstractMutation;
import java.util.Collections;

/**
 *
 * @author eric
 */
public class GOMConfiguration<G extends ConstrainedSequence, T, F extends Fitness> extends StandardConfiguration<G, T, F> {

  private final FOSBuilder fosBuilder;
  private final AbstractMutation<G> mutationOperator;

  public GOMConfiguration(FOSBuilder fosBuilder, AbstractMutation<G> mutationOperator, int populationSize, int numberOfGenerations, PopulationInitializer<G> populationInitializer, Validator<G> initGenotypeValidator, Mapper<G, T> mapper, Ranker<Individual<G, T, F>> ranker, Problem<T, F> problem, boolean actualEvaluations, double maxRelativeTimeMult) {
    super(populationSize, numberOfGenerations, populationInitializer, initGenotypeValidator, mapper, Collections.EMPTY_MAP, ranker, null, null, populationSize, false, problem, actualEvaluations, maxRelativeTimeMult);
    this.fosBuilder = fosBuilder;
    this.mutationOperator = mutationOperator;
  }

  public FOSBuilder getFosBuilder() {
    return fosBuilder;
  }

  public AbstractMutation<G> getMutationOperator() {
    return mutationOperator;
  }

  @Override
  public String toString() {
    return "GOMConfiguration{" + "fosBuilder=" + fosBuilder + ", mutationOperator=" + mutationOperator + '}';
  }

}
