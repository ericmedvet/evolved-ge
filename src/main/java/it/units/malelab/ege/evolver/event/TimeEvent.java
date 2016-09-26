/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.evolver.Evolver;

/**
 *
 * @author eric
 */
public class TimeEvent<G extends Genotype, T> extends EvolutionEvent<G, T> {
  
  private final long elapsedNanos;

  public TimeEvent(long elapsedNanos, int generation, Evolver<G, T> evolver) {
    super(generation, evolver);
    this.elapsedNanos = elapsedNanos;
  }

  public long getElapsedNanos() {
    return elapsedNanos;
  }
  
}
