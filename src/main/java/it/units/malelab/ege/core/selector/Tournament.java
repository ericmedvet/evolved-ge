/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.selector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class Tournament<T extends Ranked> implements Selector<T> {
  
  private final int size;
  private final Random random;

  public Tournament(int size, Random random) {
    this.size = size;
    this.random = random;
  }

  @Override
  public T select(List<T> ts) {
    List<T> preselectedTs = new ArrayList<>(size);
    for (int i = 0; i<size; i++) {
      preselectedTs.add(ts.get(random.nextInt(ts.size())));
    }
    T selectedT = preselectedTs.get(0);
    for (int i =1; i<preselectedTs.size(); i++) {
      if (preselectedTs.get(i).getRank()<selectedT.getRank()) {
        selectedT = preselectedTs.get(i);
      }
    }
    return selectedT;
  }

  @Override
  public String toString() {
    return "Tournament{" + "size=" + size + ", random=" + random + '}';
  }

}
