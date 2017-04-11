/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark;

import it.units.malelab.ege.core.LeavesJoiner;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.LeafContentsDistance;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.util.distance.EditDistance;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author eric
 */
public class Text extends Problem<String, NumericFitness> {

  public Text() throws IOException {
    this("Hello world!");
  }

  public Text(String target) throws IOException {
    super(Utils.parseFromFile(new File("grammars/text.bnf")),
            new LeafContentsDistance<>(Arrays.asList(target.replace(" ", "_").split("")), new EditDistance<String>()),
            null,
            new LeavesJoiner<String>()
    );
  }

}
