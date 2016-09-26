/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.operator;

import it.units.malelab.ege.BitsGenotype;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class OnePointCrossover extends AbstractCrossover<BitsGenotype> {

  private final Random random;

  public OnePointCrossover(Random random) {
    this.random = random;
  }
          
  @Override
  public List<BitsGenotype> apply(List<BitsGenotype> parents) {
    BitsGenotype parent1 = parents.get(0);
    BitsGenotype parent2 = parents.get(1);
    int cutPointIndex1 = Math.min(Math.max(1, random.nextInt(parent1.size())), parent1.size()-1);
    int cutPointIndex2 = Math.min(Math.max(1, random.nextInt(parent2.size())), parent2.size()-1);
    int child1Size = cutPointIndex1+(parent2.size()-cutPointIndex2);
    int child2Size = cutPointIndex2+(parent1.size()-cutPointIndex1);
    BitsGenotype child1 = new BitsGenotype(child1Size);
    BitsGenotype child2 = new BitsGenotype(child2Size);
    child1.set(0, parent1.slice(0, cutPointIndex1));
    child2.set(0, parent2.slice(0, cutPointIndex2));
    child1.set(cutPointIndex1, parent2.slice(cutPointIndex2, parent2.size()));
    child2.set(cutPointIndex2, parent1.slice(cutPointIndex1, parent1.size()));
    List<BitsGenotype> children = new ArrayList<>();
    children.add(child1);
    children.add(child2);
    return children;
  }
  
}
