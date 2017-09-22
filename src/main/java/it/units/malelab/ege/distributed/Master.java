/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import com.google.common.collect.Multimap;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import it.units.malelab.ege.core.Node;
import java.io.FileNotFoundException;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
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

  private final String keyPhrase;
  private final int port;
  private final String baseResultFileName;

  private final ExecutorService executor;
  private final Random random;
  private final Map<List<String>, PrintStream> streams;

  private final List<Job> toDoJobs;
  private final Map<Job, List<List<Node>>> completedJobs;

  private final static Logger L = Logger.getLogger(Master.class.getName());

  public Master(String keyPhrase, int port, String baseResultFileName) {
    this.keyPhrase = keyPhrase;
    this.port = port;
    this.baseResultFileName = baseResultFileName;
    this.executor = Executors.newCachedThreadPool();
    random = new Random();
    streams = Collections.synchronizedMap(new HashMap<List<String>, PrintStream>());
    toDoJobs = Collections.synchronizedList(new ArrayList<Job>());
    completedJobs = Collections.synchronizedMap(new HashMap<Job, List<List<Node>>>());
  }

  public static void main(String[] args) throws IOException {
    new Master("hi", 9000, "me").start();
  }

  public void start() {
    executor.submit(getServerRunnable());
    DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
    Screen screen;
    try {
      screen = terminalFactory.createScreen();
      screen.startScreen();
      executor.submit(getUIRunnable(screen)); //TODO likely schedule, rather than just invoke
    } catch (IOException ex) {
      L.log(Level.SEVERE, String.format("Cannot start screen: will run in log-only mode: %s", ex), ex);
    }
  }
  
  private Runnable getUIRunnable(final Screen screen) {
    return new Runnable() {
      @Override
      public void run() {
        //do things here
      }
    };
  }

  private Runnable getServerRunnable() {
    return new Runnable() {
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
            L.finer(String.format("Connection from %s:%d.", socket.getInetAddress(), socket.getPort()));
            executor.submit(getClientRunnable(socket));
          } catch (IOException ex) {
            L.log(Level.WARNING, String.format("Cannot accept socket: %s", ex.getMessage()), ex);
          }
        }
      }
    };
  }

  private Runnable getClientRunnable(final Socket socket) {
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
            throw new SecurityException("Client %s:%d did not correctly replied to the challenge!");
          }
          L.finer(String.format("Client %s:%d completed andshake correctly with \"%s\".", socket.getInetAddress(), socket.getPort(), randomData));
          //get updates
          int dataItemsCount = 0;
          Multimap<Job, Map<String, Object>> jobData = (Multimap<Job, Map<String, Object>>) ois.readObject();
          for (Job job : jobData.keySet()) {
            List<String> streamKey = DistributedUtils.jobKeys(job);
            PrintStream ps = streams.get(streamKey);
            if (ps == null) {
              L.fine(String.format("Building new stream for %s.", streamKey));
              ps = build(streamKey);
              streams.put(streamKey, ps);
            }
            for (Map<String, Object> dataItem : jobData.get(job)) {
              dataItemsCount = dataItemsCount + 1;
              DistributedUtils.writeData(ps, job, dataItem);
            }
          }
          L.fine(String.format("Client %s:%d sent %d data items from %d jobs.", socket.getInetAddress(), socket.getPort(), dataItemsCount, jobData.keySet().size()));
          //get results
          Map<Job, List<List<Node>>> newCompletedJobs = (Map<Job, List<List<Node>>>) ois.readObject();
          L.fine(String.format("Client %s:%d sent %d results.", socket.getInetAddress(), socket.getPort(), newCompletedJobs.size()));
          completedJobs.putAll(newCompletedJobs);
          //possibly assign new jobs
          Integer freeRemoteThreads = (Integer) ois.readObject();
          L.fine(String.format("Client %s:%d would accept jobs for %d threads.", socket.getInetAddress(), socket.getPort(), freeRemoteThreads));
          if ((freeRemoteThreads > 0) && !toDoJobs.isEmpty()) {
            //send one job (might be improved w/ better choice)
            synchronized (toDoJobs) {
              List<Job> newJobs = new ArrayList<>();
              int remaining = freeRemoteThreads;
              for (Job job : toDoJobs) {
                if (job.getEstimatedMaxThreads() <= remaining) {
                  newJobs.add(job);
                  remaining = remaining - job.getEstimatedMaxThreads();
                }
              }
              if (newJobs.isEmpty()) {
                newJobs.add(toDoJobs.get(random.nextInt(toDoJobs.size())));
              }
              toDoJobs.removeAll(newJobs);
              L.info(String.format("Sending %d jobs to client %s:%d (%d remaining).", newJobs.size(), socket.getInetAddress(), socket.getPort(), toDoJobs.size()));
              oos.writeObject(newJobs);
            }
          } else {
            oos.writeObject(Collections.EMPTY_LIST);
          }
          //get stats
          Map<String, Number> stats = (Map<String, Number>) ois.readObject();
          L.fine(String.format("Client %s:%d stats: %s", socket.getInetAddress(), socket.getPort(), stats));
        } catch (IOException ex) {
          L.log(Level.WARNING, String.format("Cannot build Object streams: %s", ex.getMessage()), ex);
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
    };
  }

  public Future<List<List<Node>>> submit(final Job job) {
    toDoJobs.add(job);
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
        return completedJobs.containsKey(job);
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
          List<List<Node>> result = completedJobs.get(job);
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

}
