/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.fitness;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.fitness.FitnessComputer;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.util.booleanfunction.BooleanUtils;
import java.util.Map;

/**
 *
 * @author eric
 */
public class ParityErrors implements FitnessComputer<String, NumericFitness> {
  
  private final int size;
  
  private Map<String, boolean[]> cases;
  private boolean[] expectedResult;

  public ParityErrors(int size) {
    this.size = size;
    String[] names = new String[size];
    for (int i = 0; i<size; i++) {
      names[i] = "b"+i;
    }
    cases = BooleanUtils.buildCompleteCases(names);
    expectedResult = new boolean[cases.get(names[0]).length];
    for (int i = 0; i<expectedResult.length; i++) {
      int count = 0;
      for (String name : names) {
        count = count+((cases.get(name)[i])?1:0);
      }
      expectedResult[i] = (count%2)!=0;
    }
  }    

  @Override
  public NumericFitness compute(Node<String> phenotype) {
    boolean[] result = BooleanUtils.compute(BooleanUtils.transform(phenotype), cases, expectedResult.length);
    double errors = 0;
    for (int i = 0; i<expectedResult.length; i++) {
      errors = errors+((expectedResult[i]!=result[i])?1:0);
    }
    return new NumericFitness(errors);
  }

  @Override
  public NumericFitness worstValue() {
    return new NumericFitness(Math.pow(2, size)+1);
  }
  
}
