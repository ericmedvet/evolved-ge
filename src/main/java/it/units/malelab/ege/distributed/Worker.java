/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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

  private final ScheduledExecutorService comExecutor;

  private final static Logger L = Logger.getLogger(Worker.class.getName());

  public Worker(String keyPhrase, InetAddress masterAddress, int masterPort) {
    this.keyPhrase = keyPhrase;
    this.masterAddress = masterAddress;
    this.masterPort = masterPort;
    comExecutor = Executors.newSingleThreadScheduledExecutor();
  }

  public static void main(String[] args) throws UnknownHostException, IOException {
    LogManager.getLogManager().readConfiguration(Master.class.getClassLoader().getResourceAsStream("logging.properties"));
    Worker worker = new Worker("hi", InetAddress.getLocalHost(), 9000);
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
        try (Socket socket = new Socket(masterAddress, masterPort);
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ) {
          //handshake
          String challenge = DistributedUtils.decrypt((byte[]) ois.readObject(), keyPhrase);
          oos.writeObject(DistributedUtils.encrypt(DistributedUtils.reverse(challenge), keyPhrase));
          L.fine(String.format("Handshake response sent with \"%s\".", challenge));
          //send updates
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
