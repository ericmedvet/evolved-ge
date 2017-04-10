/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.event;

import it.units.malelab.ege.core.Evolver;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import java.util.List;
import java.util.Map;


/**
 *
 * @author eric
 */
public class EvolutionEndEvent<T, F extends Fitness> extends GenerationEvent<T, F> {

  public EvolutionEndEvent(List<List<Individual<T, F>>> rankedPopulation, int generation, Evolver<T, F> evolver, Map<String, Object> data) {
    super(rankedPopulation, generation, evolver, data);
  }

}
