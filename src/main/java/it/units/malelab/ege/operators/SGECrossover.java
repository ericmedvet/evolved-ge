/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.operators;

import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.mapper.StructuralGEMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class SGECrossover implements GeneticOperator {

  private final List<Integer> nonTerminalSizes;
  private final Random random;
  private int overallSize;

  public SGECrossover(StructuralGEMapper sgeMapper, Random random) {
    nonTerminalSizes = sgeMapper.getNonTerminalSizes();
    this.random = random;
    overallSize = 0;
    for (int size : nonTerminalSizes) {
      overallSize = overallSize+size;
    }
  }
          
  @Override
  public List<Genotype> apply(List<Genotype> parents) {
    Genotype parent1 = parents.get(0);
    Genotype parent2 = parents.get(1);
    if (Math.min(parent1.size(), parent2.size())<overallSize) {
      //should be an exception
      return parents;
    }
    int nonTerminalIndex = random.nextInt(nonTerminalSizes.size());
    List<Genotype> parent1Slices = parent1.slices(nonTerminalSizes);
    List<Genotype> parent2Slices = parent2.slices(nonTerminalSizes);
    Genotype child1 = new Genotype(0);
    Genotype child2 = new Genotype(0);
    for (int i = 0; i<parent1Slices.size(); i++) {
      child1 = child1.append(((i==nonTerminalIndex)?parent2Slices:parent1Slices).get(i));
      child2 = child2.append(((i==nonTerminalIndex)?parent1Slices:parent2Slices).get(i));
    }
    return Arrays.asList(child1, child2);
  }
  
}
