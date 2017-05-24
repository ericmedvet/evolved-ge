/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.collector;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Sequence;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class Population<G extends Sequence, T, F extends Fitness> implements Collector<G, T, F> {

  @Override
  public Map<String, Object> collect(GenerationEvent<G, T, F> event) {
    List<List<Individual<G, T, F>>> rankedPopulation = new ArrayList<>(event.getRankedPopulation());
    Map<String, Object> indexes = new LinkedHashMap<>();
    double count = 0;
    double genoLengthSum = 0;
    double phenoLengthSum = 0;
    double birthDateSum = 0;
    for (List<Individual<G, T, F>> rank : rankedPopulation) {
      for (Individual<G, T, F> individual : rank) {
        count = count+1;
        genoLengthSum = genoLengthSum+individual.getGenotype().length();
        phenoLengthSum = phenoLengthSum+individual.getPhenotype().length();
        birthDateSum = birthDateSum+individual.getBirthDate();
      }
    }
    indexes.put("population.genotype.length.average", (int)Math.round(genoLengthSum / count));
    indexes.put("population.phenotype.length.average", (int)Math.round(phenoLengthSum / count));
    indexes.put("population.birthDate.average", (int)Math.round(birthDateSum / count));
    indexes.put("population.size", (int)count);
    indexes.put("population.ranks", rankedPopulation.size());
    return indexes;
  }

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("population.size", "%5d");
    formattedNames.put("population.ranks", "%3d");
    formattedNames.put("population.genotype.length.average", "%5d");
    formattedNames.put("population.phenotype.length.average", "%4d");
    formattedNames.put("population.birthDate.average", "%3d");
    return formattedNames;
  }

}
