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
public class Copy extends AbstractMutation {

  @Override
  public List<Genotype> apply(List<Genotype> parents) {
    return parents;
  }
  
}
