/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver;

import it.units.malelab.ege.problems.BenchmarkProblems;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.initializer.PopulationInitializer;
import it.units.malelab.ege.evolver.validator.GenotypeValidator;
import it.units.malelab.ege.evolver.fitness.FitnessComputer;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.selector.Selector;
import it.units.malelab.ege.mapper.Mapper;
import it.units.malelab.ege.evolver.operator.GeneticOperator;
import it.units.malelab.ege.evolver.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.evolver.operator.ProbabilisticMutation;
import it.units.malelab.ege.evolver.selector.Best;
import it.units.malelab.ege.evolver.selector.IndividualComparator;
import it.units.malelab.ege.evolver.selector.Tournament;
import it.units.malelab.ege.evolver.validator.AnyValidator;
import it.units.malelab.ege.mapper.StandardGEMapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author eric
 */
public class StandardConfiguration<G extends Genotype, T> {

  private int populationSize;
  private int numberOfGenerations;
  private PopulationInitializer<G> populationInitializer;
  private GenotypeValidator<G> initGenotypeValidator;
  private Mapper<G, T> mapper;
  private Map<GeneticOperator<G>, Double> operators;
  private FitnessComputer<T> fitnessComputer;
  private Selector<Individual<G, T>> parentSelector;
  private Selector<Individual<G, T>> unsurvivalSelector;  
  private int offspringSize;
  private boolean overlapping;
  
  public static StandardConfiguration<BitsGenotype, String> createDefault(BenchmarkProblems.Problem problem, Random random) {
    StandardConfiguration<BitsGenotype, String> configuration = new StandardConfiguration<>();
    configuration
            .populationSize(500)
            .offspringSize(1)
            .overlapping(true)
            .numberOfGenerations(50)
            .populationInitializer(new RandomInitializer<>(random, new BitsGenotypeFactory(256)))
            .initGenotypeValidator(new AnyValidator<BitsGenotype>())
            .mapper(new StandardGEMapper<>(8, 5, problem.getGrammar()))
            .parentSelector(new Tournament(5, random, new IndividualComparator(IndividualComparator.Attribute.FITNESS)))
            .unsurvivalSelector(new Best(Collections.reverseOrder(new IndividualComparator(IndividualComparator.Attribute.FITNESS))))
            .operator(new LengthPreservingTwoPointsCrossover(random), 0.8d)
            .operator(new ProbabilisticMutation(random, 0.01), 0.2d)
            .fitnessComputer(problem.getFitnessComputer());
    return configuration;
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

  public StandardConfiguration<G, T> populationSize(int populationSize) {
    this.populationSize = populationSize;
    return this;
  }

  public StandardConfiguration<G, T> numberOfGenerations(int numberOfGenerations) {
    this.numberOfGenerations = numberOfGenerations;
    return this;
  }

  public StandardConfiguration<G, T> populationInitializer(PopulationInitializer<G> populationInitializer) {
    this.populationInitializer = populationInitializer;
    return this;
  }

  public StandardConfiguration<G, T> initGenotypeValidator(GenotypeValidator<G> initGenotypeValidator) {
    this.initGenotypeValidator = initGenotypeValidator;
    return this;
  }

  public StandardConfiguration<G, T> mapper(Mapper<G, T> mapper) {
    this.mapper = mapper;
    return this;
  }

  public StandardConfiguration<G, T> fitnessComputer(FitnessComputer<T> fitnessComputer) {
    this.fitnessComputer = fitnessComputer;
    return this;
  }

  public Selector<Individual<G, T>> getParentSelector() {
    return parentSelector;
  }

  public Selector<Individual<G, T>> getUnsurvivalSelector() {
    return unsurvivalSelector;
  }

  public int getOffspringSize() {
    return offspringSize;
  }

  public StandardConfiguration<G, T> parentSelector(Selector<Individual<G, T>> parentSelector) {
    this.parentSelector = parentSelector;
    return this;
  }

  public StandardConfiguration<G, T> unsurvivalSelector(Selector<Individual<G, T>> unsurvivalSelector) {
    this.unsurvivalSelector = unsurvivalSelector;
    return this;
  }

  public StandardConfiguration<G, T> offspringSize(int offspringSize) {
    this.offspringSize = offspringSize;
    return this;
  }

  public Map<GeneticOperator<G>, Double> getOperators() {
    return operators;
  }

  public StandardConfiguration<G, T> operators(Map<GeneticOperator<G>, Double> operators) {
    this.operators = operators;
    return this;
  }    

  public StandardConfiguration<G, T> operator(GeneticOperator<G> operator, double rate) {
    if (operators==null) {
      operators = new LinkedHashMap<>();
    }
    operators.put(operator, rate);
    return this;
  }

  public boolean isOverlapping() {
    return overlapping;
  }

  public StandardConfiguration<G, T> overlapping(boolean overlapping) {
    this.overlapping = overlapping;
    return this;
  }
  
  
  
}
