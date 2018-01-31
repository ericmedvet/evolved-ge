/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.selector.Selector;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.validator.Validator;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.ranker.Ranker;
import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author eric
 */
public class PartitionConfiguration<G, T, F extends Fitness> extends StandardConfiguration<G, T, F> {
  
  private final Comparator<Individual<G, T, F>> partitionerComparator;
  private final int partitionSize;
  private final Ranker<Individual<G, T, F>> parentInPartitionRanker;
  private final Selector<Individual<G, T, F>> parentInPartitionSelector;
  private final Ranker<Individual<G, T, F>> unsurvivalInPartitionRanker;
  private final Selector<Individual<G, T, F>> unsurvivalInPartitionSelector;

  public PartitionConfiguration(Comparator<Individual<G, T, F>> partitionerComparator, int partitionSize, Ranker<Individual<G, T, F>> parentInPartitionRanker, Selector<Individual<G, T, F>> parentInPartitionSelector, Ranker<Individual<G, T, F>> unsurvivalInPartitionRanker, Selector<Individual<G, T, F>> unsurvivalInPartitionSelector, int populationSize, int numberOfGenerations, PopulationInitializer<G> populationInitializer, Validator<G> initGenotypeValidator, Mapper<G, T> mapper, Map<GeneticOperator<G>, Double> operators, Ranker<Individual<G, T, F>> ranker, Selector<Individual<G, T, F>> parentSelector, Selector<Individual<G, T, F>> unsurvivalSelector, int offspringSize, boolean overlapping, Problem<T, F> problem, boolean actualEvaluations, double maxRelativeElapsed, double maxElapsed) {
    super(populationSize, numberOfGenerations, populationInitializer, initGenotypeValidator, mapper, operators, ranker, parentSelector, unsurvivalSelector, offspringSize, overlapping, problem, actualEvaluations, maxRelativeElapsed, maxElapsed);
    this.partitionerComparator = partitionerComparator;
    this.partitionSize = partitionSize;
    this.parentInPartitionRanker = parentInPartitionRanker;
    this.parentInPartitionSelector = parentInPartitionSelector;
    this.unsurvivalInPartitionRanker = unsurvivalInPartitionRanker;
    this.unsurvivalInPartitionSelector = unsurvivalInPartitionSelector;
  }

  public Comparator<Individual<G, T, F>> getPartitionerComparator() {
    return partitionerComparator;
  }

  public int getPartitionSize() {
    return partitionSize;
  }

  public Ranker<Individual<G, T, F>> getParentInPartitionRanker() {
    return parentInPartitionRanker;
  }

  public Selector<Individual<G, T, F>> getParentInPartitionSelector() {
    return parentInPartitionSelector;
  }

  public Ranker<Individual<G, T, F>> getUnsurvivalInPartitionRanker() {
    return unsurvivalInPartitionRanker;
  }

  public Selector<Individual<G, T, F>> getUnsurvivalInPartitionSelector() {
    return unsurvivalInPartitionSelector;
  }

  @Override
  public String toString() {
    return "PartitionConfiguration{" + "partitionerComparator=" + partitionerComparator + ", partitionSize=" + partitionSize + ", parentInPartitionRanker=" + parentInPartitionRanker + ", parentInPartitionSelector=" + parentInPartitionSelector + ", unsurvivalInPartitionRanker=" + unsurvivalInPartitionRanker + ", unsurvivalInPartitionSelector=" + unsurvivalInPartitionSelector + "} from " + super.toString();
  }
  
}
