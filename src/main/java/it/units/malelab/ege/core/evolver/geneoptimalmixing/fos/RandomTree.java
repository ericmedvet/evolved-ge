/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver.geneoptimalmixing.fos;

import it.units.malelab.ege.core.ConstrainedSequence;
import it.units.malelab.ege.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author eric
 */
public class RandomTree extends UPGMAMutualInformationTree {

  public RandomTree(int minSubsetSize, int limit) {
    super(minSubsetSize, limit);
  }

  @Override
  protected Map<Pair<Set<Integer>, Set<Integer>>, Double> computeInitialDistanceMap(int minMaxIndex, List<ConstrainedSequence> sequences, Random random) {
    //fill a map with random numbers for singleton
    Map<Pair<Set<Integer>, Set<Integer>>, Double> map = new LinkedHashMap<>();
    for (int i = 0; i < minMaxIndex; i++) {
      for (int j = i + 1; j < minMaxIndex; j++) {
        map.put(new Pair<>(Collections.singleton(i), Collections.singleton(j)), random.nextDouble());
        map.put(new Pair<>(Collections.singleton(j), Collections.singleton(i)), random.nextDouble());
      }
    }
    return map;
  }


}
