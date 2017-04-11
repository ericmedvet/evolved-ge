/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.event;

import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import java.util.List;
import java.util.Map;


/**
 *
 * @author eric
 */
public class EvolutionEndEvent<G, T, F extends Fitness> extends GenerationEvent<G, T, F> {

  public EvolutionEndEvent(List<List<Individual<G, T, F>>> rankedPopulation, int generation, Evolver<G, T, F> evolver, Map<String, Object> data) {
    super(rankedPopulation, generation, evolver, data);
  }

}
