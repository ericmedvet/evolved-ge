/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.ge.genotype.Genotype;
import it.units.malelab.ege.evolver.Evolver;
import java.util.Map;

/**
 *
 * @author eric
 */
public class EvolutionEvent<G extends Genotype, T> {
  
  private final int generation;
  private final Evolver evolver;
  private final Map<String, Object> data;

  public EvolutionEvent(int generation, Evolver<G, T> evolver, Map<String, Object> data) {
    this.generation = generation;
    this.evolver = evolver;
    this.data = data;
  }

  public int getGeneration() {
    return generation;
  }

  public Evolver<G, T> getEvolver() {
    return evolver;
  }

  public Map<String, Object> getData() {
    return data;
  }
  
}
