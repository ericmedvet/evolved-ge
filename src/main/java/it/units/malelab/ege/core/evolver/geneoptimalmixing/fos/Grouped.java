/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver.geneoptimalmixing.fos;

import it.units.malelab.ege.core.ConstrainedSequence;
import it.units.malelab.ege.core.Sequence;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author eric
 */
public class Grouped implements FOSBuilder {
  
  private final int groupSize;

  public Grouped(int groupSize) {
    this.groupSize = groupSize;
  }    

  @Override
  public Set<Set<Integer>> build(List<ConstrainedSequence> sequences, Random random) {
    int minMaxIndex = Integer.MAX_VALUE;
    for (Sequence sequence : sequences) {
      minMaxIndex = Math.min(minMaxIndex, sequence.size());
    }
    Set<Set<Integer>> fos = new LinkedHashSet<>();
    for (int i = 0; i<Math.floor((double)minMaxIndex/(double)groupSize); i++) {
      Set<Integer> subset = new LinkedHashSet<>();
      for (int j = 0; j<groupSize; j++) {
        subset.add(i*groupSize+j);
      }
      fos.add(subset);
    }
    return fos;
  }
  
}
