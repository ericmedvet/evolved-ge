/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.event;

import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.fitness.Fitness;
import java.util.Map;

/**
 *
 * @author eric
 */
public class EvolutionEvent<G, T, F extends Fitness> {
  
  private final int generation;
  private final Evolver<G, T, F> evolver;
  private final Map<String, Object> data;

  public EvolutionEvent(int generation, Evolver<G, T, F> evolver, Map<String, Object> data) {
    this.generation = generation;
    this.evolver = evolver;
    this.data = data;
  }

  public int getGeneration() {
    return generation;
  }

  public Evolver<G, T, F> getEvolver() {
    return evolver;
  }

  public Map<String, Object> getData() {
    return data;
  }

}
