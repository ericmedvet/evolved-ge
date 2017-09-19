/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import com.google.common.collect.Multimap;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.evolver.DeterministicCrowdingConfiguration;
import it.units.malelab.ege.core.evolver.DeterministicCrowdingEvolver;
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.evolver.PartitionConfiguration;
import it.units.malelab.ege.core.evolver.PartitionEvolver;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.evolver.StandardEvolver;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.collector.BestPrinter;
import it.units.malelab.ege.core.listener.collector.Collector;
import it.units.malelab.ege.core.listener.collector.Diversity;
import it.units.malelab.ege.core.listener.collector.NumericFirstBest;
import it.units.malelab.ege.core.listener.collector.Population;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author eric
 */
public class JobCallable implements Callable<List<List<Node>>> {

  private final Job job;
  private final JobDataListener jobDataListener;
  private final ExecutorService executorService;
  private final Multimap<Boolean, PrintStream> backupStreams;

  public JobCallable(Job job, JobDataListener jobDataListener, ExecutorService executorService, Multimap<Boolean, PrintStream> backupStreams) {
    this.job = job;
    this.jobDataListener = jobDataListener;
    this.executorService = executorService;
    this.backupStreams = backupStreams;
  }

  @Override
  public List<List<Node>> call() throws Exception {
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
    for (PrintStream ps : backupStreams.get(Boolean.TRUE)) {
      listeners.add(new CollectorGenerationLogger(job.getKeys(), ps, true, 10, " ", " | ", (Collector[])job.getCollectors().toArray()));
    }
    for (PrintStream ps : backupStreams.get(Boolean.FALSE)) {
      listeners.add(new CollectorGenerationLogger(job.getKeys(), ps, false, 0, ";", ";", (Collector[])job.getCollectors().toArray()));
    }
    //TODO add here custom listener connected to jobDataListener
    //TODO change backupstrams in stream factories and add header properly
    return evolver.solve(executorService, random, null);
  }

}
