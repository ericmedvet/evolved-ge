/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener;

import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.event.EvolutionEvent;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author eric
 */
public abstract class AbstractListener<T, F extends Fitness> implements EvolverListener<T, F> {
  
  private final Set<Class<? extends EvolutionEvent>> eventClasses;

  public AbstractListener(Class<? extends EvolutionEvent>... localEventClasses) {
    eventClasses = new LinkedHashSet<>();
    for (Class<? extends EvolutionEvent> eventClass : localEventClasses) {
      eventClasses.add(eventClass);
    }
  }

  @Override
  public Set<Class<? extends EvolutionEvent>> getEventClasses() {
    return eventClasses;
  }    
  
}
