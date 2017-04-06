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
public class LastWorst<T extends Ranked> implements Selector<T> {

  @Override
  public T select(List<T> ts) {
    T selectedT = ts.get(0);
    for (int i = 1; i < ts.size(); i++) {
      if (ts.get(i).getRank()>=selectedT.getRank()) {
        selectedT = ts.get(i);
      }
    }
    return selectedT;
  }

  @Override
  public String toString() {
    return "LastWorst{" + '}';
  }

}
