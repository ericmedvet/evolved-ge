/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver;

import it.units.malelab.ege.evolver.listener.EvolutionListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public interface Evolver<T> {
  
  public Configuration<T> getConfiguration();
  public void go(List<EvolutionListener<T>> listeners) throws InterruptedException, ExecutionException;
  
}
