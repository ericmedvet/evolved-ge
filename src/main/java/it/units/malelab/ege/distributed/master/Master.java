/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.master;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Multimap;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.listener.collector.Collector;
import it.units.malelab.ege.distributed.DistributedUtils;
import it.units.malelab.ege.distributed.Job;
import it.units.malelab.ege.distributed.PrintStreamFactory;
import it.units.malelab.ege.util.Utils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class Master implements PrintStreamFactory {

  private final static int JOB_POLLING_INTERVAL = 1;

  public final static String LOCAL_TIME_NAME = "local.time";
  public final static String GENERATION_NAME = "generation";
  public final static String RANDOM_SEED_NAME = "random.seed";
  private final static Logger L = Logger.getLogger(Master.class.getName());

  private final String keyPhrase;
  private final int port;
  private final String baseResultFileName;

  private final ExecutorService mainExecutor;
  private final Map<List<String>, PrintStream> streams;
  
  private final List<Job> toDoJobs;
  private final Queue<LogRecord> logQueue;
  private final Map<String, String> jobKeyFormats;
  private final Map<String, String> collectorKeyFormats;

  private final Map<String, ClientInfo> clientInfos;
  private final Map<String, JobInfo> jobs;

  static {
    try {
      LogManager.getLogManager().readConfiguration(Master.class.getClassLoader().getResourceAsStream("logging.properties"));
    } catch (IOException ex) {
      //ignore
    } catch (SecurityException ex) {
      //ignore
    }
  }

  public Master(String keyPhrase, int port, String baseResultFileName) {
    this.keyPhrase = keyPhrase;
    this.port = port;
    this.baseResultFileName = baseResultFileName;
    this.mainExecutor = Executors.newCachedThreadPool();
    streams = Collections.synchronizedMap(new HashMap<List<String>, PrintStream>());
    toDoJobs = Collections.synchronizedList(new ArrayList<Job>());
    clientInfos = Collections.synchronizedMap(new TreeMap<String, ClientInfo>());
    jobs = Collections.synchronizedMap(new HashMap<String, JobInfo>());
    logQueue = EvictingQueue.create(UIRunnable.LOG_QUEUE_SIZE);
    jobKeyFormats = Collections.synchronizedMap(new TreeMap<String, String>());
    collectorKeyFormats = Collections.synchronizedMap(new LinkedHashMap<String, String>());
    collectorKeyFormats.put(GENERATION_NAME, "%3d");
  }

  public void start() {
    mainExecutor.submit(new ServerRunnable(this));
    //TODO build and add a job rescheduler
    DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
    Screen screen;
    try {
      screen = terminalFactory.createScreen();
      screen.startScreen();
      mainExecutor.submit(new UIRunnable(screen, this));
      //redirect logging
      LogManager.getLogManager().reset();
      LogManager.getLogManager().getLogger("").setLevel(Level.ALL);
      LogManager.getLogManager().getLogger("").addHandler(new Handler() {
        @Override
        public void publish(LogRecord record) {
          synchronized (logQueue) {
            if (record.getSourceClassName().startsWith("it.units")) {
              logQueue.add(record);
            }
          }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
      });
      L.fine("Starting logging in UI");
    } catch (IOException ex) {
      L.log(Level.SEVERE, String.format("Cannot start screen: will run in log-only mode: %s", ex), ex);
    }
  }

  public Future<List<List<Node>>> submit(final Job job) {
    jobs.put(job.getId(), new JobInfo(job));
    toDoJobs.add(job);
    //TODO move to UIRunnable
    //update job keys format    
    for (Map.Entry<String, Object> keyEntry : ((Map<String, Object>) job.getKeys()).entrySet()) {
      String currentFormat = String.format("%%%1$d.%1$ds", keyEntry.getValue().toString().length());
      if (!jobKeyFormats.containsKey(keyEntry.getKey())) {
        jobKeyFormats.put(keyEntry.getKey(), currentFormat);
      } else {
        String existingFormat = jobKeyFormats.get(keyEntry.getKey());
        if (Utils.formatSize(currentFormat) > Utils.formatSize(existingFormat)) {
          jobKeyFormats.put(keyEntry.getKey(), currentFormat);
        }
      }
    }
    //TODO move to UIRunnable
    //update collector format
    for (Collector collector : (List<Collector>) job.getCollectors()) {
      for (Map.Entry<String, String> formattedName : ((Map<String, String>) collector.getFormattedNames()).entrySet()) {
        if (!collectorKeyFormats.containsKey(formattedName.getKey())) {
          collectorKeyFormats.put(formattedName.getKey(), formattedName.getValue());
        } else {
          if (Utils.formatSize(formattedName.getValue()) > Utils.formatSize(collectorKeyFormats.get(formattedName.getKey()))) {
            collectorKeyFormats.put(formattedName.getKey(), formattedName.getValue());
          }
        }
      }
    }
    return new Future<List<List<Node>>>() {
      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean isCancelled() {
        return false;
      }

      @Override
      public boolean isDone() {
        return jobs.get(job.getId()).getStatus().equals(JobInfo.Status.DONE);
      }

      @Override
      public List<List<Node>> get() throws InterruptedException, ExecutionException {
        while (true) {
          try {
            return get(-1, TimeUnit.MILLISECONDS);
          } catch (TimeoutException ex) {
            //should not happen
          }
        }
      }

      @Override
      public List<List<Node>> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long elapsed = 0;
        while (true) {
          long m = System.currentTimeMillis();
          List<List<Node>> result = jobs.get(job.getId()).getResults();
          if (result != null) {
            return result;
          }
          Thread.sleep(JOB_POLLING_INTERVAL * 1000);
          m = System.currentTimeMillis() - m;
          elapsed = elapsed + m;
          if ((timeout > 0) && elapsed > unit.toMillis(timeout)) {
            throw new TimeoutException();
          }
        }
      }

    };
  }

  @Override
  public PrintStream build(List<String> keys) {
    String fileName = baseResultFileName + "-" + keys.hashCode() + ".txt";
    try {
      PrintStream ps = new PrintStream(fileName);
      DistributedUtils.writeHeader(ps, keys);
      return ps;
    } catch (FileNotFoundException ex) {
      L.log(Level.SEVERE, String.format("Cannot create file %s: %s", fileName, ex.getMessage()), ex);
    }
    return null;
  }

  public String getKeyPhrase() {
    return keyPhrase;
  }

  public int getPort() {
    return port;
  }

  public void startClientRunnable(Socket socket) {
    mainExecutor.submit(new ClientRunnable(socket, this));
  }

  public Map<String, ClientInfo> getClientInfos() {
    return clientInfos;
  }

  public void pushJobData(String jobId, Collection<Map<String, Object>> data) {
    Job job = jobs.get(jobId);
    if (job == null) {
      L.warning(String.format("Job \"%s\" does not exist!", jobId));
      return;
    }
    currentJobsData.putAll(job, data);
  }

  public void pushJobResults(String jobId, List<List<Node>> results) {
    Job job = jobs.get(jobId);
    if (job == null) {
      L.warning(String.format("Job \"%s\" does not exist!", jobId));
      return;
    }
    completedJobsResults.put(job, results);
    //save on disk and clear entry from ongoing jobs
    currentJobsData.keySet().remove(job);
  }

  public Job pullJob(int threads, String clientName) {
    ClientInfo clientInfo = clientInfos.get(clientName);
    if (clientInfo==null) {
      L.warning(String.format("Client \"%s\" does not exist!", clientName));
      return null;
    }
    Job chosenJob = null;
    synchronized (toDoJobs) {
      for (Job job : toDoJobs) {
        if (job.getEstimatedMaxThreads() <= threads) {
          chosenJob = job;
          break;
        }
      }
      if ((chosenJob==null)&&!toDoJobs.isEmpty()) {
        chosenJob = toDoJobs.get(0);
        toDoJobs.remove(chosenJob);
        clientInfo.getJobs().add(chosenJob);
      }
    }
    return chosenJob;
  }

  public List<Job> getToDoJobs() {
    return toDoJobs;
  }

  public Queue<LogRecord> getLogQueue() {
    return logQueue;
  }

  public Map<String, String> getJobKeyFormats() {
    return jobKeyFormats;
  }

  public Map<String, String> getCollectorKeyFormats() {
    return collectorKeyFormats;
  }

  public Multimap<Job, Map<String, Object>> getCurrentJobsData() {
    return currentJobsData;
  }

  public Map<Job, List<List<Node>>> getCompletedJobsResults() {
    return completedJobsResults;
  }
  
  public void shutdown() {
    mainExecutor.shutdownNow();
  }

}
