/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.booleanfunction;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.fitness.FitnessComputer;
import it.units.malelab.ege.core.fitness.NumericFitness;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author eric
 */
public class MOPMErrors implements FitnessComputer<String, NumericFitness> {

  private final int size;

  public MOPMErrors(int size) {
    this.size = size;
  }

  @Override
  public NumericFitness compute(Node<String> phenotype) {
    int errors = 0;
    int cases = 0;
    for (int m0 = 0; m0 < Math.pow(2, size); m0++) {
      Map<String, boolean[]> vars = new LinkedHashMap<>();
      //prepare 1st arg
      boolean[] bits0 = BooleanUtils.toBinary(m0, size);
      for (int i = 0; i<size; i++) {
        vars.put("b0."+i, new boolean[] {bits0[i]});
      }
      for (int m1 = 0; m1 < Math.pow(2, size); m1++) {
        //prepare 2nd arg
        boolean[] bits1 = BooleanUtils.toBinary(m1, size);
        for (int i = 0; i<size; i++) {
          vars.put("b1."+i, new boolean[] {bits1[i]});
        }
        //compute expected result
        boolean[] bitsExpectedResult = BooleanUtils.toBinary(m0*m1, 2*size);
        //compute result
        boolean[] bitsResult = new boolean[2*size];
        for (int i = 0; i<2*size; i++) {
          boolean[] bits = BooleanUtils.compute(BooleanUtils.transform(phenotype.getChildren().get(i)), vars, 1);
          bitsResult[i] = bits[0];
        }
        //compare
        if (!Arrays.equals(bitsExpectedResult, bitsResult)) {
          errors = errors+1;
        }
        cases = cases+1;
      }
    }
    return new NumericFitness((double)errors/(double)cases);
  }

  @Override
  public NumericFitness worstValue() {
    return new NumericFitness(1);
  }

  @Override
  public NumericFitness bestValue() {
    return new NumericFitness(0);
  }
  
}
