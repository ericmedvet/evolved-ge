/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.ranker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class RandomizerRanker<T> implements Ranker<T> {
  
  private final Random random;

  public RandomizerRanker(Random random) {
    this.random = random;
  }

  @Override
  public List<List<T>> rank(List<T> ts) {
    List<List<T>> ranks = new ArrayList<>(ts.size());
    for (T t : ts) {
      ranks.add(Collections.singletonList(t));
    }
    Collections.shuffle(ranks, random);
    return ranks;
  }
  
}
