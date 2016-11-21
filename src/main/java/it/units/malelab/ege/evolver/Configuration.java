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
import java.util.LinkedHashMap;
import java.util.Map;

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
  private Map<GeneticOperator<G>, Double> operators;
  private FitnessComputer<T> fitnessComputer;
  private Selector parentSelector;
  private Selector survivalSelector;  
  private int offspringSize;
  private boolean overlapping;
    
  public Configuration<G, T> copy() {
    Configuration<G, T> copy = new Configuration<>();
    copy.populationSize = populationSize;
    copy.numberOfGenerations = numberOfGenerations;
    copy.populationInitializer = populationInitializer;
    copy.initGenotypeValidator = initGenotypeValidator;
    copy.mapper = mapper;
    copy.operators = new LinkedHashMap<>();
    copy.operators.putAll(operators);
    copy.fitnessComputer = fitnessComputer;
    copy.survivalSelector = survivalSelector;
    copy.parentSelector = parentSelector;
    copy.offspringSize = offspringSize;
    copy.overlapping = overlapping;
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

  public Configuration<G, T> fitnessComputer(FitnessComputer<T> fitnessComputer) {
    this.fitnessComputer = fitnessComputer;
    return this;
  }

  public Selector getParentSelector() {
    return parentSelector;
  }

  public Selector getSurvivalSelector() {
    return survivalSelector;
  }

  public int getOffspringSize() {
    return offspringSize;
  }

  public Configuration<G, T> parentSelector(Selector parentSelector) {
    this.parentSelector = parentSelector;
    return this;
  }

  public Configuration<G, T> survivalSelector(Selector survivalSelector) {
    this.survivalSelector = survivalSelector;
    return this;
  }

  public Configuration<G, T> offspringSize(int offspringSize) {
    this.offspringSize = offspringSize;
    return this;
  }

  public Map<GeneticOperator<G>, Double> getOperators() {
    return operators;
  }

  public Configuration<G, T> operators(Map<GeneticOperator<G>, Double> operators) {
    this.operators = operators;
    return this;
  }    

  public Configuration<G, T> operator(GeneticOperator<G> operator, double rate) {
    if (operators==null) {
      operators = new LinkedHashMap<>();
    }
    operators.put(operator, rate);
    return this;
  }

  public boolean isOverlapping() {
    return overlapping;
  }

  public Configuration<G, T> overlapping(boolean overlapping) {
    this.overlapping = overlapping;
    return this;
  }
  
  
  
}
