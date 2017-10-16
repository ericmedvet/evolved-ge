/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.worker;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.distributed.DistributedUtils;
import it.units.malelab.ege.distributed.Job;
import it.units.malelab.ege.distributed.master.MasterMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class CommunicationRunnable implements Runnable {

  private final Worker worker;

  private final static Logger L = Logger.getLogger(CommunicationRunnable.class.getName());

  public CommunicationRunnable(Worker worker) {
    this.worker = worker;
  }

  @Override
  public void run() {
    try (Socket socket = new Socket(worker.getMasterAddress(), worker.getMasterPort());) {
      //handshake
      ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
      oos.flush();
      ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
      String challenge = DistributedUtils.decrypt((byte[]) ois.readObject(), worker.getKeyPhrase());
      oos.writeObject(DistributedUtils.encrypt(DistributedUtils.reverse(challenge), worker.getKeyPhrase()));
      L.finer(String.format("Handshake response sent with \"%s\".", challenge));
      //average stats
      Map<String, Number> avgStats = new HashMap<>();
      for (String statName : worker.getStats().keySet()) {
        double s = 0;
        for (Number v : worker.getStats().get(statName)) {
          s = s + v.doubleValue();
        }
        if (!worker.getStats().get(statName).isEmpty()) {
          avgStats.put(statName, s / worker.getStats().get(statName).size());
        }
      }
      //prepare jobs data
      Multimap<String, Map<String, Object>> jobsData = ArrayListMultimap.create();
      synchronized (worker.getCurrentJobsData()) {
        for (Job job : worker.getCurrentJobsData().keySet()) {
          jobsData.putAll(job.getId(), worker.getCurrentJobsData().get(job));
        }
        worker.getCurrentJobsData().clear();
      }
      //prepare jobs result
      Map<String, List<Node>> jobsResults = new HashMap<>();
      synchronized (worker.getCompletedJobsResults()) {
        for (Map.Entry<Job, List<Node>> jobResultsEntry : worker.getCompletedJobsResults().entrySet()) {
          jobsResults.put(jobResultsEntry.getKey().getId(), jobResultsEntry.getValue());
        }
        worker.getCompletedJobsResults().clear();
      }
      //send worker message
      WorkerMessage workerMessage = new WorkerMessage(
              worker.getName(),
              worker.getInterval(),
              avgStats,
              worker.getFreeThreads(),
              worker.getMaxThreads(),
              jobsData,
              jobsResults);
      oos.writeObject(workerMessage);
      //read and consume master message
      MasterMessage masterMessage = (MasterMessage) ois.readObject();
      for (Job job : masterMessage.getNewJobs()) {
        worker.submitJob(job);
      }
      //close
      socket.close();
    } catch (IOException ex) {
      L.log(Level.SEVERE, String.format("Cannot connect to master: %s", ex.getMessage()), ex);
    } catch (ClassNotFoundException ex) {
      L.log(Level.SEVERE, String.format("Cannot decode response: %s", ex.getMessage()), ex);
    } catch (Throwable ex) {
      L.log(Level.SEVERE, String.format("Some error: %s", ex.getMessage()), ex);
    }
  }

}
