/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.genotype;

import it.units.malelab.ege.core.Factory;
import it.units.malelab.ege.util.Pair;
import it.units.malelab.ege.ge.mapper.SGEMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author eric
 */
public class SGEGenotypeFactory<T> implements Factory<SGEGenotype<T>> {

  private final SGEMapper<T> mapper;

  public SGEGenotypeFactory(SGEMapper<T> mapper) {
    this.mapper = mapper;
  }

  @Override
  public SGEGenotype<T> build(Random random) {
    SGEGenotype<T> genotype = new SGEGenotype<>();
    for (Map.Entry<Pair<T, Integer>, List<Integer>> entry : mapper.getGenesBound().entrySet()) {
      List<Integer> values = new ArrayList<>(entry.getValue().size());
      for (int bound : entry.getValue()) {
        values.add(random.nextInt(bound));
      }
      genotype.getGenes().put(entry.getKey(), values);
    }
    return genotype;
  }

  public int getBitSize() {
    double sum = 0;
    for (Map.Entry<Pair<T, Integer>, List<Integer>> entry : mapper.getGenesBound().entrySet()) {
      for (int bound : entry.getValue()) {
        sum = sum + Math.log10((double)bound)/Math.log10(2d);
      }
    }
    return (int) Math.round(sum);
  }

}
