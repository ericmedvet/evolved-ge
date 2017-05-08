/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.cfggp.mapper;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.mapper.MappingException;
import java.util.Map;

/**
 *
 * @author eric
 */
public class CfgGpMapper<T> implements Mapper<Node<T>, T>{

  @Override
  public Node<T> map(Node<T> genotype, Map<String, Object> report) throws MappingException {
    return genotype;
  }
  
}
