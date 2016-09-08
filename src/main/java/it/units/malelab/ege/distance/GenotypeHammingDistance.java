/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distance;

import it.units.malelab.ege.Genotype;
import java.util.BitSet;

/**
 *
 * @author eric
 */
public class GenotypeHammingDistance implements Distance<Genotype> {

  @Override
  public double d(Genotype g1, Genotype g2) {
    BitSet xor = g1.asBitSet();
    xor.xor(g2.asBitSet());
    return xor.cardinality();
  }
  
}
