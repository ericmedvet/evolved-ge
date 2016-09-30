/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.initializer;

import it.units.malelab.ege.evolver.validator.GenotypeValidator;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.util.List;

/**
 *
 * @author eric
 */
public interface PopulationInitializer<G extends Genotype> {
    
  public List<G> getGenotypes(int n, GenotypeValidator<G> genotypeValidator);
  
}
