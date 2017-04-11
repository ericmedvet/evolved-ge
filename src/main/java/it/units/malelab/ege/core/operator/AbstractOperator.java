/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.operator;

import java.util.Random;

/**
 *
 * @author eric
 */
public abstract class AbstractOperator<G> implements GeneticOperator<G> {
  
  protected final Random random;

  public AbstractOperator(Random random) {
    this.random = random;
  }
  
}
