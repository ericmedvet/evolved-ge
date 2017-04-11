/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver;

import it.units.malelab.ege.core.evolver.Configuration;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.selector.Selector;
import it.units.malelab.ege.core.Validator;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.ranker.Ranker;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import java.util.Map;

/**
 *
 * @author eric
 */
public class StandardConfiguration<G, T, F extends Fitness> implements Configuration<G, T, F> {

  private final int populationSize;
  private final int numberOfGenerations;
  private final PopulationInitializer<G> populationInitializer;
  private final Validator<G> initGenotypeValidator;
  private final Mapper<G, T> mapper;
  private final Map<GeneticOperator<G>, Double> operators;
  private final Ranker<Individual<G, T, F>> ranker;
  private final Selector<Individual<G, T, F>> parentSelector;
  private final Selector<Individual<G, T, F>> unsurvivalSelector;
  private final int offspringSize;
  private final boolean overlapping;
  private final Problem<T, F> problem;

  public StandardConfiguration(int populationSize, int numberOfGenerations, PopulationInitializer<G> populationInitializer, Validator<G> initGenotypeValidator, Mapper<G, T> mapper, Map<GeneticOperator<G>, Double> operators, Ranker<Individual<G, T, F>> ranker, Selector<Individual<G, T, F>> parentSelector, Selector<Individual<G, T, F>> unsurvivalSelector, int offspringSize, boolean overlapping, Problem<T, F> problem) {
    this.populationSize = populationSize;
    this.numberOfGenerations = numberOfGenerations;
    this.populationInitializer = populationInitializer;
    this.initGenotypeValidator = initGenotypeValidator;
    this.mapper = mapper;
    this.operators = operators;
    this.ranker = ranker;
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

  public Validator<G> getInitGenotypeValidator() {
    return initGenotypeValidator;
  }

  public Mapper<G, T> getMapper() {
    return mapper;
  }

  public Map<GeneticOperator<G>, Double> getOperators() {
    return operators;
  }

  public Ranker<Individual<G, T, F>> getRanker() {
    return ranker;
  }

  public Selector<Individual<G, T, F>> getParentSelector() {
    return parentSelector;
  }

  public Selector<Individual<G, T, F>> getUnsurvivalSelector() {
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

  @Override
  public String toString() {
    return "StandardConfiguration{" + "populationSize=" + populationSize + ", numberOfGenerations=" + numberOfGenerations + ", populationInitializer=" + populationInitializer + ", initGenotypeValidator=" + initGenotypeValidator + ", mapper=" + mapper + ", operators=" + operators + ", ranker=" + ranker + ", parentSelector=" + parentSelector + ", unsurvivalSelector=" + unsurvivalSelector + ", offspringSize=" + offspringSize + ", overlapping=" + overlapping + ", problem=" + problem + '}';
  }

}
