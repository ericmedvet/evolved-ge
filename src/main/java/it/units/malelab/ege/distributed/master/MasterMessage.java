/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.master;

import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.distributed.Job;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author eric
 */
public class MasterMessage implements Serializable {
  
  private final Set<Job> newJobs;

  public MasterMessage() {
    this.newJobs = new HashSet<Job>();
  }

  public Set<Job> getNewJobs() {
    return newJobs;
  }
  
}
