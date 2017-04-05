/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core;

import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.grammar.Node;
import it.units.malelab.ege.core.listener.EvolverListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public interface Evolver<T, F extends Fitness> {
  
  public Configuration<T, F> getConfiguration();
  public List<Node<T>> solve(List<EvolverListener<T, F>> listeners) throws InterruptedException, ExecutionException;
  
}
