/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.grammar.Node;

/**
 *
 * @author eric
 */
public interface Mapper<G extends Genotype, T> {
  
  public Node<T> map(G genotype) throws MappingException;
  
}
