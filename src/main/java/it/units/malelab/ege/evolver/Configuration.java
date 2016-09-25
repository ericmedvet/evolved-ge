/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver;

import it.units.malelab.ege.evolver.initializer.PopulationInitializer;
import it.units.malelab.ege.evolver.validator.GenotypeValidator;
import it.units.malelab.ege.evolver.fitness.FitnessComputer;
import it.units.malelab.ege.evolver.selector.Selector;
import it.units.malelab.ege.mapper.Mapper;
import it.units.malelab.ege.operator.GeneticOperator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class Configuration<T> {
  
  private final int populationSize;
  private final int numberOfGenerations;
  private final PopulationInitializer populationInitializer;
  private final GenotypeValidator initGenotypeValidator;
  private final Mapper<T> mapper;
  private final List<GeneticOperatorConfiguration> operators;
  private final FitnessComputer<T> fitnessComputer;
  
  public static class GeneticOperatorConfiguration {
    private final GeneticOperator operator;
    private final Selector selector;
    private final double rate;

    public GeneticOperatorConfiguration(GeneticOperator operator, Selector selector, double rate) {
      this.operator = operator;
      this.selector = selector;
      this.rate = rate;
    }

    public GeneticOperator getOperator() {
      return operator;
    }

    public Selector getSelector() {
      return selector;
    }

    public double getRate() {
      return rate;
    }
    
  }

  public Configuration(int populationSize, int numberOfGenerations, PopulationInitializer populationInitializer, GenotypeValidator initGenotypeValidator, Mapper<T> mapper, List<GeneticOperatorConfiguration> operators, FitnessComputer<T> fitnessComputer) {
    this.populationSize = populationSize;
    this.numberOfGenerations = numberOfGenerations;
    this.populationInitializer = populationInitializer;
    this.initGenotypeValidator = initGenotypeValidator;
    this.mapper = mapper;
    this.operators = operators;
    this.fitnessComputer = fitnessComputer;
  }

  public int getPopulationSize() {
    return populationSize;
  }

  public int getNumberOfGenerations() {
    return numberOfGenerations;
  }

  public PopulationInitializer getPopulationInitializer() {
    return populationInitializer;
  }

  public GenotypeValidator getInitGenotypeValidator() {
    return initGenotypeValidator;
  }

  public Mapper<T> getMapper() {
    return mapper;
  }

  public List<GeneticOperatorConfiguration> getOperators() {
    return operators;
  }

  public FitnessComputer<T> getFitnessComputer() {
    return fitnessComputer;
  }
  
}
