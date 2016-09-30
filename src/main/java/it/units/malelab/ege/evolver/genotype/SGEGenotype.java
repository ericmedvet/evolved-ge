/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.genotype;

import it.units.malelab.ege.Pair;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class SGEGenotype<T> implements Genotype {
  
  private final Map<Pair<T, Integer>, List<Integer>> genes;

  public SGEGenotype() {
    genes = new LinkedHashMap<>();
  }

  public SGEGenotype(SGEGenotype<T> genotype) {
    this();
    for (Map.Entry<Pair<T, Integer>, List<Integer>> entry : genotype.genes.entrySet()) {
      genes.put(entry.getKey(), new ArrayList<>(entry.getValue()));
    }
  }

  public Map<Pair<T, Integer>, List<Integer>> getGenes() {
    return genes;
  }

  @Override
  public String toString() {
    return genes.toString();
  }

  @Override
  public int size() {
    int size = 0;
    for (List<Integer> gene : genes.values()) {
      size = size+gene.size();
    }
    return size;
  }
  
}
