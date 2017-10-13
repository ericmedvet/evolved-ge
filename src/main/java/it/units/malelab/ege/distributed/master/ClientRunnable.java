/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.master;

import it.units.malelab.ege.distributed.DistributedUtils;
import it.units.malelab.ege.distributed.Job;
import it.units.malelab.ege.distributed.worker.WorkerMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class ClientRunnable implements Runnable {

  private final Socket socket;
  private final Master master;

  private final static Logger L = Logger.getLogger(ClientRunnable.class.getName());

  public ClientRunnable(Socket socket, Master master) {
    this.socket = socket;
    this.master = master;
  }

  @Override
  public void run() {
    ObjectOutputStream oos = null;
    ObjectInputStream ois = null;
    try {
      oos = new ObjectOutputStream(socket.getOutputStream());
      oos.flush();
      ois = new ObjectInputStream(socket.getInputStream());
      //handshake
      String randomData = Double.toHexString((new Random()).nextDouble());
      oos.writeObject(DistributedUtils.encrypt(randomData, master.getKeyPhrase()));
      String reversed = DistributedUtils.decrypt((byte[]) ois.readObject(), master.getKeyPhrase());
      if (!DistributedUtils.reverse(reversed).equals(randomData)) {
        L.warning(String.format("Client %s:%d did not correctly replied to the challenge!",
                socket.getInetAddress(),
                socket.getPort(), randomData));
        throw new SecurityException("Client did not correctly replied to the challenge!");
      }
      L.finer(String.format("Client %s:%d completed andshake correctly with \"%s\".", socket.getInetAddress(), socket.getPort(), randomData));
      //read worker message
      WorkerMessage workerMessage = (WorkerMessage) ois.readObject();
      ClientInfo clientInfo = master.getClientInfos().get(workerMessage.getName());
      if (clientInfo == null) {
        clientInfo = new ClientInfo();
        master.getClientInfos().put(workerMessage.getName(), clientInfo);
      }
      clientInfo.setLastMessage(workerMessage);
      clientInfo.setLastContactDate(new Date());
      //get updates
      int dataItemsCount = 0;
      for (String jobId : workerMessage.getJobsData().keySet()) {
        Collection<Map<String, Object>> data = workerMessage.getJobsData().get(jobId);
        dataItemsCount = dataItemsCount + data.size();
        master.pushJobData(jobId, data);
      }
      L.fine(String.format("Client %s sent %d data items from %d jobs.",
              workerMessage.getName(),
              dataItemsCount,
              workerMessage.getJobsData().keySet().size()));
      //get results
      for (String jobId : workerMessage.getJobsResults().keySet()) {
        master.pushJobResults(jobId, workerMessage.getJobsResults().get(jobId));
      }
      L.fine(String.format("Client %s sent %d results.",
              workerMessage.getName(),
              workerMessage.getJobsResults().size()));
      L.fine(String.format("Client %s would accept jobs for %d threads.",
              workerMessage.getName(),
              workerMessage.getFreeThreads()));
      //prepare master message
      MasterMessage masterMessage = new MasterMessage();
      int freeThreads = workerMessage.getFreeThreads();
      while (freeThreads > 0) {
        Job job = master.pullJob(freeThreads, workerMessage.getName());
        if (job != null) {
          masterMessage.getNewJobs().add(job);
          freeThreads = freeThreads - job.getEstimatedMaxThreads();
        } else {
          break;
        }
      }
      if (!masterMessage.getNewJobs().isEmpty()) {
        L.info(String.format("Sending %d jobs to client %s.",
                masterMessage.getNewJobs().size(),
                workerMessage.getName()));
      }
      oos.writeObject(masterMessage);
    } catch (IOException ex) {
      L.log(Level.WARNING, String.format("Cannot connect to client: %s", ex.getMessage()), ex);
      
      ex.printStackTrace();
      
    } catch (ClassNotFoundException ex) {
      L.log(Level.WARNING, String.format("Cannot decode response: %s", ex.getMessage()), ex);
    } finally {
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException ex) {
        }
      }
    }
  }

}
