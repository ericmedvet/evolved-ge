/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.ranker.Ranker;
import java.io.Serializable;

/**
 *
 * @author eric
 */
public interface Configuration<G, T, F extends Fitness> extends Serializable {
  
  public Problem<T, F> getProblem();
  public Mapper<G, T> getMapper();
  public Ranker<Individual<G, T, F>> getRanker();
  
}
