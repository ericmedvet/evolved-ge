/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.collector;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public abstract class FirstBest<T, F extends Fitness> implements PopulationInfoCollector<T, F>{
  
  private final boolean ancestry;

  public FirstBest(boolean ancestry) {
    this.ancestry = ancestry;
  }    
  
  @Override
  public Map<String, Object> collect(List<Individual<T, F>> population) {
    Individual<T, F> best = null;
    for (Individual<T, F> individual : population) {
      if (individual.getRank()==0) {
        best = individual;
        break;
      }
    }
    Map<String, Object> indexes = new LinkedHashMap<>();
    for (Map.Entry<String, Object> fitnessEntry : getFitnessIndexes(best.getFitness()).entrySet()) {
      indexes.put(
              augmentFitnessName(fitnessEntry.getKey()),
              fitnessEntry.getValue());
    }
    indexes.put("best.phenotype.size", best.getPhenotype().size());
    indexes.put("best.phenotype.length", best.getPhenotype().leaves().size());
    indexes.put("best.phenotype.depth", best.getPhenotype().depth());
    indexes.put("best.birth", best.getBirthDate());
    if (ancestry) {
      indexes.put("best.ancestry.depth", getAncestryDepth(best));
      indexes.put("best.ancestry.size", getAncestrySize(best));
    }
    return indexes;
  }  

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    for (Map.Entry<String, String> fitnessEntry : getFitnessFormattedNames().entrySet()) {
      formattedNames.put(
              augmentFitnessName(fitnessEntry.getKey()),
              fitnessEntry.getValue());
    }
    formattedNames.put("best.phenotype.size", "%3d");
    formattedNames.put("best.phenotype.length", "%3d");
    formattedNames.put("best.phenotype.depth", "%2d");
    formattedNames.put("best.birth", "%3d");
    if (ancestry) {
      formattedNames.put("best.ancestry.depth", "%2d");
      formattedNames.put("best.ancestry.size", "%5d");
    }
    return formattedNames;
  }  
  
  private int getAncestrySize(Individual<T, F> individual) {
    int count = 1;
    for (Individual<T, F> parent : individual.getParents()) {
      count = count+getAncestrySize(parent);
    }
    return count;
  }

  private int getAncestryDepth(Individual<T, F> individual) {
    int count = 1;
    for (Individual<T, F> parent : individual.getParents()) {
      count = Math.max(count, getAncestryDepth(parent)+1);
    }
    return count;
  }
  
  protected abstract Map<String, String> getFitnessFormattedNames();
  protected abstract Map<String, Object> getFitnessIndexes(F fitness);
  
  private String augmentFitnessName(String fitnessName) {
    if (fitnessName.isEmpty()) {
      return "best.fitness";
    }
    return "best.fitness"+fitnessName;
  }
  
  
}
