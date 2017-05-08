/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.initializer;

import it.units.malelab.ege.core.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class MultiInitializer<G> implements PopulationInitializer<G> {
  
  private final Map<PopulationInitializer<G>, Double> initializers;

  public MultiInitializer(Map<PopulationInitializer<G>, Double> initializers) {
    this.initializers = initializers;
  }

  @Override
  public List<G> build(int n, Validator<G> genotypeValidator) {
    double sum = 0;
    for (double d : initializers.values()) {
      sum = sum+d;
    }
    List<G> population = new ArrayList<>();
    for (Map.Entry<PopulationInitializer<G>, Double> entry : initializers.entrySet()) {
      int localN = (int)Math.round(entry.getValue()/sum*(double)n);
      if (localN>0) {
        population.addAll(entry.getKey().build((int)Math.round(entry.getValue()/sum*(double)n), genotypeValidator));
      }
    }
    if (population.size()>n) {
      population = population.subList(0, n);
    }
    return population;
  }
  
}
