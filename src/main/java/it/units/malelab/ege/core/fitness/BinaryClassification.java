/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.fitness;

import it.units.malelab.ege.core.Node;
import java.util.List;

/**
 *
 * @author eric
 */
public class BinaryClassification<I, T> implements FitnessComputer<T, MultiObjectiveFitness<Double>> {
  
  public static interface Classifier<I, T> {
    public boolean classify(I instance, Node<T> classifier);
  }
  
  private final List<I> positives;
  private final List<I> negatives;
  private final Classifier<I, T> classifier;

  public BinaryClassification(List<I> positives, List<I> negatives, Classifier<I, T> classifier) {
    this.positives = positives;
    this.negatives = negatives;
    this.classifier = classifier;
  }
  
  public BinaryClassification<I, T> subset(double from, double to) {
    return new BinaryClassification<>(
            positives.subList((int)Math.round((double)positives.size()*from), (int)Math.round((double)positives.size()*to)),
            negatives.subList((int)Math.round((double)negatives.size()*from), (int)Math.round((double)negatives.size()*to)),
            classifier);
  }
  
  @Override
  public MultiObjectiveFitness<Double> compute(Node<T> phenotype) {
    double falsePositives = 0;
    double falseNegatives = 0;
    for (I positive : positives) {
      falseNegatives = falseNegatives+(classifier.classify(positive, phenotype)?0:1);
    }
    for (I negative : negatives) {
      falsePositives = falsePositives+(classifier.classify(negative, phenotype)?1:0);
    }
    return new MultiObjectiveFitness<Double>(
            falsePositives/(double)negatives.size(),
            falseNegatives/(double)positives.size());
  }

  @Override
  public MultiObjectiveFitness<Double> worstValue() {
    return new MultiObjectiveFitness<Double>(1d, 1d);
  }

  public List<I> getPositives() {
    return positives;
  }

  public List<I> getNegatives() {
    return negatives;
  }

  public Classifier<I, T> getClassifier() {
    return classifier;
  }    
  
}
