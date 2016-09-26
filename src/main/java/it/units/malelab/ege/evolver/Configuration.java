/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver;

import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.initializer.PopulationInitializer;
import it.units.malelab.ege.evolver.validator.GenotypeValidator;
import it.units.malelab.ege.evolver.fitness.FitnessComputer;
import it.units.malelab.ege.evolver.selector.Selector;
import it.units.malelab.ege.mapper.Mapper;
import it.units.malelab.ege.evolver.operator.GeneticOperator;
import java.util.List;

/**
 *
 * @author eric
 */
public class Configuration<G extends Genotype, T> {
  
  private final int populationSize;
  private final int numberOfGenerations;
  private final PopulationInitializer<G> populationInitializer;
  private final GenotypeValidator<G> initGenotypeValidator;
  private final Mapper<G, T> mapper;
  private final List<GeneticOperatorConfiguration<G>> operators;
  private final FitnessComputer<T> fitnessComputer;
  
  public static class GeneticOperatorConfiguration<G extends Genotype> {
    private final GeneticOperator<G> operator;
    private final Selector selector;
    private final double rate;

    public GeneticOperatorConfiguration(GeneticOperator<G> operator, Selector selector, double rate) {
      this.operator = operator;
      this.selector = selector;
      this.rate = rate;
    }

    public GeneticOperator<G> getOperator() {
      return operator;
    }

    public Selector getSelector() {
      return selector;
    }

    public double getRate() {
      return rate;
    }
    
  }

  public Configuration(int populationSize, int numberOfGenerations, PopulationInitializer<G> populationInitializer, GenotypeValidator<G> initGenotypeValidator, Mapper<G, T> mapper, List<GeneticOperatorConfiguration<G>> operators, FitnessComputer<T> fitnessComputer) {
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

  public PopulationInitializer<G> getPopulationInitializer() {
    return populationInitializer;
  }

  public GenotypeValidator<G> getInitGenotypeValidator() {
    return initGenotypeValidator;
  }

  public Mapper<G, T> getMapper() {
    return mapper;
  }

  public List<GeneticOperatorConfiguration<G>> getOperators() {
    return operators;
  }

  public FitnessComputer<T> getFitnessComputer() {
    return fitnessComputer;
  }

}
