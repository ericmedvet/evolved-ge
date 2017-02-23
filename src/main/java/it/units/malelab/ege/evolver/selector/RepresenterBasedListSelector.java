/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class RepresenterBasedListSelector<T> implements Selector<List<T>> {
  
  private final Selector<T> innerSelector;
  private final Selector<T> outerSelector;

  public RepresenterBasedListSelector(Selector<T> innerSelector, Selector<T> outerSelector) {
    this.innerSelector = innerSelector;
    this.outerSelector = outerSelector;
  }

  @Override
  public List<T> select(List<List<T>> partitions) {
    Map<T, List<T>> map = new LinkedHashMap<>();
    for (List<T> partition : partitions) {
      map.put(innerSelector.select(partition), partition);
    }
    return map.get(outerSelector.select(new ArrayList<T>(map.keySet())));
  }

  @Override
  public String toString() {
    return "RepresenterBasedListSelector{" + "innerSelector=" + innerSelector + ", outerSelector=" + outerSelector + '}';
  }  
  
}
