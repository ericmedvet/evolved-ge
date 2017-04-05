/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.event;

import it.units.malelab.ege.core.Evolver;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import java.util.Map;

/**
 *
 * @author eric
 */
public class BirthEvent<T, F extends Fitness> extends TimeEvent<T, F> {
  
  private final Individual<T, F> individual;

  public BirthEvent(Individual<T, F> individual, long elapsedNanos, int generation, Evolver<T, F> evolver, Map<String, Object> data) {
    super(elapsedNanos, generation, evolver, data);
    this.individual = individual;
  }

  public Individual<T, F> getIndividual() {
    return individual;
  }
  
}
