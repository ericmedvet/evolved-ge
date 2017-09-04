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
import java.util.Random;

/**
 *
 * @author eric
 */
public class Nguyen7 extends Problem<String, NumericFitness> {

  private final static SymbolicRegression.TargetFunction TARGET_FUNCTION = new SymbolicRegression.TargetFunction() {
    @Override
    public double compute(double... v) {
      return Math.log(v[0] + 1) + Math.log(v[0] * v[0] + 1);
    }

    @Override
    public String[] varNames() {
      return new String[]{"x"};
    }
  };

  public Nguyen7(long seed) throws IOException {
    super(Utils.parseFromFile(new File("grammars/symbolic-regression-nguyen7.bnf")),
            new SymbolicRegression(
                    TARGET_FUNCTION,
                    new LinkedHashMap<>(MathUtils.valuesMap("x", MathUtils.uniformSample(0, 2, 20, new Random(seed))))),
            null,
            MathUtils.phenotypePrinter());
  }

}
