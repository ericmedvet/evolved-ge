/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.fitness;

import java.util.List;

/**
 *
 * @author eric
 */
public interface FitnessRanker<F extends Fitness> {
  
  public List<Integer> rank(List<F> fitnesses);
  
}
