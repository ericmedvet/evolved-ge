/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark;

import it.units.malelab.ege.benchmark.fitness.RegexMatch;
import it.units.malelab.ege.core.LeavesJoiner;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.BinaryClassification;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.util.Utils;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author eric
 */
public class BinaryRegex extends Problem<String, MultiObjectiveFitness> {

  private final static BinaryClassification<String, String> DATASET = new RegexMatch("01", 20, 100, new Random(1l), "0+1?0+", "1010.+0101", "111.+", "1?0.+01?");

  public BinaryRegex() throws IOException {
    super(Utils.parseFromFile(new File("grammars/binary-regex.bnf")),
            DATASET.subset(0, 0.8),
            DATASET.subset(0.8, 1),
            new LeavesJoiner<String>()
    );
  }

}
