/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import it.units.malelab.ege.grammar.Node;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class MultiMapper<T> implements Mapper<BitsGenotype, T> {
  
  public static enum SelectionCriterion {RESERVED_BITS, ALL_MODULE, ALL_BINARY, ALL_MAX};

  public static final String MAPPER_INDEX_NAME = "mapper.index";
  
  private final SelectionCriterion selectionCriterion;
  private final List<Mapper<BitsGenotype, T>> mappers;

  public MultiMapper(SelectionCriterion selectionCriterion, List<Mapper<BitsGenotype, T>> mappers) {
    this.selectionCriterion = selectionCriterion;
    this.mappers = mappers;
  }
  
  public MultiMapper(SelectionCriterion selectionCriterion, Mapper<BitsGenotype, T>... mappers) {
    this(selectionCriterion, Arrays.asList(mappers));
  }

  @Override
  public Node<T> map(BitsGenotype genotype, Map<String, Object> report) throws MappingException {
    int mapperIndex = 0;
    BitsGenotype innerGenotype = genotype;
    if (selectionCriterion.equals(SelectionCriterion.RESERVED_BITS)) {
      int mapperBits = (int) Math.ceil(Math.log10(mappers.size()) / Math.log10(2d));
      mapperIndex = genotype.slice(0, mapperBits).toInt()%mappers.size();
      innerGenotype = genotype.slice(mapperIndex, genotype.size());
    } else if (selectionCriterion.equals(SelectionCriterion.ALL_MODULE)) {
      mapperIndex = genotype.count()%mappers.size();
    } else if (selectionCriterion.equals(SelectionCriterion.ALL_BINARY)) {
      int mapperBits = (int) Math.ceil(Math.log10(mappers.size()) / Math.log10(2d));
      int index = 0;
      List<BitsGenotype> slices = genotype.slices(mapperBits);
      for (int i = 0; i<mapperBits; i++) {
        int value = Math.round(slices.get(i).count()/slices.get(i).size());
        index = index+value*(int)Math.pow(2, i);
      }
      mapperIndex = index%mappers.size();
    } else if (selectionCriterion.equals(SelectionCriterion.ALL_MAX)) {
      int maxIndex = 0;
      double maxValue = Double.NEGATIVE_INFINITY;
      List<BitsGenotype> slices = genotype.slices(mappers.size());
      for (int i = 0; i<slices.size(); i++) {
        double value = slices.get(i).count()/slices.get(i).size();
        if (value>maxValue) {
          maxValue = value;
          maxIndex = i;
        }
      }
      mapperIndex = maxIndex;
    }
    report.put(MAPPER_INDEX_NAME, mapperIndex);
    return mappers.get(mapperIndex).map(innerGenotype, report);
  }

  public List<Mapper<BitsGenotype, T>> getMappers() {
    return mappers;
  }

  @Override
  public String toString() {
    return "MultiMapper{" + "selectionCriterion=" + selectionCriterion + ", mappers=" + mappers + '}';
  }
    
}
