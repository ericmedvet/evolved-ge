/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core;

import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.fitness.FitnessComputer;

/**
 *
 * @author eric
 */
public class Problem<T, F extends Fitness> {
  
  private final Grammar<T> grammar;
  private final FitnessComputer<T, F> learningFitnessComputer;
  private final FitnessComputer<T, F> testingFitnessComputer;
  private final PhenotypePrinter<T> phenotypePrinter;

  public Problem(Grammar<T> grammar, FitnessComputer<T, F> learningFitnessComputer, FitnessComputer<T, F> testingFitnessComputer, PhenotypePrinter<T> phenotypePrinter) {
    this.grammar = grammar;
    this.learningFitnessComputer = learningFitnessComputer;
    this.testingFitnessComputer = testingFitnessComputer;
    this.phenotypePrinter = phenotypePrinter;
  }

  public Grammar<T> getGrammar() {
    return grammar;
  }

  public FitnessComputer<T, F> getLearningFitnessComputer() {
    return learningFitnessComputer;
  }

  public FitnessComputer<T, F> getTestingFitnessComputer() {
    return testingFitnessComputer;
  }

  public PhenotypePrinter<T> getPhenotypePrinter() {
    return phenotypePrinter;
  }

  
}
