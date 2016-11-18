/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.Individual;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class TournamentSelector implements Selector {
  
  private final int size;
  private final Random random;
  private final Comparator<Individual> comparator;

  public TournamentSelector(int size, Random random, Comparator<Individual> comparator) {
    this.size = size;
    this.random = random;
    this.comparator = comparator;
  }

  @Override
  public Individual select(List<Individual> population, boolean reverse) {
    Individual bestIndividual = null;
    for (int i = 0; i<Math.min(population.size(), size); i++) {
      int index = random.nextInt(population.size());
      Individual individual = population.get(index);
      if (bestIndividual==null) {
        bestIndividual = individual;
      } else if (comparator.compare(individual, bestIndividual)*(reverse?-1:1)<0) {
        bestIndividual = individual;
      }
    }
    return bestIndividual;
  }
  
}
