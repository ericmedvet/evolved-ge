/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.worker;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.distributed.DistributedUtils;
import it.units.malelab.ege.distributed.Job;
import it.units.malelab.ege.distributed.master.Master;
import it.units.malelab.ege.distributed.PrintStreamFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class Worker implements Runnable, PrintStreamFactory {

  public final static int MASTER_INTERVAL = 10;
  public final static int STATS_INTERVAL = 1;

  private final String keyPhrase;
  private final InetAddress masterAddress;
  private final int masterPort;
  private final int maxThreads;
  private final String logDirectoryName;
  private final String name;
  private final int interval;

  private final ScheduledExecutorService comExecutor;
  private final ExecutorService taskExecutor;
  private final ExecutorService runExecutor;
  private final Multimap<Job, Map<String, Object>> currentJobsData;
  private final Set<Job> currentJobs;
  private final Map<Job, List<List<Node>>> completedJobsResults;
  private final Multimap<String, Number> stats;

  private final static Logger L = Logger.getLogger(Worker.class.getName());

  public Worker(String keyPhrase, InetAddress masterAddress, int masterPort, int nThreads, String logDirectoryName) {
    this.keyPhrase = keyPhrase;
    this.masterAddress = masterAddress;
    this.masterPort = masterPort;
    this.maxThreads = nThreads;
    this.logDirectoryName = logDirectoryName;
    comExecutor = Executors.newSingleThreadScheduledExecutor();
    taskExecutor = Executors.newFixedThreadPool(nThreads);
    runExecutor = Executors.newCachedThreadPool();
    currentJobsData = (Multimap) Multimaps.synchronizedMultimap(ArrayListMultimap.create());
    currentJobs = Collections.synchronizedSet(new HashSet<Job>());
    completedJobsResults = Collections.synchronizedMap(new HashMap<Job, List<List<Node>>>());
    stats = (Multimap) Multimaps.synchronizedMultimap(ArrayListMultimap.create());
    name = ManagementFactory.getRuntimeMXBean().getName();
    interval = MASTER_INTERVAL;
  }

  //java -cp EvolvedGrammaticalEvolution-1.0-SNAPSHOT.jar:. it.units.malelab.ege.distributed.Worker hi 35.194.17.98 9000 0 ./log
  public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
    LogManager.getLogManager().readConfiguration(Master.class.getClassLoader().getResourceAsStream("logging.properties"));
    String keyPhrase = args[0];
    InetAddress masterAddress = InetAddress.getByName(args[1]);
    int masterPort = Integer.parseInt(args[2]);
    int nThreads = Integer.parseInt(args[3]);
    if (nThreads<=0) {
      nThreads = Runtime.getRuntime().availableProcessors();
    }
    String logDir = null;
    if (args.length > 4) {
      logDir = args[4];
    }
    Worker worker = new Worker(keyPhrase, masterAddress, masterPort, nThreads, logDir);
    worker.run();
  }

  @Override
  public void run() {
    comExecutor.scheduleAtFixedRate(new CommunicationRunnable(this), 0, interval, TimeUnit.SECONDS);
    comExecutor.scheduleAtFixedRate(new StatsRunnable(this), 0, STATS_INTERVAL, TimeUnit.SECONDS);
  }

  @Override
  public PrintStream build(List<String> keys) {
    if (logDirectoryName != null) {
      //possibly create dir
      File logDir = new File(logDirectoryName);
      if (!logDir.exists()) {
        logDir.mkdir();
      }
      //create file
      String fileName = name + "-" + keys.hashCode() + ".txt";
      try {
        PrintStream ps = new PrintStream(logDirectoryName + File.separator + fileName);
        DistributedUtils.writeHeader(ps, keys);
        return ps;
      } catch (FileNotFoundException ex) {
        L.log(Level.SEVERE, String.format("Cannot create file %s: %s", fileName, ex.getMessage()), ex);
      }
    }
    return null;
  }

  public ExecutorService getTaskExecutor() {
    return taskExecutor;
  }

  public Multimap<Job, Map<String, Object>> getCurrentJobsData() {
    return currentJobsData;
  }

  public void notifyEndedJob(Job job, List<List<Node>> solutions) {
    currentJobs.remove(job);
    completedJobsResults.put(job, solutions);
  }

  public int getMaxThreads() {
    return maxThreads;
  }
  
  public int getFreeThreads() {
    int count = 0;
    for (Job job : currentJobs) {
      count = count+job.getEstimatedMaxThreads();
    }
    return Math.max(0, maxThreads-count);
  }

  public String getKeyPhrase() {
    return keyPhrase;
  }

  public InetAddress getMasterAddress() {
    return masterAddress;
  }

  public int getMasterPort() {
    return masterPort;
  }

  public String getName() {
    return name;
  }

  public int getInterval() {
    return interval;
  }

  public Multimap<String, Number> getStats() {
    return stats;
  }

  public Map<Job, List<List<Node>>> getCompletedJobsResults() {
    return completedJobsResults;
  }
  
  public void submitJob(Job job) {
    L.info(String.format("Got new job: %s", job.getKeys()));
    currentJobs.add(job);
    runExecutor.submit(new JobRunnable(job, this));    
  }

}
