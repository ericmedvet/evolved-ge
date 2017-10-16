/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.master;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class ClientCheckerRunnable implements Runnable {

  private final Master master;

  public final static int INTERVAL = 10; //seconds
  private static final double MAX_DELAY_RATIO = 10.0d;

  private final static Logger L = Logger.getLogger(Master.class.getName());

  public ClientCheckerRunnable(Master master) {
    this.master = master;
  }

  @Override
  public void run() {
    Set<String> clientsToRemove = new HashSet<>();
    for (Map.Entry<String, ClientInfo> clientEntry : master.getClients().entrySet()) {
      String clientName = clientEntry.getKey();
      ClientInfo clientInfo = clientEntry.getValue();
      double interval = clientInfo.getLastMessage().getInterval();
      double elapsed = 0d;
      if (clientInfo.getLastContactDate() != null) {
        elapsed = (System.currentTimeMillis() - clientInfo.getLastContactDate().getTime()) / 1000;
      }
      if (elapsed > (interval * MAX_DELAY_RATIO)) {
        //reschedule client jobs
        for (JobInfo jobInfo : master.getJobs().values()) {
          if (clientName.equals(jobInfo.getClientName()) && JobInfo.Status.ONGOING.equals(jobInfo.getStatus())) {
            synchronized (jobInfo) {
              jobInfo.setClientName(null);
              jobInfo.setStatus(JobInfo.Status.TO_DO);
              if (jobInfo.getResults()!=null) {
                jobInfo.getResults().clear();
              }
              if (jobInfo.getData()!=null) {
                jobInfo.getData().clear();
              }
              L.warning(String.format("Rescheduling job %s of dead client %s.", jobInfo.getJob().getId(), clientName));
            }
          }
        }
        //mark to be removed
        clientsToRemove.add(clientName);
      }
    }
    if (clientsToRemove.isEmpty()) {
      L.fine("All clients are alive.");
    } else {
      synchronized (master.getClients()) {
        master.getClients().keySet().removeAll(clientsToRemove);
        L.warning(String.format("Marking %d clients as dead.", clientsToRemove.size()));
      }
    }
  }

}
