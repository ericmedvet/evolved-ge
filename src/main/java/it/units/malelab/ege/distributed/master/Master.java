/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.master;

import com.google.common.collect.EvictingQueue;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.distributed.DistributedUtils;
import it.units.malelab.ege.distributed.Job;
import it.units.malelab.ege.distributed.PrintStreamFactory;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
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
public class Master {

  private final static int JOB_POLLING_INTERVAL = 1;

  public final static String LOCAL_TIME_NAME = "local.time";
  public final static String GENERATION_NAME = "generation";
  public final static String CLIENT_NAME = "client";
  public final static String JOB_ID_NAME = "job.id";
  public final static String RANDOM_SEED_NAME = "random.seed";
    
  private final static Logger L = Logger.getLogger(Master.class.getName());

  private final String keyPhrase;
  private final int port;
  private final String baseResultFileName;

  private final ExecutorService mainExecutor;
  private final ScheduledExecutorService scheduledExecutor;

  private final Queue<LogRecord> logs;
  private final Map<String, ClientInfo> clients;
  private final Map<String, JobInfo> jobs;
  private final PrintStreamFactory printStreamFactory;

  static {
    try {
      LogManager.getLogManager().readConfiguration(Master.class.getClassLoader().getResourceAsStream("logging.properties"));
    } catch (IOException ex) {
      //ignore
    } catch (SecurityException ex) {
      //ignore
    }
  }

  public Master(String keyPhrase, int port, String baseResultDirName, String baseResultFileName) {
    this.keyPhrase = keyPhrase;
    this.port = port;
    this.baseResultFileName = baseResultFileName;
    mainExecutor = Executors.newCachedThreadPool();
    scheduledExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    clients = Collections.synchronizedMap(new TreeMap<String, ClientInfo>());
    jobs = Collections.synchronizedMap(new HashMap<String, JobInfo>());
    logs = EvictingQueue.create(UIRunnable.LOG_QUEUE_SIZE);
    printStreamFactory = new PrintStreamFactory(baseResultDirName);
  }

  public void start() {
    mainExecutor.submit(new ServerRunnable(this));
    scheduledExecutor.scheduleAtFixedRate(new ClientCheckerRunnable(this), 0, ClientCheckerRunnable.INTERVAL, TimeUnit.SECONDS);
    DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
    Screen screen;
    try {
      screen = terminalFactory.createScreen();
      screen.startScreen();
      mainExecutor.submit(new UIRunnable(screen, this));
      //redirect logging
      LogManager.getLogManager().reset();
      LogManager.getLogManager().getLogger("").setLevel(Level.INFO);
      LogManager.getLogManager().getLogger("").addHandler(new Handler() {
        @Override
        public void publish(LogRecord record) {
          synchronized (logs) {
            if (record.getSourceClassName().startsWith("it.units")) {
              logs.add(record);
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

  public Future<List<Node>> submit(final Job job) {
    jobs.put(job.getId(), new JobInfo(job));
    return new Future<List<Node>>() {
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
      public List<Node> get() throws InterruptedException, ExecutionException {
        while (true) {
          try {
            return get(-1, TimeUnit.MILLISECONDS);
          } catch (TimeoutException ex) {
            //should not happen
          }
        }
      }

      @Override
      public List<Node> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long elapsed = 0;
        while (true) {
          long m = System.currentTimeMillis();
          List<Node> result = jobs.get(job.getId()).getResults();
          jobs.get(job.getId()).setResults(null);
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

  public String getKeyPhrase() {
    return keyPhrase;
  }

  public int getPort() {
    return port;
  }

  public void startClientRunnable(Socket socket) {
    mainExecutor.submit(new ClientRunnable(socket, this));
  }

  public Map<String, ClientInfo> getClients() {
    return clients;
  }

  public void pushJobData(String jobId, Collection<Map<String, Object>> data) {
    JobInfo jobInfo = jobs.get(jobId);
    if (jobInfo == null) {
      L.warning(String.format("Job \"%s\" does not exist!", jobId));
      return;
    }
    jobInfo.getData().addAll(data);
  }

  public void pushJobResults(String jobId, List<Node> results) {
    JobInfo jobInfo = jobs.get(jobId);
    if (jobInfo == null) {
      L.warning(String.format("Job \"%s\" does not exist!", jobId));
      return;
    }
    jobInfo.setResults(results);
    //save on disk and clear entry from ongoing jobs
    PrintStream ps = printStreamFactory.get(DistributedUtils.jobKeys(jobInfo.getJob()), baseResultFileName);
    DistributedUtils.writeData(ps, jobInfo.getJob(), jobInfo.getData());
    jobInfo.getData().clear();    
    jobInfo.setStatus(JobInfo.Status.DONE);
  }

  public Job pullJob(int threads, String clientName) {
    ClientInfo clientInfo = clients.get(clientName);
    if (clientInfo == null) {
      L.warning(String.format("Client \"%s\" does not exist!", clientName));
      return null;
    }
    JobInfo chosenJobInfo = null;
    synchronized (jobs) {
      for (JobInfo jobInfo : jobs.values()) {
        if (jobInfo.getStatus().equals(JobInfo.Status.TO_DO) && (jobInfo.getJob().getEstimatedMaxThreads() <= threads)) {
          chosenJobInfo = jobInfo;
          break;
        }
      }
      if (chosenJobInfo == null) {
        for (JobInfo jobInfo : jobs.values()) {
          if (jobInfo.getStatus().equals(JobInfo.Status.TO_DO)) {
            chosenJobInfo = jobInfo;
            break;
          }
        }
      }
      if (chosenJobInfo!=null) {
        chosenJobInfo.setStatus(JobInfo.Status.ONGOING);
        chosenJobInfo.getData().clear();
        chosenJobInfo.setClientName(clientName);
      }
    }
    return chosenJobInfo.getJob();
  }

  public Queue<LogRecord> getLogs() {
    return logs;
  }

  public void shutdown() {
    mainExecutor.shutdownNow();
  }

  public Map<String, JobInfo> getJobs() {
    return jobs;
  }
  
  public Set<JobInfo> getClientJobIds(String clientName) {
    Set<JobInfo> jobInfos = Collections.synchronizedSet(new HashSet<JobInfo>());
    for (JobInfo jobInfo : jobs.values()) {
      if (JobInfo.Status.ONGOING.equals(jobInfo.getStatus())&&clientName.equals(jobInfo.getClientName())) {
        jobInfos.add(jobInfo);
      }
    }
    return jobInfos;
  }

  public PrintStreamFactory getPrintStreamFactory() {
    return printStreamFactory;
  }

}
