/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author eric
 */
public class Tournament<T> implements Selector<T> {
  
  private final int size;
  private final Random random;

  public Tournament(int size, Random random) {
    this.size = size;
    this.random = random;
  }

  @Override
  public T select(List<List<T>> ts) {
    SortedMap<Integer, List<T>> selected = new TreeMap<>();
    for (int i = 0; i<size; i++) {
      int rankIndex = random.nextInt(ts.size());
      int index = random.nextInt(ts.get(rankIndex).size());
      List<T> localTs = selected.get(rankIndex);
      if (localTs==null) {
        localTs = new ArrayList<>();
        selected.put(rankIndex, localTs);
      }
      localTs.add(ts.get(rankIndex).get(index));
    }
    return selected.get(selected.firstKey()).get(0);
  }

  @Override
  public String toString() {
    return "Tournament{" + "size=" + size + ", random=" + random + '}';
  }

}
