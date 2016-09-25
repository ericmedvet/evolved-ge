/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.Individual;

/**
 *
 * @author eric
 */
public class FitnessComputationEvent<T> extends TimeEvent<T> {
  
  private final Individual<T> individual;

  public FitnessComputationEvent(Individual<T> individual, long elapsedNanos, int generation, Evolver<T> evolver) {
    super(elapsedNanos, generation, evolver);
    this.individual = individual;
  }

  public Individual<T> getIndividual() {
    return individual;
  }
  
}
