/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.ranker;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import java.util.Comparator;

/**
 *
 * @author eric
 */
public class LexicoGraphicalMOComparator implements Comparator<MultiObjectiveFitness> {
  
  private final int[] order;

  public LexicoGraphicalMOComparator(int... order) {
    this.order = order;
  }

  
  @Override
  public int compare(MultiObjectiveFitness f1, MultiObjectiveFitness f2) {
    for (int index : order) {
      int result = f1.getValue()[index].compareTo(f2.getValue()[index]);
      if (result!=0) {
        return result;
      }
    }
    return 0;
  }  
  
}
