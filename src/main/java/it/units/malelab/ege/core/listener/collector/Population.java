/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.collector;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.listener.collector.PopulationInfoCollector;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.fitness.NumericFitness;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class Population<T, F extends NumericFitness> implements PopulationInfoCollector<T, F> {
  
  private final String fitnessFormat;

  public Population(String fitnessFormat) {
    this.fitnessFormat = fitnessFormat;
  }  

  @Override
  public Map<String, Object> collect(List<Individual<T, F>> population) {
    Map<String, Object> indexes = new LinkedHashMap<>();
    indexes.put("population.size", population.size());
    List<Double> values = new ArrayList<>(population.size());
    double sum = 0;
    for (Individual<T, F> individual : population) {
      values.add(individual.getFitness().getValue());
      sum = sum + individual.getFitness().getValue();
    }
    Collections.sort(values);
    indexes.put("population.fitness.median", values.get(values.size() / 2));
    if (!values.isEmpty()) {
      indexes.put("population.fitness.average", sum/values.size());
    }
    return indexes;
  }

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("population.size", "%5d");
    formattedNames.put("population.fitness.median", fitnessFormat);
    formattedNames.put("population.fitness.average", fitnessFormat);
    return formattedNames;
  }

}
