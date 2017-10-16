/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.fitness;

import java.util.Arrays;

/**
 *
 * @author eric
 */
public class MultiObjectiveFitness<T extends Comparable<T>> implements Fitness<T[]> {
  
  private final T[] values;

  public MultiObjectiveFitness(T... values) {
    this.values = values;
  }    

  @Override
  public T[] getValue() {
    return values;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 29 * hash + Arrays.deepHashCode(this.values);
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
    final MultiObjectiveFitness other = (MultiObjectiveFitness) obj;
    if (!Arrays.deepEquals(this.values, other.values)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i<values.length; i++) {
      if (i>0) {
        sb.append(", ");
      }
      sb.append(values[i].toString());
    }
    sb.append("]");
    return sb.toString();
  }

}
