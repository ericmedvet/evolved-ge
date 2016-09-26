/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.initializer;

import it.units.malelab.ege.evolver.validator.GenotypeValidator;
import it.units.malelab.ege.BitsGenotype;
import it.units.malelab.ege.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class BitsRandomInitializer implements PopulationInitializer<BitsGenotype> {
  
  private final int size;
  private final Random random;

  public BitsRandomInitializer(int size, Random random) {
    this.size = size;
    this.random = random;
  }

  @Override
  public List<BitsGenotype> getGenotypes(int n, GenotypeValidator genotypeValidator) {
    List<BitsGenotype> genotypes = new ArrayList<>(n);
    for (int i = 0; i<n; i++) {
      while (true) {
        BitsGenotype genotype = Utils.randomGenotype(size, random);
        if (genotypeValidator.validate(genotype)) {
          genotypes.add(genotype);
          break;
        }
      }
    }
    return genotypes;
  }
  
}
