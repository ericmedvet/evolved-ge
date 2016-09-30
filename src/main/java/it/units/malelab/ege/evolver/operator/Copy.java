/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.operator;

import it.units.malelab.ege.evolver.genotype.Genotype;
import java.util.List;

/**
 *
 * @author eric
 */
public class Copy<G extends Genotype> implements GeneticOperator<G> {

  @Override
  public List<G> apply(List<G> parents) {
    return parents;
  }

  @Override
  public int getParentsArity() {
    return 1;
  }

  @Override
  public int getChildrenArity() {
    return 1;
  }
  
}
