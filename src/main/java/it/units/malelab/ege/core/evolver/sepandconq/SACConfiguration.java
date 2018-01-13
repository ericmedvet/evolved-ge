/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver.sepandconq;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.validator.Validator;
import it.units.malelab.ege.core.evolver.PartitionConfiguration;
import it.units.malelab.ege.core.fitness.BinaryClassification;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.ranker.Ranker;
import it.units.malelab.ege.core.selector.Selector;
import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author eric
 */
public class SACConfiguration<I, G, T, F extends MultiObjectiveFitness> extends PartitionConfiguration<G, T, F> {

  private final Joiner<T> joiner;

  public SACConfiguration(Joiner<T> joiner, Comparator<Individual<G, T, F>> partitionerComparator, int partitionSize, Ranker<Individual<G, T, F>> parentInPartitionRanker, Selector<Individual<G, T, F>> parentInPartitionSelector, Ranker<Individual<G, T, F>> unsurvivalInPartitionRanker, Selector<Individual<G, T, F>> unsurvivalInPartitionSelector, int populationSize, int numberOfGenerations, PopulationInitializer<G> populationInitializer, Validator<G> initGenotypeValidator, Mapper<G, T> mapper, Map<GeneticOperator<G>, Double> operators, Ranker<Individual<G, T, F>> ranker, Selector<Individual<G, T, F>> parentSelector, Selector<Individual<G, T, F>> unsurvivalSelector, int offspringSize, boolean overlapping, Problem<T, F> problem, boolean actualEvaluations, double maxRelativeTimeMult) {
    super(partitionerComparator, partitionSize, parentInPartitionRanker, parentInPartitionSelector, unsurvivalInPartitionRanker, unsurvivalInPartitionSelector, populationSize, numberOfGenerations, populationInitializer, initGenotypeValidator, mapper, operators, ranker, parentSelector, unsurvivalSelector, offspringSize, overlapping, problem, actualEvaluations, maxRelativeTimeMult);
    this.joiner = joiner;
  }
  
  public Joiner<T> getJoiner() {
    return joiner;
  }

  @Override
  public String toString() {
    return "SACConfiguration{" + "joiner=" + joiner + "} from "+super.toString();
  }

}
