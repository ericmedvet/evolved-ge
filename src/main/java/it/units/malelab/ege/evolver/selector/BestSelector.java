/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.util.List;

/**
 *
 * @author eric
 */
public class BestSelector<G extends Genotype, T> implements Selector<G, T>{

  @Override
  public Individual<G, T> select(List<Individual<G, T>> population) {
    Individual<G, T> best = population.get(0);
    for (Individual<G, T> individual : population) {
      if (individual.getFitness().compareTo(best.getFitness())<0) {
        best = individual;
      }
    }
    return best;
  }
  
}
