/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener;

import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.event.EvolutionEvent;
import java.util.Set;

/**
 *
 * @author eric
 */
public interface EvolverListener<G, T, F extends Fitness> {
  
  public void listen(EvolutionEvent<G, T, F> event);
  public Set<Class<? extends EvolutionEvent>> getEventClasses();
  
}
