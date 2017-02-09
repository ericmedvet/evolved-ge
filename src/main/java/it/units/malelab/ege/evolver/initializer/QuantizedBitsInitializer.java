/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.initializer;

import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.validator.GenotypeValidator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class QuantizedBitsInitializer implements PopulationInitializer<BitsGenotype> {
  
  private final int size;

  public QuantizedBitsInitializer(int size) {
    this.size = size;
  }  

  @Override
  public List<BitsGenotype> getGenotypes(int n, GenotypeValidator<BitsGenotype> genotypeValidator) {
    List<BitsGenotype> genotypes = new ArrayList<>(n);
    int pieces = (int)Math.ceil(Math.log(n)/Math.log(2d));
    for (int i = 0; i<n; i++) {
      BitsGenotype genotype = new BitsGenotype(size);
      List<BitsGenotype> slices = genotype.slices(pieces);
      int b = 0;
      int v = i;
      for (int j = slices.size()-1; j>=0; j--) {
        if (v>=Math.pow(2, j)) {
          slices.get(j).flip();
          v = v-(int)Math.pow(2, j);
        }
        genotype.set(b, slices.get(j));
        b = b+slices.get(j).size();
      }
      genotypes.add(genotype);
    }
    return genotypes;
  }

}
