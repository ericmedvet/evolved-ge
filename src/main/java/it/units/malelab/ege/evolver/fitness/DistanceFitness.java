/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.fitness;

import it.units.malelab.ege.Node;
import it.units.malelab.ege.Utils;
import it.units.malelab.ege.distance.Distance;
import java.util.List;

/**
 *
 * @author eric
 */
public class DistanceFitness<T> implements FitnessComputer<T> {
  
  private final List<T> target;
  private final Distance<List<T>> distance;

  public DistanceFitness(List<T> target, Distance<List<T>> distance) {
    this.target = target;
    this.distance = distance;
  }

  @Override
  public Fitness compute(Node<T> phenotype) {
    double d = distance.d(Utils.contents(phenotype.leaves()), target);
    return new NumericFitness(d);
  }

  @Override
  public Fitness worstValue() {
    return new NumericFitness(Double.POSITIVE_INFINITY);
  }
  
}
