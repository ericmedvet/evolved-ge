/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.symbolicregression;

import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.util.symbolicregression.MathUtils;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

/**
 *
 * @author eric
 */
public class HarmonicCurve extends Problem<String, NumericFitness> {
  
  //AKA Keijzer-6

  private final static SymbolicRegression.TargetFunction TARGET_FUNCTION = new SymbolicRegression.TargetFunction() {
    @Override
    public double compute(double... v) {
      double s = 0;
      for (double i = 1; i < v[0]; i++) {
        s = s + 1 / i;
      }
      return s;
    }

    @Override
    public String[] varNames() {
      return new String[]{"x"};
    }
  };

  public HarmonicCurve() throws IOException {
    super(Utils.parseFromFile(new File("grammars/symbolic-regression-harmonic.bnf")),
            new SymbolicRegression(
                    TARGET_FUNCTION,
                    new LinkedHashMap<>(MathUtils.valuesMap("x", MathUtils.equispacedValues(1, 50, 1)))),
            new SymbolicRegression(
                    TARGET_FUNCTION,
                    new LinkedHashMap<>(MathUtils.valuesMap("x", MathUtils.equispacedValues(1, 120, 1)))),
            MathUtils.phenotypePrinter());
  }

}
