/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.Node;
import java.util.List;

/**
 *
 * @author eric
 */
public interface Mapper<T> {
  
  public Node<T> map(Genotype genotype) throws MappingException;
  
}
