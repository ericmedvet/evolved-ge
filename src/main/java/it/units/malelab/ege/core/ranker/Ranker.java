/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.ranker;

import java.util.List;

/**
 *
 * @author eric
 */
public interface Ranker<T> {
  
  public List<List<T>> rank(List<T> ts);
  
}
