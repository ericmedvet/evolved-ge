/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver.sepandconq;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.evolver.PartitionEvolver;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.core.listener.EvolverListener;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class SACvolver<I, G, T, F extends MultiObjectiveFitness> extends PartitionEvolver<G, T, F>{
  
  public SACvolver(int numberOfThreads, SACConfiguration<I, G, T, F> configuration, Random random, boolean saveAncestry) {
    super(numberOfThreads, configuration, random, saveAncestry);
  }

  @Override
  public List<Node<T>> solve(List<EvolverListener<G, T, F>> listeners) throws InterruptedException, ExecutionException {
    
  }
  
  
  
}
