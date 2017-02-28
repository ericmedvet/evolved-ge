/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener.collector;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.fitness.Fitness;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class Population<G extends Genotype, T> implements PopulationInfoCollector<G, T> {
  
  private final String fitnessFormat;

  public Population(String fitnessFormat) {
    this.fitnessFormat = fitnessFormat;
  }  

  @Override
  public Map<String, Object> collect(List<Individual<G, T>> population) {
    Map<String, Object> indexes = new LinkedHashMap<>();
    indexes.put("population.size", population.size());
    List<Fitness> fitnesses = new ArrayList<>(population.size());
    List<Double> values = new ArrayList<>();
    double sum = 0;
    for (Individual<G, T> individual : population) {
      fitnesses.add(individual.getFitness());
      if (individual.getFitness().getValue() instanceof Number) {
        double value = ((Number) individual.getFitness().getValue()).doubleValue();
        values.add(value);
        sum = sum + value;
      }
    }
    Collections.sort(fitnesses);
    indexes.put("population.fitness.median", fitnesses.get(fitnesses.size() / 2).getValue());
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
