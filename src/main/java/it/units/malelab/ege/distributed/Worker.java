/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import it.units.malelab.ege.core.Node;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.Socket;
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

  public final static int MASTER_INTERVAL = 5;
  public final static int STATS_INTERVAL = 1;

  public final static String STAT_CPU_SYSTEM_NAME = "cpu.system";
  public final static String STAT_CPU_PROCESS_NAME = "cpu.process";
  public final static String STAT_FREE_MEM_NAME = "memory.free";
  public final static String STAT_MAX_MEM_NAME = "memory.max";
  public final static String STAT_CORES = "cores";

  private final String keyPhrase;
  private final InetAddress masterAddress;
  private final int masterPort;
  private final int nThreads;
  private final String logDirectoryName;

  private final ScheduledExecutorService comExecutor;
  private final ExecutorService taskExecutor;
  private final ExecutorService runExecutor;
  private final Multimap<Job, Map<String, Object>> currentJobsData;
  private final Set<Job> currentJobs;
  private final Map<Job, List<List<Node>>> completedJobs;
  private final Multimap<String, Number> stats;

  private final static Logger L = Logger.getLogger(Worker.class.getName());
  private final static OperatingSystemMXBean OS = ManagementFactory.getOperatingSystemMXBean();
  private final static String WORKER_NAME = ManagementFactory.getRuntimeMXBean().getName();

  public Worker(String keyPhrase, InetAddress masterAddress, int masterPort, int nThreads, String logDirectoryName) {
    this.keyPhrase = keyPhrase;
    this.masterAddress = masterAddress;
    this.masterPort = masterPort;
    this.nThreads = nThreads;
    this.logDirectoryName = logDirectoryName;
    comExecutor = Executors.newSingleThreadScheduledExecutor();
    taskExecutor = Executors.newFixedThreadPool(nThreads);
    runExecutor = Executors.newCachedThreadPool();
    currentJobsData = (Multimap) Multimaps.synchronizedMultimap(ArrayListMultimap.create());
    currentJobs = Collections.synchronizedSet(new HashSet<Job>());
    completedJobs = Collections.synchronizedMap(new HashMap<Job, List<List<Node>>>());
    stats = (Multimap) Multimaps.synchronizedMultimap(ArrayListMultimap.create());
  }

  public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
    LogManager.getLogManager().readConfiguration(Master.class.getClassLoader().getResourceAsStream("logging.properties"));

    args = new String[]{"hi", "127.0.1.1", "9000", "/home/eric/experiments/ge/dist/log"};

    String keyPhrase = args[0];
    InetAddress masterAddress = InetAddress.getByName(args[1]);
    int masterPort = Integer.parseInt(args[2]);
    String logDir = null;
    if (args.length > 3) {
      logDir = args[3];
    }
    Worker worker = new Worker(keyPhrase, masterAddress, masterPort, Runtime.getRuntime().availableProcessors(), logDir);
    worker.run();
  }

  @Override
  public void run() {
    comExecutor.scheduleAtFixedRate(getCommunicationRunnable(masterAddress, masterPort), 0, MASTER_INTERVAL, TimeUnit.SECONDS);
    comExecutor.scheduleAtFixedRate(getStatsCollectorRunnable(), 0, STATS_INTERVAL, TimeUnit.SECONDS);
  }

  private Runnable getCommunicationRunnable(final InetAddress masterAddress, final int masterPort) {
    final Worker thisWorker = this;
    return new Runnable() {
      @Override
      public void run() {
        try (Socket socket = new Socket(masterAddress, masterPort);) {
          //handshake
          ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
          oos.flush();
          ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
          String challenge = DistributedUtils.decrypt((byte[]) ois.readObject(), keyPhrase);
          oos.writeObject(DistributedUtils.encrypt(DistributedUtils.reverse(challenge), keyPhrase));
          L.finer(String.format("Handshake response sent with \"%s\".", challenge));
          //send name
          oos.writeObject(WORKER_NAME);
          //send updates
          synchronized (currentJobsData) { //to avoid losing data
            oos.writeObject(currentJobsData);
            currentJobsData.clear();
          }
          //send job results
          synchronized (completedJobs) {
            oos.writeObject(completedJobs);
            completedJobs.clear();
          }
          //possibly ask for new jobs
          int freeThreads = nThreads;
          for (Job job : currentJobs) {
            freeThreads = freeThreads - job.getEstimatedMaxThreads();
          }
          oos.writeObject(new Integer(Math.max(0, freeThreads)));
          List<Job> newJobs = (List<Job>) ois.readObject();
          for (Job job : newJobs) {
            L.info(String.format("Got new job: %s", job.getKeys()));
            currentJobs.add(job);
            runExecutor.submit(new JobRunnable(job, thisWorker));
          }
          //collect and send stats
          Map<String, Number> avgStats = new HashMap<>();
          for (String statName : stats.keySet()) {
            double s = 0;
            for (Number v : stats.get(statName)) {
              s = s + v.doubleValue();
            }
            if (!stats.get(statName).isEmpty()) {
              avgStats.put(statName, s / stats.get(statName).size());
            }
          }
          oos.writeObject(avgStats);
          //close
          socket.close();
        } catch (IOException ex) {
          L.log(Level.WARNING, String.format("Cannot connect to master: %s", ex.getMessage()), ex);
        } catch (ClassNotFoundException ex) {
          L.log(Level.WARNING, String.format("Cannot decode response: %s", ex.getMessage()), ex);
        }
      }
    };
  }

  private Runnable getStatsCollectorRunnable() {
    return new Runnable() {
      @Override
      public void run() {
        stats.put(STAT_CPU_SYSTEM_NAME, OS.getSystemLoadAverage());
        stats.put(STAT_MAX_MEM_NAME, Runtime.getRuntime().maxMemory());
        stats.put(STAT_FREE_MEM_NAME, Runtime.getRuntime().freeMemory());
        stats.put(STAT_CORES, Runtime.getRuntime().availableProcessors());
      }
    };
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
      String fileName = WORKER_NAME + "-" + keys.hashCode() + ".txt";
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
    completedJobs.put(job, solutions);
  }

}
