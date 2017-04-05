/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener.collector;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.ge.genotype.Genotype;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class Best<G extends Genotype, T> implements PopulationInfoCollector<G, T>{
  
  private final String fitnessFormat;

  public Best(String fitnessFormat) {
    this.fitnessFormat = fitnessFormat;
  }    

  @Override
  public Map<String, Object> collect(List<Individual<G, T>> population) {
    Individual<G, T> best = population.get(0);
    for (Individual<G, T> individual : population) {
      if (individual.getFitness().compareTo(best.getFitness())<0) {
        best = individual;
      }
    }
    Map<String, Object> indexes = new LinkedHashMap<>();
    indexes.put("best.fitness", best.getFitness().getValue());
    indexes.put("best.phenotype.size", best.getPhenotype().size());
    indexes.put("best.phenotype.length", best.getPhenotype().leaves().size());
    indexes.put("best.phenotype.depth", best.getPhenotype().depth());
    indexes.put("best.genotype.size", best.getGenotype().size());
    indexes.put("best.birth", best.getBirthDate());
    indexes.put("best.ancestry.depth", getAncestryDepth(best));
    indexes.put("best.ancestry.size", getAncestrySize(best));
    return indexes;
  }  

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("best.fitness", fitnessFormat);
    formattedNames.put("best.phenotype.size", "%3d");
    formattedNames.put("best.phenotype.length", "%3d");
    formattedNames.put("best.phenotype.depth", "%2d");
    formattedNames.put("best.genotype.size", "%5d");
    formattedNames.put("best.birth", "%3d");
    formattedNames.put("best.ancestry.depth", "%2d");
    formattedNames.put("best.ancestry.size", "%5d");
    return formattedNames;
  }  
  
  private int getAncestrySize(Individual<G, T> individual) {
    int count = 1;
    for (Individual<G, T> parent : individual.getParents()) {
      count = count+getAncestrySize(parent);
    }
    return count;
  }

  private int getAncestryDepth(Individual<G, T> individual) {
    int count = 1;
    for (Individual<G, T> parent : individual.getParents()) {
      count = Math.max(count, getAncestryDepth(parent)+1);
    }
    return count;
  }
  
  
}
