/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.operator;

import it.units.malelab.ege.evolver.genotype.Genotype;
import java.util.Random;

/**
 *
 * @author eric
 */
public abstract class AbstractOperator<G extends Genotype> implements GeneticOperator<G> {
  
  protected final Random random;

  public AbstractOperator(Random random) {
    this.random = random;
  }
  
}
