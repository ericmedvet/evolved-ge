/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.fitness;

import it.units.malelab.ege.core.grammar.Node;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.util.distance.Distance;
import java.util.List;

/**
 *
 * @author eric
 */
public class LeafContentsDistance<T> implements FitnessComputer<T, NumericFitness> {
  
  private final List<T> target;
  private final Distance<List<T>> distance;

  public LeafContentsDistance(List<T> target, Distance<List<T>> distance) {
    this.target = target;
    this.distance = distance;
  }

  @Override
  public NumericFitness compute(Node<T> phenotype) {
    double d = distance.d(Utils.contents(phenotype.leaves()), target);
    return new NumericFitness(d);
  }

  @Override
  public NumericFitness worstValue() {
    return new NumericFitness(Double.POSITIVE_INFINITY);
  }
  
}
