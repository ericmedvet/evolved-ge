/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.genotype;

import it.units.malelab.ege.core.Factory;
import java.util.BitSet;
import java.util.Random;

/**
 *
 * @author eric
 */
public class BitsGenotypeFactory implements Factory<BitsGenotype> {

  private final int size;

  public BitsGenotypeFactory(int size) {
    this.size = size;
  }
  
  @Override
  public BitsGenotype build(Random random) {
    BitSet bitSet = new BitSet(size);
    for (int i = 0; i<size; i++) {
      bitSet.set(i, random.nextBoolean());
    }
    return new BitsGenotype(size, bitSet);
  }
  
}
