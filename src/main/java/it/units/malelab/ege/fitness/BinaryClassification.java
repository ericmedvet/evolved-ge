/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.fitness;

import it.units.malelab.ege.core.fitness.FitnessComputer;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.grammar.Node;
import java.util.List;

/**
 *
 * @author eric
 */
public abstract class BinaryClassification<I, C> implements FitnessComputer<C> {
  
  private final List<I> positives;
  private final List<I> negatives;

  public BinaryClassification(List<I> positives, List<I> negatives) {
    this.positives = positives;
    this.negatives = negatives;
  }

  @Override
  public Fitness compute(Node<C> phenotype) {
    double falsePositives = 0;
    double falseNegatives = 0;
    for (I positive : positives) {
      falseNegatives = falseNegatives+(classify(positive, phenotype)?0:1);
    }
    for (I negative : negatives) {
      falsePositives = falsePositives+(classify(negative, phenotype)?1:0);
    }
    return new MultiObjectiveFitness(
            falsePositives/(double)negatives.size(),
            falseNegatives/(double)positives.size());
  }

  @Override
  public Fitness worstValue() {
    return new MultiObjectiveFitness(1d, 1d);
  }

  public abstract boolean classify(I instance, Node<C> classifier);

  public List<I> getPositives() {
    return positives;
  }

  public List<I> getNegatives() {
    return negatives;
  }    
  
}
