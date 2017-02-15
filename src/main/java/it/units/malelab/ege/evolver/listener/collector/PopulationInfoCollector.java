/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener.collector;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public interface PopulationInfoCollector<G extends Genotype, T> {
  
  public Map<String, String> getFormattedNames();
  public Map<String, Object> collect(List<Individual<G, T>> population);
  
}
