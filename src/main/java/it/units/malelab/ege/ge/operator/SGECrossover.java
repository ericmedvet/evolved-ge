/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.operator;

import it.units.malelab.ege.core.operator.AbstractCrossover;
import it.units.malelab.ege.util.Pair;
import it.units.malelab.ege.ge.genotype.SGEGenotype;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author eric
 */
public class SGECrossover<T> extends AbstractCrossover<SGEGenotype<T>> {

  public SGECrossover(Random random) {
    super(random);
  }

  @Override
  public List<SGEGenotype<T>> apply(List<SGEGenotype<T>> parents) {
    SGEGenotype<T> parent0 = parents.get(0);
    SGEGenotype<T> parent1 = parents.get(1);
    SGEGenotype<T> child0 = new SGEGenotype<>(parent0);
    SGEGenotype<T> child1 = new SGEGenotype<>(parent1);
    for (Pair<T, Integer> key : child0.getGenes().keySet()) {
      if (random.nextBoolean()) {
        child0.getGenes().put(key, parent1.getGenes().get(key));
        child1.getGenes().put(key, parent0.getGenes().get(key));
      }
    }
    return Arrays.asList(child0, child1);
  }

}
