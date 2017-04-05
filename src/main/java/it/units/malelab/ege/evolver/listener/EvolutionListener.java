/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.ge.genotype.Genotype;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import java.util.Set;

/**
 *
 * @author eric
 */
public interface EvolutionListener<G extends Genotype, T> {
  
  public void listen(EvolutionEvent<G, T> event);
  public Set<Class<? extends EvolutionEvent>> getEventClasses();
  
}
