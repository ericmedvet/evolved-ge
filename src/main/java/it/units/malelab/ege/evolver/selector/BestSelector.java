/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author eric
 */
public class BestSelector implements Selector {
  
  private final Comparator<Individual> comparator;

  public BestSelector(Comparator<Individual> comparator) {
    this.comparator = comparator;
  }    

  @Override
  public Individual select(List<Individual> population, boolean reverse) {
    Individual bestIndividual = population.get(0);
    for (Individual individual : population) {
      if (comparator.compare(individual, bestIndividual)*(reverse?-1:1)<0) {
        bestIndividual = individual;
      }
    }
    return bestIndividual;
  }
  
}
