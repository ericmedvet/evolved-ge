/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.evolver.Configuration;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.EvolverListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public interface Evolver<G, T, F extends Fitness> {
  
  public Configuration<G, T, F> getConfiguration();
  public List<Node<T>> solve(List<EvolverListener<G, T, F>> listeners) throws InterruptedException, ExecutionException;
  
}
