/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import it.units.malelab.ege.evolver.Individual;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author eric
 */
public class IndividualComparator implements Comparator<Individual> {
    
  public static enum Attribute {
    FITNESS, AGE, GENO_SIZE, PHENO_SIZE, GENO, PHENO;
  }

  private final Map<Attribute, Boolean> attributes;

  public IndividualComparator(Map<Attribute, Boolean> attributes) {
    this.attributes = attributes;
  }
  
  public IndividualComparator(Attribute... attributes) {
    this.attributes = new LinkedHashMap<>();
    for (Attribute attribute : attributes) {
      this.attributes.put(attribute, false);
    }
  }

  @Override
  public int compare(Individual i1, Individual i2) {
    int v = -1;
    for (Map.Entry<Attribute, Boolean> entry : attributes.entrySet()) {
      if (entry.getKey().equals(Attribute.FITNESS)) {
        v = i1.getFitness().compareTo(i2.getFitness());
      } else if (entry.getKey().equals(Attribute.AGE)) {
        v = -Integer.compare(i1.getBirthDate(), i2.getBirthDate());
      } else if (entry.getKey().equals(Attribute.GENO_SIZE)) {
        v = Integer.compare(i1.getGenotype().size(), i2.getGenotype().size());
      } else if (entry.getKey().equals(Attribute.PHENO_SIZE)) {
        v = Integer.compare(i1.getPhenotype().size(), i2.getPhenotype().size());
      } else if (entry.getKey().equals(Attribute.GENO)) {
        v = i1.getGenotype().equals(i2.getGenotype())?0:-1;
      } else if (entry.getKey().equals(Attribute.PHENO)) {
        v = i1.getPhenotype().equals(i2.getPhenotype())?0:-1;
      }
      if (entry.getValue()) {
        v = -v;
      }
      if (v!=0) {
        break;
      }
    }
    return v;
  }
  
}
