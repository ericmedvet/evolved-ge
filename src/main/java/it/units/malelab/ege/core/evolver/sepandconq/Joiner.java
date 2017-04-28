/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver.sepandconq;

import it.units.malelab.ege.core.Node;
import java.util.List;

/**
 *
 * @author eric
 */
public interface Joiner<T> {
  
  public Node<T> join(List<Node<T>> pieces);
  
}
