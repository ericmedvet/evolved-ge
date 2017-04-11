/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.selector.Selector;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.Validator;
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
  private final Ranker<Individual<G, T, F>> partitionRanker;
  private final Selector<Individual<G, T, F>> parentRepresenterSelector;
  private final Selector<Individual<G, T, F>> unsurvivalRepresenterSelector;

  public PartitionConfiguration(Comparator<Individual<G, T, F>> partitionerComparator, int partitionSize, Ranker<Individual<G, T, F>> partitionRanker, Selector<Individual<G, T, F>> parentRepresenterSelector, Selector<Individual<G, T, F>> unsurvivalRepresenterSelector, int populationSize, int numberOfGenerations, PopulationInitializer<G> populationInitializer, Validator<G> initGenotypeValidator, Mapper<G, T> mapper, Map<GeneticOperator<G>, Double> operators, Ranker<Individual<G, T, F>> ranker, Selector<Individual<G, T, F>> parentSelector, Selector<Individual<G, T, F>> unsurvivalSelector, int offspringSize, boolean overlapping, Problem<T, F> problem) {
    super(populationSize, numberOfGenerations, populationInitializer, initGenotypeValidator, mapper, operators, ranker, parentSelector, unsurvivalSelector, offspringSize, overlapping, problem);
    this.partitionerComparator = partitionerComparator;
    this.partitionSize = partitionSize;
    this.partitionRanker = partitionRanker;
    this.parentRepresenterSelector = parentRepresenterSelector;
    this.unsurvivalRepresenterSelector = unsurvivalRepresenterSelector;
  }

  public Comparator<Individual<G, T, F>> getPartitionerComparator() {
    return partitionerComparator;
  }

  public int getPartitionSize() {
    return partitionSize;
  }

  public Ranker<Individual<G, T, F>> getPartitionRanker() {
    return partitionRanker;
  }

  public Selector<Individual<G, T, F>> getParentRepresenterSelector() {
    return parentRepresenterSelector;
  }

  public Selector<Individual<G, T, F>> getUnsurvivalRepresenterSelector() {
    return unsurvivalRepresenterSelector;
  }

  @Override
  public String toString() {
    return "PartitionConfiguration{" + "partitionerComparator=" + partitionerComparator + ", partitionSize=" + partitionSize + ", partitionRanker=" + partitionRanker + ", parentRepresenterSelector=" + parentRepresenterSelector + ", unsurvivalRepresenterSelector=" + unsurvivalRepresenterSelector + '}';
  }
  
}
