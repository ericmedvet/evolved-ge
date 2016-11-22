/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class Tournament<T> implements Selector<T> {
  
  private final int size;
  private final Random random;
  private final Comparator<T> comparator;

  public Tournament(int size, Random random, Comparator<T> comparator) {
    this.size = size;
    this.random = random;
    this.comparator = comparator;
  }

  @Override
  public T select(List<T> ts) {
    List<T> preselectedTs = new ArrayList<>(size);
    for (int i = 0; i<size; i++) {
      preselectedTs.add(ts.get(random.nextInt(ts.size())));
    }
    T selectedT = preselectedTs.get(0);
    for (int i =1; i<preselectedTs.size(); i++) {
      if (comparator.compare(preselectedTs.get(i), selectedT)<0) {
        selectedT = preselectedTs.get(i);
      }
    }
    return selectedT;
  }

}
