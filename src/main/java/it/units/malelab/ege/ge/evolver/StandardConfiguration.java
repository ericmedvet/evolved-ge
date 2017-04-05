/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.evolver;

import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.selector.Selector;
import it.units.malelab.ege.ge.genotype.Genotype;
import it.units.malelab.ege.ge.genotype.validator.GenotypeValidator;
import it.units.malelab.ege.ge.mapper.Mapper;
import it.units.malelab.ege.ge.operator.GeneticOperator;
import it.units.malelab.ege.ge.GEConfiguration;
import it.units.malelab.ege.ge.GEIndividual;
import it.units.malelab.ege.ge.genotype.initializer.PopulationInitializer;
import java.util.Map;

/**
 *
 * @author eric
 */
public class StandardConfiguration<G extends Genotype, T, F extends Fitness> implements GEConfiguration<G, T, F> {

  private final int populationSize;
  private final int numberOfGenerations;
  private final PopulationInitializer<G> populationInitializer;
  private final GenotypeValidator<G> initGenotypeValidator;
  private final Mapper<G, T> mapper;
  private final Map<GeneticOperator<G>, Double> operators;
  private final Selector<GEIndividual<G, T, F>> parentSelector;
  private final Selector<GEIndividual<G, T, F>> unsurvivalSelector;  
  private final int offspringSize;
  private final boolean overlapping;
  private final Problem<T, F> problem;

  public StandardConfiguration(int populationSize, int numberOfGenerations, PopulationInitializer<G> populationInitializer, GenotypeValidator<G> initGenotypeValidator, Mapper<G, T> mapper, Map<GeneticOperator<G>, Double> operators, Selector<GEIndividual<G, T, F>> parentSelector, Selector<GEIndividual<G, T, F>> unsurvivalSelector, int offspringSize, boolean overlapping, Problem<T, F> problem) {
    this.populationSize = populationSize;
    this.numberOfGenerations = numberOfGenerations;
    this.populationInitializer = populationInitializer;
    this.initGenotypeValidator = initGenotypeValidator;
    this.mapper = mapper;
    this.operators = operators;
    this.parentSelector = parentSelector;
    this.unsurvivalSelector = unsurvivalSelector;
    this.offspringSize = offspringSize;
    this.overlapping = overlapping;
    this.problem = problem;
  }

  public int getPopulationSize() {
    return populationSize;
  }

  public int getNumberOfGenerations() {
    return numberOfGenerations;
  }

  public PopulationInitializer<G> getPopulationInitializer() {
    return populationInitializer;
  }

  public GenotypeValidator<G> getInitGenotypeValidator() {
    return initGenotypeValidator;
  }

  public Mapper<G, T> getMapper() {
    return mapper;
  }

  public Map<GeneticOperator<G>, Double> getOperators() {
    return operators;
  }

  public Selector<GEIndividual<G, T, F>> getParentSelector() {
    return parentSelector;
  }

  public Selector<GEIndividual<G, T, F>> getUnsurvivalSelector() {
    return unsurvivalSelector;
  }

  public int getOffspringSize() {
    return offspringSize;
  }

  public boolean isOverlapping() {
    return overlapping;
  }

  public Problem<T, F> getProblem() {
    return problem;
  }
  
}
