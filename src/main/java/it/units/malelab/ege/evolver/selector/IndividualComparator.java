/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import it.units.malelab.ege.evolver.Individual;
import java.util.Comparator;

/**
 *
 * @author eric
 */
public class IndividualComparator implements Comparator<Individual> {
  
  private final int birthDateComparatorMultiplier;

  public IndividualComparator(int useAge) {
    this.birthDateComparatorMultiplier = useAge;
  }    

  @Override
  public int compare(Individual i1, Individual i2) {
    int v = i1.getFitness().compareTo(i2.getFitness());
    if ((birthDateComparatorMultiplier!=0)&&(v==0)) {
      v = Integer.compare(i1.getBirthDate(), i2.getBirthDate())*birthDateComparatorMultiplier;
    }
    return v;
  }
  
}
