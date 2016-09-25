/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.operator;

/**
 *
 * @author eric
 */
public abstract class AbstractCrossover implements GeneticOperator {

  @Override
  public int getParentsArity() {
    return 2;
  }

  @Override
  public int getChildrenArity() {
    return 1;
  }
  
  
  
}
