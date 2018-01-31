/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.validator.Validator;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.ranker.Ranker;
import it.units.malelab.ege.core.selector.Selector;
import it.units.malelab.ege.util.distance.Distance;
import java.util.Map;

/**
 *
 * @author eric
 */
public class DeterministicCrowdingConfiguration<G, T, F extends Fitness> extends StandardConfiguration<G, T, F> {

  private final Distance<Individual<G, T, F>> individualDistance;

  public DeterministicCrowdingConfiguration(Distance<Individual<G, T, F>> individualDistance, int populationSize, int numberOfGenerations, PopulationInitializer<G> populationInitializer, Validator<G> initGenotypeValidator, Mapper<G, T> mapper, Map<GeneticOperator<G>, Double> operators, Ranker<Individual<G, T, F>> ranker, Selector<Individual<G, T, F>> parentSelector, Problem<T, F> problem, boolean actualEvaluations, double maxRelativeElapsed, double maxElapsed) {
    super(
            populationSize,
            numberOfGenerations,
            populationInitializer,
            initGenotypeValidator,
            mapper,
            operators,
            ranker,
            parentSelector,
            null,
            1,
            true,
            problem,
            actualEvaluations,
            maxRelativeElapsed,
            maxElapsed
    );
    this.individualDistance = individualDistance;
  }

  public Distance<Individual<G, T, F>> getIndividualDistance() {
    return individualDistance;
  }

  @Override
  public String toString() {
    return "DeterministicCrowdingEvolver{" + "individualDistance=" + individualDistance + '}';
  }

}
