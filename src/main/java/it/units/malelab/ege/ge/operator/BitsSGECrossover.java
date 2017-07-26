/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.operator;

import it.units.malelab.ege.core.operator.AbstractCrossover;
import com.google.common.collect.Range;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.mapper.BitsSGEMapper;
import it.units.malelab.ege.util.Utils;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class BitsSGECrossover extends AbstractCrossover<BitsGenotype> {

  private final List<Integer> nonTerminalSizes;
  private int overallSize;

  public BitsSGECrossover(BitsSGEMapper sgeMapper, Random random) {
    super(random);
    nonTerminalSizes = sgeMapper.getNonTerminalSizes();
    overallSize = 0;
    for (int size : nonTerminalSizes) {
      overallSize = overallSize+size;
    }
  }
          
  @Override
  public List<BitsGenotype> apply(List<BitsGenotype> parents) {
    BitsGenotype parent1 = parents.get(0);
    BitsGenotype parent2 = parents.get(1);
    if (Math.min(parent1.size(), parent2.size())<overallSize) {
      //should be an exception
      return parents;
    }
    int nonTerminalIndex = random.nextInt(nonTerminalSizes.size());
    List<BitsGenotype> parent1Slices = parent1.slices(Utils.slices(Range.closedOpen(0, parent1.size()), nonTerminalSizes));
    List<BitsGenotype> parent2Slices = parent2.slices(Utils.slices(Range.closedOpen(0, parent2.size()), nonTerminalSizes));
    BitsGenotype child1 = new BitsGenotype(0);
    BitsGenotype child2 = new BitsGenotype(0);
    for (int i = 0; i<parent1Slices.size(); i++) {
      child1 = child1.append(((i==nonTerminalIndex)?parent2Slices:parent1Slices).get(i));
      child2 = child2.append(((i==nonTerminalIndex)?parent1Slices:parent2Slices).get(i));
    }
    return Arrays.asList(child1, child2);
  }
  
}
