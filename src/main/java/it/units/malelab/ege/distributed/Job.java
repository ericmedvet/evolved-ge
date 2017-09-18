/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import it.units.malelab.ege.core.evolver.Configuration;
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.collector.Collector;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author eric
 */
public class Job<G, T, F extends Fitness> implements Serializable {
  
  private final Configuration<G, T, F> configuration;
  private final List<Collector<G, T, F>> collectors;
  private final int estimatedMaxThreads;

  public Job(Configuration<G, T, F> configuration, List<Collector<G, T, F>> collectors, int estimatedMaxThreads) {
    this.configuration = configuration;
    this.collectors = collectors;
    this.estimatedMaxThreads = estimatedMaxThreads;
  }

  public Configuration<G, T, F> getConfiguration() {
    return configuration;
  }

  public List<Collector<G, T, F>> getCollectors() {
    return collectors;
  }

  public int getEstimatedMaxThreads() {
    return estimatedMaxThreads;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + Objects.hashCode(this.configuration);
    hash = 53 * hash + Objects.hashCode(this.collectors);
    hash = 53 * hash + this.estimatedMaxThreads;
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
    if (!Objects.equals(this.configuration, other.configuration)) {
      return false;
    }
    if (!Objects.equals(this.collectors, other.collectors)) {
      return false;
    }
    if (this.estimatedMaxThreads != other.estimatedMaxThreads) {
      return false;
    }
    return true;
  }
  
}
