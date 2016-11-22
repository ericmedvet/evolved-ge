/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import java.util.Comparator;
import java.util.List;

/**
 *
 * @author eric
 */
public class Best<T> implements Selector<T> {
  
  private final Comparator<T> comparator;

  public Best(Comparator<T> comparator) {
    this.comparator = comparator;
  }    

  @Override
  public T select(List<T> ts) {
    T selectedT = ts.get(0);
    for (int i =1; i<ts.size(); i++) {
      if (comparator.compare(ts.get(i), selectedT)<0) {
        selectedT = ts.get(i);
      }
    }
    return selectedT;
  }

}
