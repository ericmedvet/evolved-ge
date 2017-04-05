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
public interface Selector<T extends Ranked> {
  
  public T select(List<T> ts);
  
}
