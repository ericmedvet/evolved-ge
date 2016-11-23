/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.Individual;
import java.util.Map;

/**
 *
 * @author eric
 */
public class BirthEvent<G extends  Genotype, T> extends TimeEvent<G, T> {
  
  private final Individual<G, T> individual;

  public BirthEvent(Individual<G, T> individual, long elapsedNanos, int generation, Evolver<G, T> evolver, Map<String, Object> data) {
    super(elapsedNanos, generation, evolver, data);
    this.individual = individual;
  }

  public Individual<G, T> getIndividual() {
    return individual;
  }
  
}
