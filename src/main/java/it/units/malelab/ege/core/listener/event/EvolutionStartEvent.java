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
public class EvolutionStartEvent<G, T, F extends Fitness> extends EvolutionEvent<G, T, F> {

  public EvolutionStartEvent(Evolver<G, T, F> evolver, Map<String, Object> data) {
    super(0, evolver, data);
  }

}
