/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.operator;

import it.units.malelab.ege.Pair;
import it.units.malelab.ege.evolver.genotype.SGEGenotype;
import it.units.malelab.ege.mapper.SGEMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author eric
 */
public class SGEMutation<T> extends AbstractMutation<SGEGenotype<T>> {
  
  private final double p;
  private final SGEMapper<T> mapper;

  public SGEMutation(double p, SGEMapper<T> mapper, Random random) {
    super(random);
    this.p = p;
    this.mapper = mapper;
  }

  @Override
  public List<SGEGenotype<T>> apply(List<SGEGenotype<T>> parents) {
    SGEGenotype<T> parent = parents.get(0);
    SGEGenotype<T> child = new SGEGenotype<>(parent);
    for (Map.Entry<Pair<T, Integer>, List<Integer>> entry : child.getGenes().entrySet()) {
      for (int i = 0; i<entry.getValue().size(); i++) {
        if (random.nextDouble()<p) {
          int bound = mapper.getGenesBound().get(entry.getKey()).get(i);
          entry.getValue().set(i, random.nextInt(bound));
        }
      }
    }
    return Arrays.asList(child);
  }
  
}
