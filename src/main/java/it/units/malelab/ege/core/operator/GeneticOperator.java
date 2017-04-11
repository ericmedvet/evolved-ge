/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.operator;

import java.util.List;

/**
 *
 * @author eric
 */
public interface GeneticOperator<G> {
  
  public List<G> apply(List<G> parents);
  public int getParentsArity();
  public int getChildrenArity();
  
}
