/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core;

import it.units.malelab.ege.core.fitness.Fitness;

/**
 *
 * @author eric
 */
public interface Configuration<T, F extends Fitness> {
  
  public Problem<T, F> getProblem();
  
}
