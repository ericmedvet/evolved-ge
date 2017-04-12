/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.operator;

import it.units.malelab.ege.core.operator.AbstractCrossover;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class OnePointCrossover extends AbstractCrossover<BitsGenotype> {

  public OnePointCrossover(Random random) {
    super(random);
  }
          
  @Override
  public List<BitsGenotype> apply(List<BitsGenotype> parents) {
    BitsGenotype parent1 = parents.get(0);
    BitsGenotype parent2 = parents.get(1);
    int cutPointIndex1 = Math.min(Math.max(1, random.nextInt(parent1.length())), parent1.length()-1);
    int cutPointIndex2 = Math.min(Math.max(1, random.nextInt(parent2.length())), parent2.length()-1);
    int child1Size = cutPointIndex1+(parent2.length()-cutPointIndex2);
    int child2Size = cutPointIndex2+(parent1.length()-cutPointIndex1);
    BitsGenotype child1 = new BitsGenotype(child1Size);
    BitsGenotype child2 = new BitsGenotype(child2Size);
    child1.set(0, parent1.slice(0, cutPointIndex1));
    child2.set(0, parent2.slice(0, cutPointIndex2));
    child1.set(cutPointIndex1, parent2.slice(cutPointIndex2, parent2.length()));
    child2.set(cutPointIndex2, parent1.slice(cutPointIndex1, parent1.length()));
    List<BitsGenotype> children = new ArrayList<>();
    children.add(child1);
    children.add(child2);
    return children;
  }
  
}