/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.fitness;

import it.units.malelab.ege.core.fitness.FitnessComputer;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.util.symbolicregression.MathUtils;
import java.util.Map;

/**
 *
 * @author eric
 */
public class SymbolicRegression implements FitnessComputer<String, NumericFitness> {
  
  public static interface TargetFunction {
    public double compute(double... arguments);
    public String[] varNames();
  }
  
  private final double[] targetValues;
  private final Map<String, double[]> varValues;

  public SymbolicRegression(TargetFunction targetFunction, Map<String, double[]> varValues) {
    this.varValues = varValues;
    targetValues = new double[varValues.get((String)varValues.keySet().toArray()[0]).length];
    for (int i = 0; i<targetValues.length; i++) {
      double[] arguments = new double[varValues.keySet().size()];
      for (int j = 0; j<targetFunction.varNames().length; j++) {
        arguments[j] = varValues.get(targetFunction.varNames()[j])[i];
      }
      targetValues[i] = targetFunction.compute(arguments);
    }
  }

  @Override
  public NumericFitness compute(Node<String> phenotype) {
    double[] computed = MathUtils.compute(MathUtils.transform(phenotype), varValues, targetValues.length);
    double mae = 0;
    for (int i = 0; i<targetValues.length; i++) {
      mae = mae+Math.abs(computed[i]-targetValues[i]);
    }
    return new NumericFitness(mae);
  }

  @Override
  public NumericFitness worstValue() {
    return new NumericFitness(Double.POSITIVE_INFINITY);
  }
  
}
