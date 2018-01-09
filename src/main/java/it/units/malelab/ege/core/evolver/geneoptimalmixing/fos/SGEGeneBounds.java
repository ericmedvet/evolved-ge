/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver.geneoptimalmixing.fos;

import it.units.malelab.ege.core.ConstrainedSequence;
import it.units.malelab.ege.ge.mapper.SGEMapper;
import it.units.malelab.ege.util.Pair;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author eric
 */
public class SGEGeneBounds implements FOSBuilder {
  
  private final SGEMapper mapper;

  public SGEGeneBounds(SGEMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Set<Set<Integer>> build(List<ConstrainedSequence> sequences, Random random) {
    Map<Pair<?, Integer>, List<Integer>> geneBounds = mapper.getGeneBounds();
    Set<Set<Integer>> fos = new LinkedHashSet<>();
    int c = 0;
    for (List<Integer> bounds : geneBounds.values()) {
      Set<Integer> subset = new LinkedHashSet<>();
      for (Integer bound : bounds) {
        subset.add(c);
        fos.add(Collections.singleton(c));
        c = c+1;
      }
      fos.add(subset);
    }
    return fos;
  }    
  
}
