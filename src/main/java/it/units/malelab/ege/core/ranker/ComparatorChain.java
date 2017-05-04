/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.ranker;

import java.util.Comparator;

/**
 *
 * @author eric
 */
public class ComparatorChain<T> implements Comparator<T> {
  
  private final Comparator<T>[] comparators;

  public ComparatorChain(Comparator<T>... comparators) {
    this.comparators = comparators;
  }

  @Override
  public int compare(T t1, T t2) {
    for (Comparator<T> comparator : comparators) {
      int result = comparator.compare(t1, t2);
      if (result!=0) {
        return result;
      }        
    }
    return 0;
  }    
  
}
