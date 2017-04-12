/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.collector;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.PhenotypePrinter;
import it.units.malelab.ege.core.fitness.Fitness;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class BestPrinter<G, T, F extends Fitness> implements PopulationInfoCollector<G, T, F>{
  
  private final PhenotypePrinter<T> phenotypePrinter;
  private final String format;

  public BestPrinter(PhenotypePrinter<T> phenotypePrinter, String format) {
    this.phenotypePrinter = phenotypePrinter;
    this.format = format;
  }
  
  @Override
  public Map<String, Object> collect(List<List<Individual<G, T, F>>> rankedPopulation) {
    Individual<G, T, F> best = rankedPopulation.get(0).get(0);
    return (Map)Collections.singletonMap("best.phenotype", phenotypePrinter.toString(best.getPhenotype()));
  }  

  @Override
  public Map<String, String> getFormattedNames() {
    return Collections.singletonMap("best.phenotype", format);
  }  
    
}
