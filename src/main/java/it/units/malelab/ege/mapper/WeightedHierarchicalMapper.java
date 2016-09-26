/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.grammar.Grammar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class WeightedHierarchicalMapper<T> extends HierarchicalMapper<T> {

  private final Map<T, Integer> weightsMap;

  public WeightedHierarchicalMapper(int maxDepth, Grammar<T> grammar) {
    super(grammar);
    weightsMap = new HashMap<>();
    for (List<List<T>> options : grammar.getRules().values()) {
      for (List<T> option : options) {
        for (T symbol : option) {
          if (!weightsMap.keySet().contains(symbol)) {
            weightsMap.put(symbol, countOptions(symbol, 0, maxDepth));
          }
        }
      }
    }
    for (T symbol : weightsMap.keySet()) {
      int options = weightsMap.get(symbol);
      int bits = (int) Math.ceil(Math.log10(options) / Math.log10(2d));
      weightsMap.put(symbol, bits);
    }
  }

  private int countOptions(T symbol, int level, int maxLevel) {
    List<List<T>> options = grammar.getRules().get(symbol);
    if (options==null) {
      return 1;
    }
    if (level>=maxLevel) {
      return options.size();
    }
    int count = 0;
    for (List<T> option : options) {
      for (T optionSymbol : option) {
        count = count+countOptions(optionSymbol, level+1, maxLevel);
      }      
    }
    return count;
  }

  @Override
  protected List<BitsGenotype> getSlices(BitsGenotype genotype, List<T> symbols) {
    if (symbols.size()>genotype.size()) {
      List<BitsGenotype> genotypes = new ArrayList<>(symbols.size());
      for (T symbol : symbols) {
        genotypes.add(new BitsGenotype(0));
      }
      return genotypes;
    }
    List<Integer> sizes = new ArrayList<>(symbols.size());
    int overallWeight = 0;
    for (T symbol : symbols) {
      overallWeight = overallWeight+weightsMap.get(symbol);
    }
    for (T symbol : symbols) {
      sizes.add((int)Math.floor((double)weightsMap.get(symbol)/(double)overallWeight*(double)genotype.size()));
    }
    return genotype.slices(sizes);
  }
  
}
