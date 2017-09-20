/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.evolver.DeterministicCrowdingConfiguration;
import it.units.malelab.ege.core.evolver.DeterministicCrowdingEvolver;
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.evolver.PartitionConfiguration;
import it.units.malelab.ege.core.evolver.PartitionEvolver;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.evolver.StandardEvolver;
import it.units.malelab.ege.core.listener.AbstractListener;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.collector.Collector;
import it.units.malelab.ege.core.listener.event.EvolutionEvent;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class JobRunnable extends AbstractListener implements Runnable {

  private final Job job;
  private final Worker worker;
  
  private final static Logger L = Logger.getLogger(JobRunnable.class.getName());
  
  public JobRunnable(Job job, Worker worker) {
    super((Class) GenerationEvent.class);
    this.job = job;
    this.worker = worker;
  }

  @Override
  public void run() {
    L.fine(String.format("Starting job: %s", job.getKeys()));
    //prepare evolver
    Evolver evolver = null;
    if (job.getConfiguration().getClass().equals(StandardConfiguration.class)) {
      evolver = new StandardEvolver((StandardConfiguration) job.getConfiguration(), false);
    } else if (job.getConfiguration().getClass().equals(PartitionConfiguration.class)) {
      evolver = new PartitionEvolver((PartitionConfiguration) job.getConfiguration(), false);
    } else if (job.getConfiguration().getClass().equals(DeterministicCrowdingConfiguration.class)) {
      evolver = new DeterministicCrowdingEvolver((DeterministicCrowdingConfiguration) job.getConfiguration(), false);
    } else {
      throw new IllegalArgumentException(String.format("Configuration of type %s is unknown/unmanageable.", job.getConfiguration().getClass()));
    }
    //prepare random
    Integer randomSeed = (Integer) job.getKeys().get(Master.SEED_NAME);
    Random random = new Random();
    if (randomSeed != null) {
      random = new Random(randomSeed.longValue());
    }
    //prepare listeners
    List<EvolverListener> listeners = new ArrayList<>();
    listeners.add(new CollectorGenerationLogger(job.getKeys(), System.out, true, 10, " ", " | ", (Collector[])job.getCollectors().toArray()));
    //listeners.add(new CollectorGenerationLogger(job.getKeys(), worker.build(DistributedUtils.jobKeys(job)), false, 0, ";", ";", (Collector[])job.getCollectors().toArray()));
    listeners.add(this);
    try {
      List<List<Node>> finalBestRank = evolver.solve(worker.getTaskExecutor(), random, listeners);
      worker.notifyEndedJob(job, finalBestRank);
      L.fine(String.format("Ended job: %s", job.getKeys()));
      //TODO send result to worker
    } catch (InterruptedException ex) {
      L.log(Level.SEVERE, String.format("Interrupted job: %s", job.getKeys()), ex);
    } catch (ExecutionException ex) {
      L.log(Level.SEVERE, String.format("Exception in job: %s", job.getKeys()), ex);
    }
  }

  @Override
  public void listen(EvolutionEvent event) {
    Map<String, Object> data = new LinkedHashMap<>();
    int generation = ((GenerationEvent) event).getGeneration();
    data.put(Master.GENERATION_NAME, generation);
    data.put(Master.LOCAL_TIME_NAME, Calendar.getInstance().getTime());
    for (Collector collector : (List<Collector>)job.getCollectors()) {
      data.putAll(collector.collect((GenerationEvent) event));
    }
    worker.getCurrentJobsData().put(job, data);
  }

}
