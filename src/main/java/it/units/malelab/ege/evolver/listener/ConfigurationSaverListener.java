/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eric
 */
public class ConfigurationSaverListener<G extends Genotype, T> implements EvolutionListener<G, T>, WithConstants {

  private final Set<Class<? extends EvolutionEvent>> eventClasses;
  private final Map<String, Object> constants;
  private final PrintStream ps;

  public ConfigurationSaverListener(Map<String, Object> constants, PrintStream ps) {
    eventClasses = new LinkedHashSet<>();
    eventClasses.add(GenerationEvent.class);
    this.constants = constants;
    this.ps = ps;
  } 
  
  @Override
  public void listen(EvolutionEvent<G, T> event) {
    int generation = ((GenerationEvent) event).getGeneration();
    if (generation==1) {
      ps.println(constants);
      ps.println(event.getEvolver().getConfiguration());
    }
  }
  
  @Override
  public void updateConstants(Map<String, Object> newConstants) {
    for (String key : constants.keySet()) {
      if (newConstants.containsKey(key)) {
        constants.put(key, newConstants.get(key));
      }
    }
  }

  @Override
  public Set<Class<? extends EvolutionEvent>> getEventClasses() {
    return eventClasses;
  }
  
}
