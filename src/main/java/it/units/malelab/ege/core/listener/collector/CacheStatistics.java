/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.collector;

import com.google.common.cache.CacheStats;
import it.units.malelab.ege.core.evolver.StandardEvolver;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author eric
 */
public class CacheStatistics implements Collector {

  @Override
  public Map<String, Object> getFormattedNames() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("cache.mapping.hit.rate", "%4.2f");
    map.put("cache.mapping.avg.load.penalty", "%4.0f");
    map.put("cache.fitness.miss.count", "%8d");
    map.put("cache.fitness.hit.rate", "%4.2f");
    map.put("cache.fitness.avg.load.penalty", "%8.0f");
    return map;
  }

  @Override
  public Map<String, Object> collect(GenerationEvent generationEvent) {
    CacheStats mappingStats = (CacheStats)generationEvent.getData().get(StandardEvolver.MAPPING_CACHE_NAME);
    CacheStats fitnessStats = (CacheStats)generationEvent.getData().get(StandardEvolver.FITNESS_CACHE_NAME);
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("cache.mapping.hit.rate", mappingStats.hitRate());
    map.put("cache.mapping.avg.load.penalty", mappingStats.averageLoadPenalty()/1000);
    map.put("cache.fitness.miss.count", fitnessStats.missCount());
    map.put("cache.fitness.hit.rate", fitnessStats.hitRate());
    map.put("cache.fitness.avg.load.penalty", fitnessStats.averageLoadPenalty()/1000);
    return map;
  }
  
}
