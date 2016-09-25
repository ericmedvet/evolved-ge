/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.operator;

import com.google.common.collect.Range;
import it.units.malelab.ege.Genotype;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class TwoPointsCrossover extends AbstractCrossover {

  protected final Random random;

  public TwoPointsCrossover(Random random) {
    this.random = random;
  }
          
  @Override
  public List<Genotype> apply(List<Genotype> parents) {
    Genotype parent1 = parents.get(0);
    Genotype parent2 = parents.get(1);
    int startIndex1 = Math.min(Math.max(1, random.nextInt(parent1.size())), parent1.size()-2);
    int startIndex2 = Math.min(Math.max(1, random.nextInt(parent2.size())), parent2.size()-2);
    int endIndex1 = Math.min(Math.max(startIndex1+1, random.nextInt(parent1.size())), parent1.size());
    int endIndex2 = Math.min(Math.max(startIndex2+1, random.nextInt(parent2.size())), parent2.size());
    return children(
            parent1, Range.openClosed(startIndex1, endIndex1),
            parent2, Range.openClosed(startIndex2, endIndex2));
  }
  
  protected List<Genotype> children(Genotype parent1, Range<Integer> range1, Genotype parent2, Range<Integer> range2) {
    int startIndex1 = range1.lowerEndpoint();
    int startIndex2 = range2.lowerEndpoint();
    int endIndex1 = range1.upperEndpoint();
    int endIndex2 = range2.upperEndpoint();
    int child1Size = parent1.size()-(endIndex1-startIndex1)+(endIndex2-startIndex2);
    int child2Size = parent2.size()-(endIndex2-startIndex2)+(endIndex1-startIndex1);
    Genotype child1 = new Genotype(child1Size);
    Genotype child2 = new Genotype(child2Size);
    if (startIndex1>0) {
      child1.set(0, parent1.slice(0, startIndex1));    
    }
    if (startIndex2>0) {
      child2.set(0, parent2.slice(0, startIndex2));
    }
    child1.set(startIndex1, parent2.slice(startIndex2, endIndex2));
    child2.set(startIndex2, parent1.slice(startIndex1, endIndex1));
    if (endIndex1<parent1.size()) {
      child1.set(startIndex1+endIndex2-startIndex2, parent1.slice(endIndex1, parent1.size()));
    }
    if (endIndex2<parent2.size()) {
      child2.set(startIndex2+endIndex1-startIndex1, parent2.slice(endIndex2, parent2.size()));
    }
    return Arrays.asList(child1, child2);    
  }
  
}
