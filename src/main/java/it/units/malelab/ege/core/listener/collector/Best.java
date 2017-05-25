/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.collector;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Sequence;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.fitness.FitnessComputer;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public abstract class Best<G extends Sequence, T, F extends Fitness> implements Collector<G, T, F>{
  
  private final boolean ancestry;
  private final FitnessComputer<T, F> validationFitnessComputer;

  public Best(boolean ancestry, FitnessComputer<T, F> validationFitnessComputer) {
    this.ancestry = ancestry;
    this.validationFitnessComputer = validationFitnessComputer;
  }
  
  @Override
  public Map<String, Object> collect(GenerationEvent<G, T, F> event) {
    List<List<Individual<G, T, F>>> rankedPopulation = new ArrayList<>(event.getRankedPopulation());
    Individual<G, T, F> best = rankedPopulation.get(0).get(0);
    Map<String, Object> indexes = new LinkedHashMap<>();
    for (Map.Entry<String, Object> fitnessEntry : getFitnessIndexes(best.getFitness()).entrySet()) {
      indexes.put(
              augmentFitnessName("best.fitness", fitnessEntry.getKey()),
              fitnessEntry.getValue());
    }
    if (validationFitnessComputer!=null) {
      F validationFitness = validationFitnessComputer.compute(best.getPhenotype());
      for (Map.Entry<String, Object> fitnessEntry : getFitnessIndexes(validationFitness).entrySet()) {
        indexes.put(
                augmentFitnessName("best.validation.fitness", fitnessEntry.getKey()),
                fitnessEntry.getValue());
      }
    }
    indexes.put("best.genotype.length", best.getGenotype().length());
    indexes.put("best.phenotype.size", best.getPhenotype().size());
    indexes.put("best.phenotype.length", best.getPhenotype().length());
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
              augmentFitnessName("best.fitness", fitnessEntry.getKey()),
              fitnessEntry.getValue());
    }
    for (Map.Entry<String, String> fitnessEntry : getFitnessFormattedNames().entrySet()) {
      formattedNames.put(
              augmentFitnessName("best.validation.fitness", fitnessEntry.getKey()),
              fitnessEntry.getValue());
    }      
    formattedNames.put("best.genotype.length", "%4d");
    formattedNames.put("best.phenotype.length", "%3d");
    formattedNames.put("best.phenotype.size", "%3d");
    formattedNames.put("best.phenotype.depth", "%2d");
    formattedNames.put("best.birth", "%3d");
    if (ancestry) {
      formattedNames.put("best.ancestry.depth", "%2d");
      formattedNames.put("best.ancestry.size", "%5d");
    }
    return formattedNames;
  }  
  
  private int getAncestrySize(Individual<G, T, F> individual) {
    int count = 1;
    for (Individual<G, T, F> parent : individual.getParents()) {
      count = count+getAncestrySize(parent);
    }
    return count;
  }

  private int getAncestryDepth(Individual<G, T, F> individual) {
    int count = 1;
    for (Individual<G, T, F> parent : individual.getParents()) {
      count = Math.max(count, getAncestryDepth(parent)+1);
    }
    return count;
  }
  
  protected abstract Map<String, String> getFitnessFormattedNames();
  protected abstract Map<String, Object> getFitnessIndexes(F fitness);
  
  private String augmentFitnessName(String prefix, String fitnessName) {
    if (fitnessName.isEmpty()) {
      return prefix;
    }
    return prefix+"."+fitnessName;
  }
  
  
}
