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

  public static final String MAPPER_INDEX_NAME = "mapper.index";
  
  private final List<Mapper<BitsGenotype, T>> mappers;

  public MultiMapper(List<Mapper<BitsGenotype, T>> mappers) {
    this.mappers = mappers;
  }
  
  public MultiMapper(Mapper<BitsGenotype, T>... mappers) {
    this(Arrays.asList(mappers));
  }

  @Override
  public Node<T> map(BitsGenotype genotype, Map<String, Object> report) throws MappingException {
    int mapperBits = (int) Math.ceil(Math.log10(mappers.size()) / Math.log10(2d));
    int mapperIndex = genotype.slice(0, mapperBits).toInt()%mappers.size();
    report.put(MAPPER_INDEX_NAME, mapperIndex);
    return mappers.get(mapperIndex).map(genotype.slice(mapperIndex, genotype.size()), report);
  }

  public List<Mapper<BitsGenotype, T>> getMappers() {
    return mappers;
  }
    
}