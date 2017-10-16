/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.worker;

import com.google.common.collect.Multimap;
import it.units.malelab.ege.core.Node;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class WorkerMessage implements Serializable {
  
  private final String name;
  private final int interval;
  private final Map<String, Number> stats;
  private final int freeThreads;
  private final int maxThreads;
  private final Multimap<String, Map<String, Object>> jobsData;
  private final Map<String, List<Node>> jobsResults;

  public WorkerMessage(String name, int interval, Map<String, Number> stats, int freeThreads, int maxThreads, Multimap<String, Map<String, Object>> jobsData, Map<String, List<Node>> jobsResults) {
    this.name = name;
    this.interval = interval;
    this.stats = stats;
    this.freeThreads = freeThreads;
    this.maxThreads = maxThreads;
    this.jobsData = jobsData;
    this.jobsResults = jobsResults;
  }

  public String getName() {
    return name;
  }

  public int getInterval() {
    return interval;
  }

  public Map<String, Number> getStats() {
    return stats;
  }

  public int getFreeThreads() {
    return freeThreads;
  }

  public int getMaxThreads() {
    return maxThreads;
  }

  public Multimap<String, Map<String, Object>> getJobsData() {
    return jobsData;
  }

  public Map<String, List<Node>> getJobsResults() {
    return jobsResults;
  }
  
}
