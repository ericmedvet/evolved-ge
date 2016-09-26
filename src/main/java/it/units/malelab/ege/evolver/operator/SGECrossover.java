/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.operator;

import it.units.malelab.ege.Pair;
import it.units.malelab.ege.evolver.genotype.SGEGenotype;
import java.util.Arrays;
import java.util.List;
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
    SGEGenotype<T> parent1 = parents.get(0);
    SGEGenotype<T> parent2 = parents.get(1);
    Pair<T, Integer> key = (Pair)parent1.getGenes().keySet().toArray()[random.nextInt(parent1.getGenes().keySet().size())];
    List<Integer> values = parent2.getGenes().get(key);
    SGEGenotype<T> child1 = new SGEGenotype<>(parent1);
    SGEGenotype<T> child2 = new SGEGenotype<>(parent2);
    if (values!=null) {
      child1.getGenes().put(key, values);
      child2.getGenes().put(key, parent1.getGenes().get(key));
    }
    return Arrays.asList(child1, child2);
  }
  
}
