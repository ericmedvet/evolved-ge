/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.evolver.event.EvolutionEndEvent;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author eric
 */
public class ConfigurationSaverListener<G extends Genotype, T> implements EvolutionListener<G, T> {
  
  private final Set<Class<? extends EvolutionEvent>> eventClasses;
  
  private final String key;
  private final PrintStream ps;

  public ConfigurationSaverListener(String key, PrintStream ps) {
    eventClasses = new LinkedHashSet<>();
    eventClasses.add(EvolutionEndEvent.class);
    this.key = key;
    this.ps = ps;
  }

  @Override
  public void listen(EvolutionEvent<G, T> event) {
    ps.printf("Key: %s%n", key);
    try {
      //ps.println(BeanUtils.describe(event.getEvolver().getConfiguration()));
    } catch (Exception ex) {
      System.err.printf("Cannot write configuration: %s", ex.getMessage());
    }
  }

  @Override
  public Set<Class<? extends EvolutionEvent>> getEventClasses() {
    return eventClasses;
  }
  
}
