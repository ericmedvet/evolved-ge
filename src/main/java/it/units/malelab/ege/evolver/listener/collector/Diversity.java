/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener.collector;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.fitness.Fitness;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.grammar.Node;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eric
 */
public class Diversity<G extends Genotype, T> implements PopulationInfoCollector<G, T>{

  @Override
  public Map<String, Object> collect(List<Individual<G, T>> population) {
    Set<G> genotypes = new HashSet<>();
    Set<Node<T>> phenotypes = new HashSet<>();
    Set<Fitness> fitnesses = new HashSet<>();
    for (Individual<G, T> individual : population) {
      genotypes.add(individual.getGenotype());
      phenotypes.add(individual.getPhenotype());
      fitnesses.add(individual.getFitness());
    }
    Map<String, Object> indexes = new LinkedHashMap<>();
    indexes.put("diversity.genotype", (double) genotypes.size() / (double) population.size());
    indexes.put("diversity.phenotype", (double) phenotypes.size() / (double) population.size());
    indexes.put("diversity.fitness", (double) fitnesses.size() / (double) population.size());
    return indexes;
  }  
  
  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("diversity.genotype", "%4.2f");
    formattedNames.put("diversity.phenotype", "%4.2f");
    formattedNames.put("diversity.fitness", "%4.2f");
    return formattedNames;
  }  
    
}
