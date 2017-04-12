/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.collector;

import it.units.malelab.ege.core.Sequence;
import it.units.malelab.ege.core.fitness.FitnessComputer;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author eric
 */
public class MultiObjectiveFitnessFirstBest<G extends Sequence, T> extends Best<G, T, MultiObjectiveFitness> {

  private final String[] fitnessFormats;

  public MultiObjectiveFitnessFirstBest(boolean ancestry, FitnessComputer<T, MultiObjectiveFitness> validationFitnessComputer, String... fitnessFormats) {
    super(ancestry, validationFitnessComputer);
    this.fitnessFormats = fitnessFormats;
  }

  @Override
  protected Map<String, String> getFitnessFormattedNames() {
    Map<String, String> formattedNames = new LinkedHashMap<>();
    for (int i = 0; i<fitnessFormats.length; i++) {
      formattedNames.put(""+i, fitnessFormats[i]);
    }
    return formattedNames;
  }

  @Override
  protected Map<String, Object> getFitnessIndexes(MultiObjectiveFitness fitness) {
    Map<String, Object> indexes = new LinkedHashMap<>();
    for (int i = 0; i<Math.min(fitnessFormats.length, fitness.getValue().length); i++) {
      indexes.put(""+i, fitness.getValue()[i]);
    }
    return indexes;
  }

}
