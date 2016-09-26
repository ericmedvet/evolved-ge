/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.operator;

import it.units.malelab.ege.Genotype;
import java.util.List;

/**
 *
 * @author eric
 */
public class Copy<G extends Genotype> extends AbstractMutation<G> {

  @Override
  public List<G> apply(List<G> parents) {
    return parents;
  }
  
}
