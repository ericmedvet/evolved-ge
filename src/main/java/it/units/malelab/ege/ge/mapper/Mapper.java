/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.mapper;

import it.units.malelab.ege.ge.genotype.Genotype;
import it.units.malelab.ege.core.grammar.Node;
import java.util.Map;

/**
 *
 * @author eric
 */
public interface Mapper<G extends Genotype, T> {
  
  public Node<T> map(G genotype, Map<String, Object> report) throws MappingException;
  
}
