/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import it.units.malelab.ege.evolver.Individual;
import java.util.List;

/**
 *
 * @author eric
 */
public interface Selector {
  
  public <T> Individual<T> select(List<Individual<T>> population);
  
}
