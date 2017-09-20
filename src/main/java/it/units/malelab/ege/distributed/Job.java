/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import it.units.malelab.ege.core.evolver.Configuration;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.collector.Collector;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author eric
 */
public class Job<G, T, F extends Fitness> implements Serializable {
  
  private static final AtomicInteger atomicInteger = new AtomicInteger();
  
  private final String id;
  private final Configuration<G, T, F> configuration;
  private final List<Collector<G, T, F>> collectors;
  private final Map<String, Object> keys;
  private final int estimatedMaxThreads;

  public Job(Configuration<G, T, F> configuration, List<Collector<G, T, F>> collectors, Map<String, Object> keys, int estimatedMaxThreads) {
    this.configuration = configuration;
    this.collectors = collectors;
    this.keys = keys;
    this.estimatedMaxThreads = estimatedMaxThreads;
    id = UUID.randomUUID().toString()+"-"+atomicInteger.incrementAndGet();
  }

  public Configuration<G, T, F> getConfiguration() {
    return configuration;
  }

  public List<Collector<G, T, F>> getCollectors() {
    return collectors;
  }

  public Map<String, Object> getKeys() {
    return keys;
  }

  public int getEstimatedMaxThreads() {
    return estimatedMaxThreads;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 13 * hash + Objects.hashCode(this.id);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Job<?, ?, ?> other = (Job<?, ?, ?>) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Job{" + "id=" + id + ", keys=" + keys + '}';
  }
  
}
