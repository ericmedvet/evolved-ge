/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
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
public class Worker implements Runnable {

  public final static int INTERVAL = 5;

  private final String keyPhrase;
  private final InetAddress masterAddress;
  private final int masterPort;
  private final int nThreads;

  private final ScheduledExecutorService comExecutor;
  private final ExecutorService executor;
  private final Multimap<Job, Map<String, Object>> currentJobsData;

  private final static Logger L = Logger.getLogger(Worker.class.getName());

  public Worker(String keyPhrase, InetAddress masterAddress, int masterPort, int nThreads) {
    this.keyPhrase = keyPhrase;
    this.masterAddress = masterAddress;
    this.masterPort = masterPort;
    this.nThreads = nThreads;
    comExecutor = Executors.newSingleThreadScheduledExecutor();
    executor = Executors.newFixedThreadPool(nThreads);
    currentJobsData = (Multimap)Multimaps.synchronizedMultimap(ArrayListMultimap.create());
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
          L.fine(String.format("Handshake response sent with \"%s\".", challenge));
          //send updates
          synchronized (currentJobsData) { //to avoid losing data
            oos.writeObject(currentJobsData);
            ois.readObject();
            currentJobsData.clear();
          }
          //possibly ask for new jobs
          int freeThreads = nThreads;
          for (Job job : currentJobsData.keySet()) {
            freeThreads = freeThreads-job.getEstimatedMaxThreads();
          }
          oos.writeObject(new Integer(Math.max(0, freeThreads)));
          List<Job> newJobs = (List<Job>)ois.readObject();
          for (Job job : newJobs) {
            
          }
          //close
          socket.close();
        } catch (IOException ex) {
          L.log(Level.WARNING, String.format("Cannot connect to master: %s", ex.getMessage()), ex);
          ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
          L.log(Level.WARNING, String.format("Cannot decode response: %s", ex.getMessage()), ex);
        }
      }
    };
  }

}
