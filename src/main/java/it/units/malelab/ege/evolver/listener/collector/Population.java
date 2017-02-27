/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener.collector;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class Population<G extends Genotype, T> implements PopulationInfoCollector<G, T>{

  @Override
  public Map<String, Object> collect(List<Individual<G, T>> population) {
    Map<String, Object> indexes = new LinkedHashMap<>();
    indexes.put("population.size", population.size());
    return indexes;
  }  
  
  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("population.size", "%5d");
    return formattedNames;
  }  
    
}
