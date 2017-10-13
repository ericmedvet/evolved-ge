/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.master;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.distributed.Job;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class JobInfo {
  
  public enum Status {TO_DO, ONGOING, DONE};
  
  private Status status;
  private final Job job;
  private final List<Map<String, Object>> data;
  private List<List<Node>> results;

  public JobInfo(Job job) {
    this.job = job;
    status = Status.TO_DO;
    data = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Job getJob() {
    return job;
  }

  public List<Map<String, Object>> getData() {
    return data;
  }      

  public List<List<Node>> getResults() {
    return results;
  }

  public void setResults(List<List<Node>> results) {
    this.results = results;
  }
  
}
