/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.genotype;

import com.google.common.collect.Range;
import it.units.malelab.ege.util.Utils;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author eric
 */
public class BitsGenotype implements Genotype {
  
  private final int size;
  private final BitSet bitSet;

  public BitsGenotype(int nBits) {
    this.size = nBits;
    bitSet = new BitSet(nBits);
  }

  public BitsGenotype(int size, BitSet bitSet) {
    this.size = size;
    this.bitSet = bitSet.get(0, size);
  }
  
  @Override
  public int size() {
    return size;
  }
  
  public BitsGenotype slice(int fromIndex, int toIndex) {
    checkIndexes(fromIndex, toIndex);
    return new BitsGenotype(toIndex-fromIndex, bitSet.get(fromIndex, toIndex));
  }
  
  public int count() {
    return bitSet.cardinality();
  }
  
  public int toInt() {
    BitsGenotype genotype = this;
    if (size>Integer.SIZE/2) {
      genotype = compress(Integer.SIZE/2);
    }
    if (genotype.bitSet.toLongArray().length<=0) {
      return 0;
    }
    return (int)genotype.bitSet.toLongArray()[0];
  }
    
  public void set(int fromIndex, BitsGenotype other) {
    checkIndexes(fromIndex, fromIndex+other.size());
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
  
  public boolean get(int index) {
    checkIndexes(index, index+1);
    return bitSet.get(index);
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
      throw new ArrayIndexOutOfBoundsException(String.format("from=%d < 0", fromIndex));
    }
    if (toIndex>size) {
      throw new ArrayIndexOutOfBoundsException(String.format("to=%d > size=%d", toIndex, size));
    }
  }
  
  public BitSet asBitSet() {
    BitSet copy = new BitSet(size);
    copy.or(bitSet);
    return copy;
  }
    
  public BitsGenotype compress(int newSize) {
    BitsGenotype compressed = new BitsGenotype(newSize);
    List<BitsGenotype> slices = slices(newSize);
    for (int i = 0; i<slices.size(); i++) {
      compressed.bitSet.set(i, slices.get(i).count()>slices.get(i).size()/2);
    }
    return compressed;
  }
    
  public List<BitsGenotype> slices(final List<Integer> sizes) {
    List<BitsGenotype> genotypes = new ArrayList<>();
    List<Range<Integer>> ranges = Utils.slices(size, sizes);
    for (Range<Integer> range : ranges) {
      if (range.lowerEndpoint()<range.upperEndpoint()) {
        genotypes.add(slice(range.lowerEndpoint(), range.upperEndpoint()));
      } else {
        genotypes.add(new BitsGenotype(0));
      }
    }
    return genotypes;
  }
  
  public List<BitsGenotype> slices(int number) {
    List<Integer> sizes = new ArrayList<>(number);
    for (int i = 0; i<number; i++) {
      sizes.add(1);
    }
    return slices(sizes);
  }
  
  public BitsGenotype append(BitsGenotype genotype) {
    BitsGenotype resultGenotype = new BitsGenotype(size+genotype.size);
    if (size>0) {
      resultGenotype.set(0, this);
    }
    resultGenotype.set(size, genotype);
    return resultGenotype;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 47 * hash + this.size;
    hash = 47 * hash + Objects.hashCode(this.bitSet);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BitsGenotype other = (BitsGenotype) obj;
    if (this.size != other.size) {
      return false;
    }
    if (!Objects.equals(this.bitSet, other.bitSet)) {
      return false;
    }
    return true;
  }
    
}
