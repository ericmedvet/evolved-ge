/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.symbolicregression;

import it.units.malelab.ege.benchmark.fitness.SymbolicRegression;
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
public class Poly4 extends Problem<String, NumericFitness> {

  private final static SymbolicRegression.TargetFunction TARGET_FUNCTION = new SymbolicRegression.TargetFunction() {
    @Override
    public double compute(double... v) {
      double x = v[0];
      return x * x * x * x + x * x * x + x * x + x;
    }

    @Override
    public String[] varNames() {
      return new String[]{"x"};
    }
  };

  public Poly4() throws IOException {
    super(Utils.parseFromFile(new File("grammars/symbolic-regression-classic4.bnf")),
            new SymbolicRegression(
                    TARGET_FUNCTION,
                    new LinkedHashMap<>(MathUtils.valuesMap("x", MathUtils.equispacedValues(-1, 1, .1)))),
            null,
            MathUtils.phenotypePrinter());
  }

}
