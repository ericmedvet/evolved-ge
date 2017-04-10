/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.selector;

import java.util.List;

/**
 *
 * @author eric
 */
public class LastWorst<T> implements Selector<T> {

  @Override
  public T select(List<List<T>> ts) {
    if (ts.isEmpty()) {
      return null;
    }
    if (ts.get(ts.size()-1).isEmpty()) {
      return null;
    }
    return ts.get(ts.size()-1).get(ts.get(ts.size()-1).size()-1);
  }

  @Override
  public String toString() {
    return "LastWorst{" + '}';
  }

}
