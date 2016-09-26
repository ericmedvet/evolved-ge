/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.operator;

import it.units.malelab.ege.BitsGenotype;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author eric
 */
public class ProbabilisticMutation extends AbstractMutation<BitsGenotype> {

  private final Random random;
  private final double p;

  public ProbabilisticMutation(Random random, double p) {
    this.random = random;
    this.p = p;
  }

  @Override
  public List<BitsGenotype> apply(List<BitsGenotype> parents) {
    BitsGenotype parent = parents.get(0);
    BitsGenotype child = new BitsGenotype(parent.size());
    child.set(0, parent);
    for (int i = 0; i<child.size(); i++) {
      if (random.nextDouble()<p) {
        child.flip(i);
      }
    }
    return Collections.singletonList(child);
  }

}
