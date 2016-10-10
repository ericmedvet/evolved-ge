/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.Individual;
import java.util.List;

/**
 *
 * @author eric
 */
public class EvolutionEndEvent<G extends Genotype, T> extends GenerationEvent<G, T> {

  public EvolutionEndEvent(List<Individual<G, T>> population, int generation, Evolver<G, T> evolver) {
    super(population, generation, evolver);
  }
  
}