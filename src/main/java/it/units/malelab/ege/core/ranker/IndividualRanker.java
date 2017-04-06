/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.ranker;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import java.util.List;

/**
 *
 * @author eric
 */
public interface IndividualRanker<T, F extends Fitness> {
  
  public void rank(List<Individual<T, F>> individuals);
  
}
