/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.Individual;
import java.util.List;

/**
 *
 * @author eric
 */
public class EvolutionEndEvent<T> extends GenerationEvent<T> {

  public EvolutionEndEvent(List<Individual<T>> population, int generation, Evolver<T> evolver) {
    super(population, generation, evolver);
  }
  
}
