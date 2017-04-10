/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.listener.collector;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.collector.Diversity;
import it.units.malelab.ege.ge.GEIndividual;
import it.units.malelab.ege.ge.genotype.Genotype;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eric
 */
public class GEDiversity<T, F extends Fitness> extends Diversity<T, F> {

  @Override
  public Map<String, Object> collect(List<List<Individual<T, F>>> rankedPopulation) {
    Map<String, Object> indexes = super.collect(rankedPopulation);
    Set<Genotype> genotypes = new HashSet<>();
    double count = 0;
    for (List<Individual<T, F>> rank : rankedPopulation) {
      for (Individual<T, F> individual : rank) {
        if (individual instanceof GEIndividual) {
          genotypes.add(((GEIndividual) individual).getGenotype());
          count = count+1;
        }
      }
    }
    indexes.put("diversity.genotype", (double) genotypes.size() / count);
    return indexes;
  }

  @Override
  public Map<String, String> getFormattedNames() {
    Map<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("diversity.genotype", "%4.2f");
    formattedNames.putAll(super.getFormattedNames());
    return formattedNames;
  }

}
