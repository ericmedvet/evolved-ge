/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.evolver;

import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.ge.genotype.Genotype;
import it.units.malelab.ege.core.selector.Selector;
import it.units.malelab.ege.ge.GEIndividual;
import it.units.malelab.ege.ge.genotype.initializer.PopulationInitializer;
import it.units.malelab.ege.ge.genotype.validator.GenotypeValidator;
import it.units.malelab.ege.ge.mapper.Mapper;
import it.units.malelab.ege.ge.operator.GeneticOperator;
import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author eric
 */
public class PartitionConfiguration<G extends Genotype, T, F extends Fitness> extends StandardConfiguration<G, T, F> {
  
  private final Comparator<GEIndividual<G, T, F>> partitionerComparator;
  private final int partitionSize;
  private final Selector<GEIndividual<G, T, F>> parentRepresenterSelector;
  private final Selector<GEIndividual<G, T, F>> unsurvivalRepresenterSelector;

  public PartitionConfiguration(Comparator<GEIndividual<G, T, F>> partitionerComparator, int partitionSize, Selector<GEIndividual<G, T, F>> parentRepresenterSelector, Selector<GEIndividual<G, T, F>> unsurvivalRepresenterSelector, int populationSize, int numberOfGenerations, PopulationInitializer<G> populationInitializer, GenotypeValidator<G> initGenotypeValidator, Mapper<G, T> mapper, Map<GeneticOperator<G>, Double> operators, Selector<GEIndividual<G, T, F>> parentSelector, Selector<GEIndividual<G, T, F>> unsurvivalSelector, int offspringSize, boolean overlapping, Problem<T, F> problem) {
    super(populationSize, numberOfGenerations, populationInitializer, initGenotypeValidator, mapper, operators, parentSelector, unsurvivalSelector, offspringSize, overlapping, problem);
    this.partitionerComparator = partitionerComparator;
    this.partitionSize = partitionSize;
    this.parentRepresenterSelector = parentRepresenterSelector;
    this.unsurvivalRepresenterSelector = unsurvivalRepresenterSelector;
  }

  public Comparator<GEIndividual<G, T, F>> getPartitionerComparator() {
    return partitionerComparator;
  }

  public int getPartitionSize() {
    return partitionSize;
  }

  public Selector<GEIndividual<G, T, F>> getParentRepresenterSelector() {
    return parentRepresenterSelector;
  }

  public Selector<GEIndividual<G, T, F>> getUnsurvivalRepresenterSelector() {
    return unsurvivalRepresenterSelector;
  }

  @Override
  public String toString() {
    return "PartitionConfiguration{" + "partitionerComparator=" + partitionerComparator + ", partitionSize=" + partitionSize + ", parentRepresenterSelector=" + parentRepresenterSelector + ", unsurvivalRepresenterSelector=" + unsurvivalRepresenterSelector + " (on "+super.toString()+")}";
  }
  
}
