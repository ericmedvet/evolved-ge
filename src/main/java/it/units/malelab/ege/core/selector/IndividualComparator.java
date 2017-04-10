/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.selector;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.fitness.NumericFitness;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author eric
 */
public class IndividualComparator<F extends Fitness> implements Comparator<Individual<?, F>> {

  public static enum Attribute {

    FITNESS, AGE, PHENO_SIZE, PHENO;
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
        if (i1.getFitness() instanceof NumericFitness) {
          v = ((NumericFitness) i1.getFitness()).getValue().compareTo(((NumericFitness) i2.getFitness()).getValue());
        } else {
          v = 0;
        }
      } else if (entry.getKey().equals(Attribute.AGE)) {
        v = -Integer.compare(i1.getBirthDate(), i2.getBirthDate());
      } else if (entry.getKey().equals(Attribute.PHENO_SIZE)) {
        v = Integer.compare(i1.getPhenotype().size(), i2.getPhenotype().size());
      } else if (entry.getKey().equals(Attribute.PHENO)) {
        v = i1.getPhenotype().equals(i2.getPhenotype()) ? 0 : -1;
      }
      if (entry.getValue()) {
        v = -v;
      }
      if (v != 0) {
        break;
      }
    }
    return v;
  }

  @Override
  public String toString() {
    return "IndividualComparator{" + "attributes=" + attributes + '}';
  }

}
