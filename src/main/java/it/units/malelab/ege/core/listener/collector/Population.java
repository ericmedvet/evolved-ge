/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.collector;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Sequence;
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
public class Population<G extends Sequence, T, F extends NumericFitness> implements PopulationInfoCollector<G, T, F> {

  private final String fitnessFormat;

  public Population(String fitnessFormat) {
    this.fitnessFormat = fitnessFormat;
  }

  @Override
  public Map<String, Object> collect(List<List<Individual<G, T, F>>> rankedPopulation) {
    Map<String, Object> indexes = new LinkedHashMap<>();
    indexes.put("population.size", rankedPopulation.size());
    List<Double> values = new ArrayList<>(rankedPopulation.size());
    double fitnessSum = 0;
    double genoLengthSum = 0;
    double phenoLengthSum = 0;
    double birthDateSum = 0;
    for (List<Individual<G, T, F>> rank : rankedPopulation) {
      for (Individual<G, T, F> individual : rank) {
        values.add(individual.getFitness().getValue());
        fitnessSum = fitnessSum + individual.getFitness().getValue();
        genoLengthSum = genoLengthSum+individual.getGenotype().length();
        phenoLengthSum = phenoLengthSum+individual.getPhenotype().length();
        birthDateSum = birthDateSum+individual.getBirthDate();
      }
    }
    Collections.sort(values);
    indexes.put("population.fitness.median", values.get(values.size() / 2));
    if (!values.isEmpty()) {
      indexes.put("population.genotype.length.average", (int)Math.round(genoLengthSum / (double)values.size()));
      indexes.put("population.phenotype.length.average", (int)Math.round(phenoLengthSum / (double)values.size()));
      indexes.put("population.birthDate.average", (int)Math.round(birthDateSum / (double)values.size()));
      indexes.put("population.fitness.average", fitnessSum / (double)values.size());
    }
    return indexes;
  }

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("population.size", "%5d");
    formattedNames.put("population.genotype.length.average", "%5d");
    formattedNames.put("population.phenotype.length.average", "%4d");
    formattedNames.put("population.fitness.average", fitnessFormat);
    formattedNames.put("population.fitness.median", fitnessFormat);
    formattedNames.put("population.birthDate.average", "%3d");
    return formattedNames;
  }

}
