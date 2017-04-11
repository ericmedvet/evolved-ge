/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.listener.collector;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.collector.PopulationInfoCollector;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.mapper.MultiMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class MultiMapperInfo<G extends BitsGenotype, T, F extends Fitness> implements PopulationInfoCollector<G, T, F> {

  private final int mappers;

  public MultiMapperInfo(int mappers) {
    this.mappers = mappers;
  }

  @Override
  public Map<String, Object> collect(List<List<Individual<G, T, F>>> rankedPopulation) {
    Individual<G, T, F> best = rankedPopulation.get(0).get(0);
    Map<String, Object> indexes = new LinkedHashMap<>();
    int[] counts = new int[mappers];
    double count = 0;
    for (List<Individual<G, T, F>> rank : rankedPopulation) {
      for (Individual<G, T, F> individual : rank) {
        Integer index = (Integer) individual.getOtherInfo().get(MultiMapper.MAPPER_INDEX_NAME);
        if (index != null) {
          counts[index] = counts[index] + 1;
          count = count + 1;
        }
      }
    }
    indexes.put("best.multimapper.i", best.getOtherInfo().get(MultiMapper.MAPPER_INDEX_NAME));
    for (int i = 0; i < mappers; i++) {
      indexes.put("pop.multimapper.freq." + i, (double) counts[i] / count);
    }
    return indexes;
  }

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("best.multimapper.i", "%1d");
    for (int i = 0; i < mappers; i++) {
      formattedNames.put("pop.multimapper.freq." + i, "%4.2f");
    }
    return formattedNames;
  }

}
