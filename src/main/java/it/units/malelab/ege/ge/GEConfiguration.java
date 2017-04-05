/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge;

import it.units.malelab.ege.core.Configuration;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.ge.genotype.Genotype;
import it.units.malelab.ege.ge.mapper.Mapper;

/**
 *
 * @author eric
 */
public interface GEConfiguration<G extends Genotype, T, F extends Fitness> extends Configuration<T, F> {
  
  public Mapper<G, T> getMapper();
  
}
