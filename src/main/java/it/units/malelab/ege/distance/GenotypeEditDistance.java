/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distance;

import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import java.util.Arrays;

/**
 *
 * @author eric
 */
public class GenotypeEditDistance implements Distance<BitsGenotype> {
  
  private final EditDistance<Boolean> d = new EditDistance<Boolean>();

  @Override
  public double d(BitsGenotype g1, BitsGenotype g2) {
    Boolean[] bs1 = new Boolean[g1.size()];
    Boolean[] bs2 = new Boolean[g2.size()];
    for (int i = 0; i<bs1.length; i++) {
      bs1[i] = g1.get(i);
    }
    for (int i = 0; i<bs2.length; i++) {
      bs2[i] = g2.get(i);
    }
    return d.d(Arrays.asList(bs1), Arrays.asList(bs2));
  }
  
}
