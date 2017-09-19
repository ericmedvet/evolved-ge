/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import com.google.common.collect.Multimap;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class Master implements Runnable {

  private final static int MAX_CLIENTS = 32;
  public final static String SEED_NAME = "random.seed";

  private final String keyPhrase;
  private final int port;
  private final PrintStreamFactory printStreamFactory;

  private final ExecutorService executor;
  private final Random random;
  private final Map<List<String>, PrintStream> streams;
  
  private final List<Job> toDoJobs;

  private final static Logger L = Logger.getLogger(Master.class.getName());

  public Master(String keyPhrase, int port, PrintStreamFactory printStreamFactory) {
    this.keyPhrase = keyPhrase;
    this.port = port;
    this.printStreamFactory = printStreamFactory;
    this.executor = Executors.newFixedThreadPool(MAX_CLIENTS);
    random = new Random();
    streams = Collections.synchronizedMap(new HashMap<List<String>, PrintStream>());
    toDoJobs = Collections.synchronizedList(new ArrayList<Job>());
  }

  public static void main(String[] args) throws IOException {
    LogManager.getLogManager().readConfiguration(Master.class.getClassLoader().getResourceAsStream("logging.properties"));
    Master master = new Master("hi", 9000, new PrintStreamFactory() {
      @Override
      public PrintStream build(List<String> keys) {
        return System.out;
      }
    });
    master.run();
  }

  @Override
  public void run() {
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(port);
      L.fine(String.format("Listening on port %d.", serverSocket.getLocalPort()));
    } catch (IOException ex) {
      L.log(Level.SEVERE, String.format("Cannot start server socket on port %d.", port), ex);
      System.exit(-1);
    }
    while (true) {
      try {
        Socket socket = serverSocket.accept();
        L.fine(String.format("Connection from %s:%d.", socket.getInetAddress(), socket.getPort()));
        executor.submit(getServerRunnable(socket));
      } catch (IOException ex) {
        L.log(Level.WARNING, String.format("Cannot accept socket: %s", ex.getMessage()), ex);
      }
    }
  }

  private Runnable getServerRunnable(final Socket socket) {
    return new Runnable() {
      @Override
      public void run() {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
          oos = new ObjectOutputStream(socket.getOutputStream());
          oos.flush();
          ois = new ObjectInputStream(socket.getInputStream());
          //handshake
          String randomData = Double.toHexString(random.nextDouble());
          oos.writeObject(DistributedUtils.encrypt(randomData, keyPhrase));
          String reversed = DistributedUtils.decrypt((byte[]) ois.readObject(), keyPhrase);
          if (!DistributedUtils.reverse(reversed).equals(randomData)) {
            throw new SecurityException("Client did not correctly replied to the challenge!");
          }
          L.fine(String.format("Handshake correctly completed with \"%s\".", randomData));
          //get updates
          int dataItemsCount = 0;
          Multimap<Job, Map<String, Object>> jobData = (Multimap<Job, Map<String, Object>>)ois.readObject();
          for (Job job : jobData.keySet()) {
            for (Map<String, Object> dataItem : jobData.get(job)) {
              List<String> streamKey = DistributedUtils.merge(job.getKeys().keySet(), dataItem.keySet());
              PrintStream ps = streams.get(streamKey);
              if (ps==null) {
                L.fine(String.format("Building new stream for %s.", streamKey));
                ps = printStreamFactory.build(streamKey);
                streams.put(streamKey, ps);
              }
              dataItemsCount = dataItemsCount+1;
              //TODO write seriously
              ps.print(dataItem);
            }
          }
          oos.writeObject(Boolean.TRUE);
          L.fine(String.format("Received %d data items.", dataItemsCount));
          //possibly assign new jobs
          Integer freeRemoteThreads = (Integer)ois.readObject();
          if (freeRemoteThreads>0) {
            //send one job (might be improved w/ better choice)
            synchronized (toDoJobs) {
              int index = random.nextInt(toDoJobs.size());
              Job job = toDoJobs.get(index);
              toDoJobs.remove(job);
              oos.writeObject(Collections.singletonList(job));
            }
          } else {
            oos.writeObject(Collections.EMPTY_LIST);
          }
        } catch (IOException ex) {
          L.log(Level.WARNING, String.format("Cannot build Object streams: %s", ex.getMessage()), ex);
          ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
          L.log(Level.WARNING, String.format("Cannot decode response: %s", ex.getMessage()), ex);
        } finally {
          if (socket!=null) {
            try {
              socket.close();
            } catch (IOException ex) {
            }
          }
        }
      }
    };
  }

}
