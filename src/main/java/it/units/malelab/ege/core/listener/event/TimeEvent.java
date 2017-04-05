/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.event;

import it.units.malelab.ege.core.Evolver;
import it.units.malelab.ege.core.fitness.Fitness;
import java.util.Map;

/**
 *
 * @author eric
 */
public class TimeEvent<T, F extends Fitness> extends EvolutionEvent<T, F> {
  
  private final long elapsedNanos;

  public TimeEvent(long elapsedNanos, int generation, Evolver<T, F> evolver, Map<String, Object> data) {
    super(generation, evolver, data);
    this.elapsedNanos = elapsedNanos;
  }

  public long getElapsedNanos() {
    return elapsedNanos;
  }

}
