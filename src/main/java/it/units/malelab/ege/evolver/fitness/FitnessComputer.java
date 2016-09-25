/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.fitness;

import it.units.malelab.ege.evolver.fitness.Fitness;
import it.units.malelab.ege.Node;

/**
 *
 * @author eric
 */
public interface FitnessComputer<T> {
  
  public Fitness compute(Node<T> phenotype);
  public Fitness worstValue();
  
}
