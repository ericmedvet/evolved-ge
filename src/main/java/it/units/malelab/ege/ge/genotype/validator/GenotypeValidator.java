/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.genotype.validator;

import it.units.malelab.ege.ge.genotype.Genotype;

/**
 *
 * @author eric
 */
public interface GenotypeValidator<G extends Genotype> {
  
    public boolean validate(G genotype);

}
