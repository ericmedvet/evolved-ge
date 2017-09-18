/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.operator;

import com.google.common.collect.Range;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class LocalizedTwoPointsCrossover extends TwoPointsCrossover {

  @Override
  public List<BitsGenotype> apply(List<BitsGenotype> parents, Random random) {
    BitsGenotype parent0 = parents.get(0);
    BitsGenotype parent1 = parents.get(1);
    int pivot1, pivot2;
    pivot1 = random.nextInt(parent0.size());
    do {
      pivot2 = random.nextInt(parent0.size());
    } while (pivot1 == pivot2);
    int startIndex0 = Math.min(pivot2, pivot1);
    int endIndex0 = Math.max(pivot2, pivot1);
    int startIndex1 = (int) Math.round((double) startIndex0 * (double) parent1.size() / (double) parent0.size());
    int endIndex1 = Math.min(Math.max((int) Math.round((double) endIndex0 * (double) parent1.size() / (double) parent0.size()), startIndex1+1), parent1.size());
    return children(
            parent0, Range.closedOpen(startIndex0, endIndex0),
            parent1, Range.closedOpen(startIndex1, endIndex1));
  }

}
