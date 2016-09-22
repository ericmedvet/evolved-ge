/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.operators;

import com.google.common.collect.Range;
import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.mapper.StructuralGEMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class SGECrossover extends TwoPointsCrossover {

  private final List<Range<Integer>> codonsRanges;
  private final int numberOfCodons;

  public SGECrossover(StructuralGEMapper sgeMapper, Random random) {
    super(random);
    codonsRanges = sgeMapper.getCodonsRanges();
    int max = Integer.MIN_VALUE;
    for (Range<Integer> range : codonsRanges) {
      max = Math.max(range.upperEndpoint(), max);
    }
    numberOfCodons = max;
  }
          
  @Override
  public List<Genotype> apply(List<Genotype> parents) {
    Genotype parent1 = parents.get(0);
    Genotype parent2 = parents.get(1);
    Range codonRange = codonsRanges.get(random.nextInt(codonsRanges.size()));
    Range<Integer> range1 = parent1.getRangeOfIndexedEqualSlices(codonRange, numberOfCodons);
    Range<Integer> range2 = parent2.getRangeOfIndexedEqualSlices(codonRange, numberOfCodons);
    if (range1.isEmpty()||range2.isEmpty()) {
      return Arrays.asList(
              new Genotype(parent1.size(), parent1.asBitSet()),
              new Genotype(parent2.size(), parent2.asBitSet()));
    }
    return children(parent1, range1, parent2, range2);
  }
  
}
