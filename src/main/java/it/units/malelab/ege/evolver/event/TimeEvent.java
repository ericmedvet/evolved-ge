/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.Evolver;
import java.util.Map;

/**
 *
 * @author eric
 */
public class TimeEvent<G extends Genotype, T> extends EvolutionEvent<G, T> {
  
  private final long elapsedNanos;

  public TimeEvent(long elapsedNanos, int generation, Evolver<G, T> evolver, Map<String, Object> data) {
    super(generation, evolver, data);
    this.elapsedNanos = elapsedNanos;
  }

  public long getElapsedNanos() {
    return elapsedNanos;
  }
  
}
