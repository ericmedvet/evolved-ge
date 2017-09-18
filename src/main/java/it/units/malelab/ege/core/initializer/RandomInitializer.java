/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.initializer;

import it.units.malelab.ege.core.validator.Validator;
import it.units.malelab.ege.core.Factory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class RandomInitializer<G> implements PopulationInitializer<G> {
  
  private final Factory<G> factory;

  public RandomInitializer(Factory<G> factory) {
    this.factory = factory;
  }

  @Override
  public List<G> build(int n, Validator<G> genotypeValidator, Random random) {
    List<G> genotypes = new ArrayList<>(n);
    for (int i = 0; i<n; i++) {
      while (true) {
        G genotype = factory.build(random);
        if (genotypeValidator.validate(genotype, random)) {
          genotypes.add(genotype);
          break;
        }
      }
    }
    return genotypes;
  }
  
}
