/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import java.util.BitSet;

/**
 *
 * @author eric
 */
public class Genotype {
  
  private int size;
  private BitSet bitSet;

  public Genotype(int nBits) {
    this.size = nBits;
    bitSet = new BitSet(nBits);
  }

  public Genotype(int size, BitSet bitSet) {
    this.size = size;
    this.bitSet = bitSet.get(0, size);
  }
  
  public int size() {
    return size;
  }
  
  public Genotype slice(int fromIndex, int toIndex) {
    checkIndexes(fromIndex, toIndex);
    return new Genotype(toIndex-fromIndex, bitSet.get(fromIndex, toIndex));
  }
  
  public int count() {
    return bitSet.cardinality();
  }
  
  public int toInt() {
    int sum = 0;
    for (long l : bitSet.toLongArray()) {
      sum = sum+(int)l;
    }
    return sum;
  }
  
  public void set(int fromIndex, Genotype other) {
    checkIndexes(fromIndex, fromIndex+1);
    for (int i = 0; i<other.size(); i++) {
      bitSet.set(fromIndex+i, other.bitSet.get(i));
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(size+":");
    for (int i = 0; i < size; i++) {
      sb.append(bitSet.get(i) ? '1' : '0');
    }
    return sb.toString();
  }
  
  public void flip() {
    bitSet.flip(0, size);
  }
  
  public void flip(int index) {
    checkIndexes(index, index+1);
    bitSet.flip(index);
  }
  
  public void flip(int fromIndex, int toIndex) {
    checkIndexes(fromIndex, toIndex);
    bitSet.flip(fromIndex, toIndex);
  }
  
  private void checkIndexes(int fromIndex, int toIndex) {
    if (fromIndex>=toIndex) {
      throw new ArrayIndexOutOfBoundsException(String.format("from=%d >= to=%d", fromIndex, toIndex));
    }
    if (fromIndex<0) {
      throw new ArrayIndexOutOfBoundsException(String.format("to=%d < 0", fromIndex));
    }
    if (toIndex>size) {
      throw new ArrayIndexOutOfBoundsException(String.format("from=%d > size=%d", toIndex, size));
    }
  }
  
  public BitSet asBitSet() {
    BitSet copy = new BitSet(size);
    copy.or(bitSet);
    return copy;
  }
    
}
