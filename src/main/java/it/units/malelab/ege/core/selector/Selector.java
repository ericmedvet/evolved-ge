/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.selector;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public interface Selector<T> extends Serializable {
  
  public T select(List<List<T>> ts, Random random);
  
}
