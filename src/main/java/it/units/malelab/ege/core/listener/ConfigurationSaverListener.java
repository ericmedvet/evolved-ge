/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener;

import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.event.EvolutionEvent;
import it.units.malelab.ege.core.listener.event.EvolutionStartEvent;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eric
 */
public class ConfigurationSaverListener<T, F extends Fitness> extends AbstractListener<T, F> implements WithConstants {

  private final Map<String, Object> constants;
  private final PrintStream ps;

  public ConfigurationSaverListener(Map<String, Object> constants, PrintStream ps) {
    super((Class)EvolutionStartEvent.class);
    this.constants = constants;
    this.ps = ps;
  } 
  
  @Override
  public void listen(EvolutionEvent<T, F> event) {
    if (event instanceof EvolutionStartEvent) {
      ps.println(constants.hashCode());
      ps.println(constants);
      ps.println(event.getEvolver().getConfiguration());
      ps.println();
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
  
}
