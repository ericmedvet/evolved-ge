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

  private int populationSize;
  private int numberOfGenerations;
  private PopulationInitializer<G> populationInitializer;
  private GenotypeValidator<G> initGenotypeValidator;
  private Mapper<G, T> mapper;
  private List<GeneticOperatorConfiguration<G>> operators;
  private FitnessComputer<T> fitnessComputer;

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
  
  public Configuration<G, T> copy() {
    Configuration<G, T> copy = new Configuration<>();
    copy.populationSize = populationSize;
    copy.numberOfGenerations = numberOfGenerations;
    copy.populationInitializer = populationInitializer;
    copy.initGenotypeValidator = initGenotypeValidator;
    copy.mapper = mapper;
    copy.operators = operators;
    copy.fitnessComputer = fitnessComputer;
    return copy;
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

  public Configuration<G, T> populationSize(int populationSize) {
    this.populationSize = populationSize;
    return this;
  }

  public Configuration<G, T> numberOfGenerations(int numberOfGenerations) {
    this.numberOfGenerations = numberOfGenerations;
    return this;
  }

  public Configuration<G, T> populationInitializer(PopulationInitializer<G> populationInitializer) {
    this.populationInitializer = populationInitializer;
    return this;
  }

  public Configuration<G, T> initGenotypeValidator(GenotypeValidator<G> initGenotypeValidator) {
    this.initGenotypeValidator = initGenotypeValidator;
    return this;
  }

  public Configuration<G, T> mapper(Mapper<G, T> mapper) {
    this.mapper = mapper;
    return this;
  }

  public Configuration<G, T> operators(List<GeneticOperatorConfiguration<G>> operators) {
    this.operators = operators;
    return this;
  }

  public Configuration<G, T> fitnessComputer(FitnessComputer<T> fitnessComputer) {
    this.fitnessComputer = fitnessComputer;
    return this;
  }

}
