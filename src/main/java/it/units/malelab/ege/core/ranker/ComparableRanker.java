/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.ranker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class ComparableRanker<T> implements Ranker<T> {

  private final Comparator<T> comparator;

  public ComparableRanker(Comparator<T> comparator) {
    this.comparator = comparator;
  }

  @Override
  public List<List<T>> rank(List<T> individuals, Random random) {
    List<List<T>> ranks = new ArrayList<>();
    Collections.sort(individuals, comparator);
    ranks.add(new ArrayList<>(Arrays.asList(individuals.get(0))));
    for (int i = 1; i < individuals.size(); i++) {
      if (comparator.compare(individuals.get(i-1), individuals.get(i))< 0) {
        ranks.add(new ArrayList<>(Arrays.asList(individuals.get(i))));
      } else {
        ranks.get(ranks.size() - 1).add(individuals.get(i));
      }
    }
    return ranks;
  }

}
