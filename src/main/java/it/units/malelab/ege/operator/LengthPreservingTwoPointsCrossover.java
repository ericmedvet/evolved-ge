/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.operator;

import com.google.common.collect.Range;
import it.units.malelab.ege.BitsGenotype;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class LengthPreservingTwoPointsCrossover extends TwoPointsCrossover {

  public LengthPreservingTwoPointsCrossover(Random random) {
    super(random);
  }
          
  @Override
  public List<BitsGenotype> apply(List<BitsGenotype> parents) {
    BitsGenotype parent1 = parents.get(0);
    BitsGenotype parent2 = parents.get(1);
    int startIndex1 = Math.min(Math.max(1, random.nextInt(parent1.size())), parent1.size()-2);
    int startIndex2 = Math.min(Math.max(1, random.nextInt(parent2.size())), parent2.size()-2);
    int crossoverSize = Math.max(1, random.nextInt(Math.min(parent1.size()-startIndex1, parent2.size()-startIndex2)));    
    int endIndex1 = startIndex1+crossoverSize;
    int endIndex2 = startIndex2+crossoverSize;
    return children(
            parent1, Range.openClosed(startIndex1, endIndex1),
            parent2, Range.openClosed(startIndex2, endIndex2));
  }
  
}
