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
import java.util.Random;

/**
 *
 * @author eric
 */
public class SGEMutation<T> extends AbstractMutation<SGEGenotype<T>> {
  
  private final SGEMapper<T> mapper;

  public SGEMutation(SGEMapper<T> mapper, Random random) {
    super(random);
    this.mapper = mapper;
  }

  @Override
  public List<SGEGenotype<T>> apply(List<SGEGenotype<T>> parents) {
    SGEGenotype<T> parent = parents.get(0);
    SGEGenotype<T> child = new SGEGenotype<>(parent);
    Pair<T, Integer> key = (Pair)child.getGenes().keySet().toArray()[random.nextInt(child.getGenes().keySet().size())];
    List<Integer> values = child.getGenes().get(key);
    List<Integer> bounds = mapper.getGenesBound().get(key);
    int index = random.nextInt(values.size());
    values.set(index, random.nextInt(bounds.get(index)));
    return Arrays.asList(child);
  }
  
}
