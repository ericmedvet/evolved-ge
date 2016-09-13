/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.grammar.Grammar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class WeightedFractalMapper<T> extends FractalMapper<T> {

  private final Map<T, Integer> weightsMap;

  public WeightedFractalMapper(int maxZooms, Grammar<T> grammar) {
    super(maxZooms, grammar);
    weightsMap = new HashMap<>();
    for (List<List<T>> options : grammar.getRules().values()) {
      for (List<T> option : options) {
        for (T symbol : option) {
          if (!weightsMap.keySet().contains(symbol)) {
            weightsMap.put(symbol, countOptions(symbol, 0, maxZooms));
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
  protected Genotype getSlice(Genotype genotype, List<T> symbols, int index) {
    //it assumes that genotype.size()>=symbols.size()
    if (symbols.size()==1) {
      return genotype;
    }
    int[] sizes = new int[symbols.size()];
    int sumOfSizes = 0;
    int maxSizeIndex = 0;
    for (int i = 0; i<symbols.size(); i++) {
      sizes[i] = weightsMap.get(symbols.get(i));
      sumOfSizes = sumOfSizes+sizes[i];
      if (sizes[i]>sizes[maxSizeIndex]) {
        maxSizeIndex = i;
      }
    }
    int sumOfAdjustedSizes = 0;
    for (int i = 0; i<sizes.length; i++) {
      sizes[i] = (int)Math.max(Math.floor((double)sizes[i]/(double)sumOfSizes*(double)genotype.size()), 1);
      sumOfAdjustedSizes = sumOfAdjustedSizes+sizes[i];
    }
    sizes[maxSizeIndex] = sizes[maxSizeIndex]+(genotype.size()-sumOfAdjustedSizes);
    int toIndex = 0;
    for (int i = 0; i<=index; i++) {
      toIndex = toIndex+sizes[i];      
    }
    int fromIndex = (index==0)?0:(toIndex-sizes[index]);
    if (fromIndex==toIndex) {
      return null;
    }
    return genotype.slice(fromIndex, toIndex);
  }
  
}
