/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.ranker;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.ComparableFitness;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author eric
 */
public class ComparableFitnessRanker<T, F extends ComparableFitness> implements IndividualRanker<T, F> {
  
  private final Comparator<Individual<T, F>> comparator;

  public ComparableFitnessRanker() {
    this.comparator = new Comparator<Individual<T, F>>() {
      @Override
      public int compare(Individual<T, F> i1, Individual<T, F> i2) {
        return i1.getFitness().compareTo(i2.getFitness());
      }
    };
  }

  @Override
  public void rank(List<Individual<T, F>> individuals) {
    Collections.sort(individuals, comparator);
    individuals.get(0).setRank(0);
    for (int i = 1; i<individuals.size(); i++) {
      if (individuals.get(i).getFitness().compareTo(individuals.get(i-1).getFitness())>0) {
        individuals.get(i).setRank(individuals.get(i-1).getRank()+1);
      } else {
        individuals.get(i).setRank(individuals.get(i-1).getRank());
      }
    }
  }
  
}
