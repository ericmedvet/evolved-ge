/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util.distance;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Sequence;
import it.units.malelab.ege.util.Utils;

/**
 *
 * @author eric
 */
public class LeavesEdit<T> implements Distance<Node<T>> {
  
  private Distance<Sequence<T>> distance = new Edit<>();

  @Override
  public double d(Node<T> t1, Node<T> t2) {
    if (Node.EMPTY_TREE.equals(t1) || Node.EMPTY_TREE.equals(t2)) {
      return Math.max(t1.leafContents().size(), t2.leafContents().size());
    }
    return distance.d(t1.leafContents(), t2.leafContents());
  }

}
