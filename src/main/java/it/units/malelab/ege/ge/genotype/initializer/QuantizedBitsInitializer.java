/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.genotype.initializer;

import it.units.malelab.ege.core.initializer.PopulationInitializer;
import com.google.common.collect.Range;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.core.Validator;
import it.units.malelab.ege.util.Utils;
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
  public List<BitsGenotype> getGenotypes(int n, Validator<BitsGenotype> genotypeValidator) {
    List<BitsGenotype> genotypes = new ArrayList<>(n);
    int pieces = (int)Math.ceil(Math.log(n)/Math.log(2d));
    for (int i = 0; i<n; i++) {
      BitsGenotype genotype = new BitsGenotype(size);
      List<BitsGenotype> slices = genotype.slices(Utils.slices(Range.closedOpen(0, genotype.length()), pieces));
      int b = 0;
      int v = i;
      for (int j = slices.size()-1; j>=0; j--) {
        if (v>=Math.pow(2, j)) {
          slices.get(j).flip();
          v = v-(int)Math.pow(2, j);
        }
        genotype.set(b, slices.get(j));
        b = b+slices.get(j).length();
      }
      genotypes.add(genotype);
    }
    return genotypes;
  }

}
