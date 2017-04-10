/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.collector;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public interface PopulationInfoCollector<T, F extends Fitness> {
  
  public Map<String, String> getFormattedNames();
  public Map<String, Object> collect(List<List<Individual<T, F>>> rankedPopulation);
  
}
