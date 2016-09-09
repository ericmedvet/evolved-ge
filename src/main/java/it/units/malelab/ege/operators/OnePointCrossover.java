/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.operators;

import it.units.malelab.ege.Genotype;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class OnePointCrossover implements GeneticOperator {

  private final Random random;

  public OnePointCrossover(Random random) {
    this.random = random;
  }
          
  @Override
  public List<Genotype> apply(List<Genotype> parents) {
    Genotype parent1 = parents.get(0);
    Genotype parent2 = parents.get(1);
    int cutPointIndex1 = Math.min(Math.max(1, random.nextInt(parent1.size())), parent1.size()-1);
    int cutPointIndex2 = Math.min(Math.max(1, random.nextInt(parent2.size())), parent2.size()-1);
    int child1Size = cutPointIndex1+(parent2.size()-cutPointIndex2);
    int child2Size = cutPointIndex2+(parent1.size()-cutPointIndex1);
    Genotype child1 = new Genotype(child1Size);
    Genotype child2 = new Genotype(child2Size);
    child1.set(0, parent1.slice(0, cutPointIndex1));
    child2.set(0, parent2.slice(0, cutPointIndex2));
    child1.set(cutPointIndex1, parent2.slice(cutPointIndex2, parent2.size()));
    child2.set(cutPointIndex2, parent1.slice(cutPointIndex1, parent1.size()));
    List<Genotype> children = new ArrayList<>();
    children.add(child1);
    children.add(child2);
    return children;
  }
  
}
