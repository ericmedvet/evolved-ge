/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver.geneoptimalmixing.fos;

import it.units.malelab.ege.core.ConstrainedSequence;
import it.units.malelab.ege.util.Pair;
import java.util.ArrayList;
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
  protected Pair<Pair<Set<Integer>, Set<Integer>>, Double> choosePair(Set<Set<Integer>> subsets, Map<Pair<Set<Integer>, Set<Integer>>, Double> dMap, Random random) {
    List<Set<Integer>> list = new ArrayList<>(subsets);
    int i1 = random.nextInt(list.size());
    int i2 = i1;
    while (i2 == i1) {
      i2 = random.nextInt(list.size());
    }
    return new Pair<>(new Pair<>(list.get(i1), list.get(i2)), random.nextDouble());
  }

  @Override
  protected Map<Pair<Set<Integer>, Set<Integer>>, Double> computeInitialDistanceMap(int minMaxIndex, List<ConstrainedSequence> sequences) {
    return null;
  }


}
