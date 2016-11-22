/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class Uniform<T> implements Selector<T> {
  
  private final Random random;

  public Uniform(Random random) {
    this.random = random;
  }

  @Override
  public T select(List<T> ts) {
    return ts.get(random.nextInt(ts.size()));
  }
  
}
