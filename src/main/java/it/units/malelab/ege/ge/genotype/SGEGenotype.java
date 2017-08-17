/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.genotype;

import it.units.malelab.ege.core.ConstrainedSequence;
import it.units.malelab.ege.util.Pair;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author eric
 */
public class SGEGenotype<T> implements ConstrainedSequence<Integer> {

  private final Map<Pair<T, Integer>, List<Integer>> genes;
  private final Map<Integer, Pair<Pair<T, Integer>, Integer>> geneIndexes;
  private final Map<Pair<T, Integer>, List<Integer>> geneBounds;
  private final List<Set<Integer>> domains;

  public SGEGenotype(Map<Pair<T, Integer>, List<Integer>> genesBound) {
    this.geneBounds = genesBound;
    int counter = 0;
    geneIndexes = new LinkedHashMap<>();
    domains = new ArrayList<>();
    for (Map.Entry<Pair<T, Integer>, List<Integer>> entry : genesBound.entrySet()) {
      for (int i = 0; i < entry.getValue().size(); i++) {
        geneIndexes.put(counter, new Pair<>(entry.getKey(), i));
        Set<Integer> domain = new LinkedHashSet<>();
        for (int j = 0; j < entry.getValue().get(i); j++) {
          domain.add(j);
        }
        domains.add(domain);
        counter++;
      }
    }
    genes = new LinkedHashMap<>();
  }

  public SGEGenotype(SGEGenotype<T> genotype) {
    this(genotype.geneBounds);
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
    int length = 0;
    for (List<Integer> gene : genes.values()) {
      length = length + gene.size();
    }
    return length;
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

  @Override
  public Integer get(int index) {
    Pair<Pair<T, Integer>, Integer> geneIndex = geneIndexes.get(index);
    if (geneIndex==null) {
      throw new IndexOutOfBoundsException();
    }
    return genes.get(geneIndex.getFirst()).get(geneIndex.getSecond());
  }

  @Override
  public Set<Integer> domain(int index) {
    return domains.get(index);
  }

}
