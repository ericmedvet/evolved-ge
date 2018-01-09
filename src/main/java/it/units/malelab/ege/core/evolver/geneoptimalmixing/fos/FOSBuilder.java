/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver.geneoptimalmixing.fos;

import it.units.malelab.ege.core.ConstrainedSequence;
import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author eric
 */
public interface FOSBuilder extends Serializable {
  
  public Set<Set<Integer>> build(List<ConstrainedSequence> sequences, Random random);
  
}
