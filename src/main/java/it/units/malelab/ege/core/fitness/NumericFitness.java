/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.fitness;

/**
 *
 * @author eric
 */
public class NumericFitness implements Comparable<NumericFitness>, Fitness<Double> {
  
  private final double value;

  public NumericFitness(double value) {
    this.value = value;
  }

  @Override
  public int compareTo(NumericFitness otherFitness) {
    return Double.compare(value, ((NumericFitness)otherFitness).getValue());
  }

  @Override
  public Double getValue() {
    return value;
  }

  @Override
  public String toString() {
    return Double.toString(value);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 73 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
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
    final NumericFitness other = (NumericFitness) obj;
    if (Double.doubleToLongBits(this.value) != Double.doubleToLongBits(other.value)) {
      return false;
    }
    return true;
  }
  
}
