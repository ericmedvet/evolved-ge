/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util.distance;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author eric
 */
public class CachedDistance<T> implements Distance<T> {

  private final LoadingCache<List<T>, Double> cache;

  private static final int CACHE_SIZE = 10000;

  public CachedDistance(final Distance<T> distance) {
    cache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(new CacheLoader<List<T>, Double>() {
      @Override
      public Double load(List<T> ts) throws Exception {
        return distance.d(ts.get(0), ts.get(1));
      }
    });
  }

  @Override
  public double d(T t1, T t2) {
    return cache.getUnchecked(Arrays.asList(t1, t2));
  }

}
