/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.initializer;

import it.units.malelab.ege.evolver.validator.GenotypeValidator;
import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class RandomInitializer implements PopulationInitializer {
  
  private final int size;
  private final Random random;

  public RandomInitializer(int size, Random random) {
    this.size = size;
    this.random = random;
  }

  @Override
  public List<Genotype> getGenotypes(int n, GenotypeValidator genotypeValidator) {
    List<Genotype> genotypes = new ArrayList<>(n);
    for (int i = 0; i<n; i++) {
      while (true) {
        Genotype genotype = Utils.randomGenotype(size, random);
        if (genotypeValidator.validate(genotype)) {
          genotypes.add(genotype);
          break;
        }
      }
    }
    return genotypes;
  }
  
}
