/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark;

import it.units.malelab.ege.core.LeavesJoiner;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.util.Utils;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author eric
 */
public class SantaFe extends Problem<String, NumericFitness> {

  public SantaFe(String target) throws IOException {
    super(Utils.parseFromFile(new File("grammars/santa-fe.bnf")),
            new it.units.malelab.ege.benchmark.pathfinding.SantaFe(),
            null,
            new LeavesJoiner<String>()
    );
  }

}
