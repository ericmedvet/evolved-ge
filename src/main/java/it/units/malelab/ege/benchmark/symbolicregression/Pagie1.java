/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.symbolicregression;

import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.util.Utils;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

/**
 *
 * @author eric
 */
public class Pagie1 extends Problem<String, NumericFitness> {

  private final static SymbolicRegression.TargetFunction TARGET_FUNCTION = new SymbolicRegression.TargetFunction() {
    @Override
    public double compute(double... v) {
      return 1 / (1 + Math.pow(v[0], -4)) + 1 / (1 + Math.pow(v[1], -4));
    }

    @Override
    public String[] varNames() {
      return new String[]{"x", "y"};
    }
  };

  public Pagie1() throws IOException {
    super(Utils.parseFromFile(new File("grammars/symbolic-regression-pagie1.bnf")),
            new SymbolicRegression(
                    TARGET_FUNCTION,
                    new LinkedHashMap<>(MathUtils.combinedValuesMap(
                            MathUtils.valuesMap("x", MathUtils.equispacedValues(-5, 5, 0.4)),
                            MathUtils.valuesMap("y", MathUtils.equispacedValues(-5, 5, 0.4))
                    ))),
            new SymbolicRegression(
                    TARGET_FUNCTION,
                    new LinkedHashMap<>(MathUtils.combinedValuesMap(
                            MathUtils.valuesMap("x", MathUtils.equispacedValues(-5, 5, 0.1)),
                            MathUtils.valuesMap("y", MathUtils.equispacedValues(-5, 5, 0.1))
                    ))),
            MathUtils.phenotypePrinter());
  }

}
