/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.validator;

import it.units.malelab.ege.Genotype;

/**
 *
 * @author eric
 */
public class AnyValidator<G extends Genotype> implements GenotypeValidator<G> {

  @Override
  public boolean validate(G genotype) {
    return true;
  }
  
}
