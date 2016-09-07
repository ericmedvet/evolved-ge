/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distance;

import it.units.malelab.ege.Node;

/**
 *
 * @author eric
 */
// from https://github.com/unnonouno/tree-edit-distance/blob/master/tree-edit-distance/src/treedist/TreeEditDistance.java
public class TreeEditDistance<T> implements Distance<Node<T>>{

  @Override
  public double d(Node<T> t1, Node<T> t2) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  
  
}
