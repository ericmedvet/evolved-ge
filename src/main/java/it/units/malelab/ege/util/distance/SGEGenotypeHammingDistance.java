/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util.distance;

import it.units.malelab.ege.util.Pair;
import it.units.malelab.ege.ge.genotype.SGEGenotype;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class SGEGenotypeHammingDistance<T> implements Distance<SGEGenotype<T>> {

  @Override
  public double d(SGEGenotype<T> g1, SGEGenotype<T> g2) {
    //not perfectly simmetric if genotypes contain different keys
    int count = 0;
    for (Map.Entry<Pair<T, Integer>, List<Integer>> entry1 : g1.getGenes().entrySet()) {
      List<Integer> values2 = g2.getGenes().get(entry1.getKey());
      if (values2==null) {
        count = count+values2.size();
      } else {
        for (int i = 0; i<Math.min(values2.size(), entry1.getValue().size()); i++) {
          if (values2.get(i)!=entry1.getValue().get(i)) {
            count = count+1;
          }
        }
        count = count+Math.abs(values2.size()-entry1.getValue().size());
      }
    }
    return count;
  }
  
}
