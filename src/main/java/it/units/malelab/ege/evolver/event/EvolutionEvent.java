/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.Evolver;

/**
 *
 * @author eric
 */
public class EvolutionEvent<G extends Genotype, T> {
  
  private final int generation;
  private final Evolver evolver;

  public EvolutionEvent(int generation, Evolver<G, T> evolver) {
    this.generation = generation;
    this.evolver = evolver;
  }

  public int getGeneration() {
    return generation;
  }

  public Evolver<G, T> getEvolver() {
    return evolver;
  }
  
}
