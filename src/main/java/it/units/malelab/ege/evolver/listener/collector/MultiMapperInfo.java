/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener.collector;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.mapper.MultiMapper;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eric
 */
public class MultiMapperInfo<G extends Genotype, T> implements PopulationInfoCollector<G, T> {
  
  private final int mappers;

  public MultiMapperInfo(int mappers) {
    this.mappers = mappers;
  }  

  @Override
  public Map<String, Object> collect(List<Individual<G, T>> population) {
    Individual<G, T> best = population.get(0);
    Map<String, Object> indexes = new LinkedHashMap<>();
    int[] counts = new int[mappers];
    for (Individual<G, T> individual : population) {
      Integer index = (Integer)individual.getOtherInfo().get(MultiMapper.MAPPER_INDEX_NAME);
      if (index!=null) {
        counts[index] = counts[index]+1;
      }
      if (individual.getFitness().compareTo(best.getFitness())<0) {
        best = individual;
      }
    }
    indexes.put("best.multimapper.i", best.getOtherInfo().get(MultiMapper.MAPPER_INDEX_NAME));
    for (int i = 0; i<mappers; i++) {
      indexes.put("pop.multimapper.freq."+i, (double)counts[i]/(double)population.size());
    }
    return indexes;
  }    

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("best.multimapper.i", "%1d");
    for (int i = 0; i<mappers; i++) {
      formattedNames.put("pop.multimapper.freq."+i, "%4.2f");
    }    
    return formattedNames;
  }  
    
}
