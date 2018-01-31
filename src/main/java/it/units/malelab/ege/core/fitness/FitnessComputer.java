/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.fitness;

import it.units.malelab.ege.core.Node;
import java.io.Serializable;

/**
 *
 * @author eric
 */
public interface FitnessComputer<T, F extends Fitness> extends Serializable {
  
  public F compute(Node<T> phenotype);
  public F worstValue();
  public F bestValue();
  
}
