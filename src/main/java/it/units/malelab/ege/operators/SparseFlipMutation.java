/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.operators;

import it.units.malelab.ege.Genotype;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author eric
 */
public class SparseFlipMutation implements GeneticOperator {
  
  private final Random random;

  public SparseFlipMutation(Random random) {
    this.random = random;
  }

  @Override
  public List<Genotype> apply(List<Genotype> parents) {
    Genotype parent = parents.get(0);
    Genotype child = new Genotype(parent.size());
    child.set(0, parent);
    int size = Math.max(1, random.nextInt(child.size()));
    Set<Integer> indexes = new HashSet<>();
    while (indexes.size()<size) {
      indexes.add(random.nextInt(child.size()));
    }
    for (int index : indexes) {
      child.flip(index);
    }
    return Collections.singletonList(child);
  }  
  
}
