/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.Individual;

/**
 *
 * @author eric
 */
public class BirthEvent<G extends  Genotype, T> extends TimeEvent<G, T> {
  
  private final Individual<G, T> individual;

  public BirthEvent(Individual<G, T> individual, long elapsedNanos, int generation, Evolver<G, T> evolver) {
    super(elapsedNanos, generation, evolver);
    this.individual = individual;
  }

  public Individual<G, T> getIndividual() {
    return individual;
  }
  
}
