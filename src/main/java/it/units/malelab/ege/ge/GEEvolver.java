/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge;

import it.units.malelab.ege.core.Evolver;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.ge.genotype.Genotype;

/**
 *
 * @author eric
 */
public interface GEEvolver<G extends Genotype, T, F extends Fitness> extends Evolver<T, F> {
  
  public GEConfiguration<G, T, F> getGEConfiguration();
  
}
