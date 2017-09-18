/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.collector;

import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author eric
 */
public interface Collector<G, T, F extends Fitness> extends Serializable {
  
  public Map<String, String> getFormattedNames();
  public Map<String, Object> collect(GenerationEvent<G, T, F> generationEvent);  
  
}
