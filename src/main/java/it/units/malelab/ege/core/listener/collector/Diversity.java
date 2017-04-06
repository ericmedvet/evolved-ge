/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.collector;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.listener.collector.PopulationInfoCollector;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.grammar.Node;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eric
 */
public class Diversity<T, F extends Fitness> implements PopulationInfoCollector<T, F>{

  @Override
  public Map<String, Object> collect(List<Individual<T, F>> population) {
    Set<Node<T>> phenotypes = new HashSet<>();
    Set<Fitness> fitnesses = new HashSet<>();
    for (Individual<T, F> individual : population) {
      phenotypes.add(individual.getPhenotype());
      fitnesses.add(individual.getFitness());
    }
    Map<String, Object> indexes = new LinkedHashMap<>();
    indexes.put("diversity.phenotype", (double) phenotypes.size() / (double) population.size());
    indexes.put("diversity.fitness", (double) fitnesses.size() / (double) population.size());
    return indexes;
  }  
  
  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("diversity.phenotype", "%4.2f");
    formattedNames.put("diversity.fitness", "%4.2f");
    return formattedNames;
  }  
    
}
