/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.fitness;

import it.units.malelab.ege.evolver.fitness.Fitness;

/**
 *
 * @author eric
 */
public class NumericFitness implements Fitness<Double> {
  
  private final double value;

  public NumericFitness(double value) {
    this.value = value;
  }

  @Override
  public int compareTo(Fitness otherFitness) {
    if (otherFitness instanceof NumericFitness) {
      return Double.compare(value, ((NumericFitness)otherFitness).getValue());
    }
    return -1;
  }

  @Override
  public Double getValue() {
    return value;
  }

  @Override
  public String toString() {
    return Double.toString(value);
  }

}
