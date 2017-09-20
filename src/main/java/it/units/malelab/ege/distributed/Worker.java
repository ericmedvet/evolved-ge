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
import it.units.malelab.ege.core.listener.AbstractListener;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
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

  public final static int INTERVAL = 2;

  private final String keyPhrase;
  private final InetAddress masterAddress;
  private final int masterPort;
  private final int nThreads;

  private final ScheduledExecutorService comExecutor;
  private final ExecutorService taskExecutor;
  private final ExecutorService runExecutor;
  private final Multimap<Job, Map<String, Object>> currentJobsData;
  private final Set<Job> currentJobs;
  private final Map<Job, List<List<Node>>> completedJobs;

  private final static Logger L = Logger.getLogger(Worker.class.getName());

  public Worker(String keyPhrase, InetAddress masterAddress, int masterPort, int nThreads) {
    this.keyPhrase = keyPhrase;
    this.masterAddress = masterAddress;
    this.masterPort = masterPort;
    this.nThreads = nThreads;
    comExecutor = Executors.newSingleThreadScheduledExecutor();
    taskExecutor = Executors.newFixedThreadPool(nThreads);
    runExecutor = Executors.newCachedThreadPool();
    currentJobsData = (Multimap)Multimaps.synchronizedMultimap(ArrayListMultimap.create());
    currentJobs = Collections.synchronizedSet(new HashSet<Job>());
    completedJobs = Collections.synchronizedMap(new HashMap<Job, List<List<Node>>>());
  }

  public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
    LogManager.getLogManager().readConfiguration(Master.class.getClassLoader().getResourceAsStream("logging.properties"));
    Worker worker = new Worker("hi", InetAddress.getLocalHost(), 9000, 3);
    worker.run();
  }

  @Override
  public void run() {
    comExecutor.scheduleAtFixedRate(getCommunicationRunnable(masterAddress, masterPort), 0, INTERVAL, TimeUnit.SECONDS);
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
            freeThreads = freeThreads-job.getEstimatedMaxThreads();
          }
          oos.writeObject(new Integer(Math.max(0, freeThreads)));
          List<Job> newJobs = (List<Job>)ois.readObject();
          for (Job job : newJobs) {
            L.info(String.format("Got new job: %s", job.getKeys()));
            currentJobs.add(job);
            runExecutor.submit(new JobRunnable(job, thisWorker));            
          }
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

  @Override
  public PrintStream build(List<String> keys) {
    return System.out; //TODO put file here
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
