/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core;

import java.io.Serializable;

/**
 *
 * @author eric
 */
public interface PhenotypePrinter<T> extends Serializable {
  
  public String toString(Node<T> node);
  
}
