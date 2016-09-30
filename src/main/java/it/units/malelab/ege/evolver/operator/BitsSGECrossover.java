/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.operator;

import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.mapper.BitsSGEMapper;
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
    List<BitsGenotype> parent1Slices = parent1.slices(nonTerminalSizes);
    List<BitsGenotype> parent2Slices = parent2.slices(nonTerminalSizes);
    BitsGenotype child1 = new BitsGenotype(0);
    BitsGenotype child2 = new BitsGenotype(0);
    for (int i = 0; i<parent1Slices.size(); i++) {
      child1 = child1.append(((i==nonTerminalIndex)?parent2Slices:parent1Slices).get(i));
      child2 = child2.append(((i==nonTerminalIndex)?parent1Slices:parent2Slices).get(i));
    }
    return Arrays.asList(child1, child2);
  }
  
}
