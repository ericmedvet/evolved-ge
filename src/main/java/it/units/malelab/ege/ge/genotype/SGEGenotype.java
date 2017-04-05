/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.genotype;

import it.units.malelab.ege.util.Pair;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 31 * hash + Objects.hashCode(this.genes);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SGEGenotype<?> other = (SGEGenotype<?>) obj;
    if (!Objects.equals(this.genes, other.genes)) {
      return false;
    }
    return true;
  }
  
}
