/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver;

import it.units.malelab.ege.grammar.Node;

/**
 *
 * @author eric
 */
public interface PhenotypePrinter<T> {
  
  public String toString(Node<T> node);
  
}
