/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.event;

import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import java.util.Map;

/**
 *
 * @author eric
 */
public class BirthEvent<G, T, F extends Fitness> extends TimeEvent<G, T, F> {
  
  private final Individual<G, T, F> individual;

  public BirthEvent(Individual<G, T, F> individual, long elapsedNanos, int generation, Evolver<G, T, F> evolver, Map<String, Object> data) {
    super(elapsedNanos, generation, evolver, data);
    this.individual = individual;
  }

  public Individual<G, T, F> getIndividual() {
    return individual;
  }
  
}
