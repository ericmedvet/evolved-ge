/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.initializer;

import it.units.malelab.ege.evolver.validator.GenotypeValidator;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.Utils;
import it.units.malelab.ege.evolver.genotype.Factory;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class RandomInitializer<G extends Genotype> implements PopulationInitializer<G> {
  
  private final Random random;
  private final Factory<G> factory;

  public RandomInitializer(Random random, Factory<G> factory) {
    this.random = random;
    this.factory = factory;
  }

  @Override
  public List<G> getGenotypes(int n, GenotypeValidator<G> genotypeValidator) {
    List<G> genotypes = new ArrayList<>(n);
    for (int i = 0; i<n; i++) {
      while (true) {
        G genotype = factory.build(random);
        if (genotypeValidator.validate(genotype)) {
          genotypes.add(genotype);
          break;
        }
      }
    }
    return genotypes;
  }
  
}
