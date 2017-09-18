/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.mapper;

import it.units.malelab.ege.core.Node;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author eric
 */
public interface Mapper<G, T> extends Serializable {
  
  public Node<T> map(G genotype, Map<String, Object> report) throws MappingException;
  
}
