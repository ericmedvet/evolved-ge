/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class RankProportional<T> implements Selector<T> {
  
  private final Comparator<T> comparator;
  private final double k;
  private final Random random;

  public RankProportional(Comparator<T> comparator, double k, Random random) {
    this.comparator = comparator;
    this.k = k;
    this.random = random;
  }  

  @Override
  public T select(List<T> ts) {
    List<T> list = new ArrayList<>(ts);
    Collections.sort(list, comparator.reversed());
    double sum = 0;
    for (double i = 0; i<ts.size(); i++) {
      sum = sum+Math.pow(k, i);
    }
    double r = random.nextDouble()*sum;
    sum = 0;
    int index = 0;
    for (double i = 0; i<ts.size(); i++) {
      sum = sum+Math.pow(k, i);
      if (r<sum) {
        index = (int)i;
        break;
      }
    }
    return list.get(index);
  }

  @Override
  public String toString() {
    return "RankProportional{" + "comparator=" + comparator + ", k=" + k + '}';
  }  
  
}
