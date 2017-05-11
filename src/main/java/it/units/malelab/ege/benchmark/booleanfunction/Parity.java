/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.booleanfunction;

import it.units.malelab.ege.benchmark.fitness.ParityErrors;
import it.units.malelab.ege.core.Grammar;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.util.booleanfunction.BooleanUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author eric
 */
public class Parity extends Problem<String, NumericFitness> {

  public Parity(int size) throws IOException {
    super(
            buildGrammar(size),
            new ParityErrors(size),
            null,
            BooleanUtils.phenotypePrinter()
            );
  }
  
  private static Grammar<String> buildGrammar(int size) throws IOException {
    Grammar<String> grammar = Utils.parseFromFile(new File("grammars/boolean-parity-var.bnf"));
    List<List<String>> vars = new ArrayList<>();
    for (int i = 0; i<size; i++) {
      vars.add(Collections.singletonList("b"+i));
    }
    grammar.getRules().put("<v>", vars);
    return grammar;
  }
  
}
