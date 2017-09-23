/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Multimap;
import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
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
  private final static int UI_INTERVAL = 250;
  private final static int LOG_QUEUE_SIZE = 5;

  private final static String STAT_LAST_CONTACT_DATE_NAME = "last.contact.date";

  public final static String LOCAL_TIME_NAME = "local.time";
  public final static String GENERATION_NAME = "generation";
  public final static String RANDOM_SEED_NAME = "random.seed";

  private final String keyPhrase;
  private final int port;
  private final String baseResultFileName;

  private final ExecutorService mainExecutor;
  private final Random random;
  private final Map<List<String>, PrintStream> streams;

  private final List<Job> toDoJobs;
  private final Map<Job, Map<String, Queue>> ongoingJobs;
  private final Map<Job, List<List<Node>>> completedJobs;
  private final Map<String, Map<String, Number>> clients;
  private final Queue<LogRecord> logQueue;

  static {
    try {
      LogManager.getLogManager().readConfiguration(Master.class.getClassLoader().getResourceAsStream("logging.properties"));
    } catch (IOException ex) {
      //ignore
    } catch (SecurityException ex) {
      //ignore
    }
  }

  private final static Logger L = Logger.getLogger(Master.class.getName());

  public Master(String keyPhrase, int port, String baseResultFileName) {
    this.keyPhrase = keyPhrase;
    this.port = port;
    this.baseResultFileName = baseResultFileName;
    this.mainExecutor = Executors.newCachedThreadPool();
    random = new Random();
    streams = Collections.synchronizedMap(new HashMap<List<String>, PrintStream>());
    toDoJobs = Collections.synchronizedList(new ArrayList<Job>());
    ongoingJobs = Collections.synchronizedMap(new HashMap<Job, Map<String, Queue>>());
    completedJobs = Collections.synchronizedMap(new HashMap<Job, List<List<Node>>>());
    clients = Collections.synchronizedMap(new TreeMap<String, Map<String, Number>>());
    logQueue = EvictingQueue.create(LOG_QUEUE_SIZE);
  }

  public static void main(String[] args) throws IOException {
    new Master("hi", 9000, "me").start();
  }

  public void start() {
    mainExecutor.submit(getServerRunnable());
    DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
    Screen screen;
    try {
      screen = terminalFactory.createScreen();
      screen.startScreen();
      mainExecutor.submit(getUIRunnable(screen));
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

  private Runnable getUIRunnable(final Screen screen) {
    return new Runnable() {
      @Override
      public void run() {
        TerminalSize terminalSize = screen.getTerminalSize();
        while (true) {
          KeyStroke keyStroke;
          try {
            keyStroke = screen.pollInput();
          } catch (IOException ex) {
            L.log(Level.WARNING, String.format("Cannot read user input: %s", ex), ex);
            continue;
          }
          if (keyStroke != null && (keyStroke.getKeyType() == KeyType.Escape || keyStroke.getKeyType() == KeyType.EOF)) {
            break;
          }
          TerminalSize newSize = screen.doResizeIfNecessary();
          if (newSize != null) {
            terminalSize = newSize;
          }
          int w = terminalSize.getColumns();
          int h = terminalSize.getRows();
          screen.clear();
          //draw lines
          TextGraphics g = screen.newTextGraphics();
          g.setForegroundColor(TextColor.ANSI.BLUE);
          g.drawLine(0, 0, w - 1, 0, Symbols.SINGLE_LINE_HORIZONTAL);
          g.drawLine(0, h / 2, w - 1, h / 2, Symbols.SINGLE_LINE_HORIZONTAL);
          g.drawLine(0, h - 1 - LOG_QUEUE_SIZE - 1, w - 1, h - 1 - LOG_QUEUE_SIZE - 1, Symbols.SINGLE_LINE_HORIZONTAL);
          g.drawLine(0, h - 1, w - 1, h - 1, Symbols.SINGLE_LINE_HORIZONTAL);
          g.drawLine(0, 0, 0, h - 1, Symbols.SINGLE_LINE_VERTICAL);
          g.drawLine(w - 1, 0, w - 1, h - 1, Symbols.SINGLE_LINE_VERTICAL);
          g.drawLine(w / 2, 0, w / 2, h / 2, Symbols.SINGLE_LINE_VERTICAL);
          g.setCharacter(0, 0, Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
          g.setCharacter(w - 1, 0, Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
          g.setCharacter(0, h - 1, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
          g.setCharacter(w - 1, h - 1, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
          g.setCharacter(w / 2, 0, Symbols.SINGLE_LINE_T_DOWN);
          g.setCharacter(w / 2, h / 2, Symbols.SINGLE_LINE_T_UP);
          g.setCharacter(0, h / 2, Symbols.SINGLE_LINE_T_RIGHT);
          g.setCharacter(w - 1, h / 2, Symbols.SINGLE_LINE_T_LEFT);
          g.setCharacter(0, h - 1 - LOG_QUEUE_SIZE - 1, Symbols.SINGLE_LINE_T_RIGHT);
          g.setCharacter(w - 1, h - 1 - LOG_QUEUE_SIZE - 1, Symbols.SINGLE_LINE_T_LEFT);
          //draw strings
          g.setForegroundColor(TextColor.ANSI.WHITE);
          g.setBackgroundColor(TextColor.ANSI.BLUE);
          g.putString(2, 0, "Workers");
          g.putString(w / 2 + 2, 0, "Job summary");
          g.putString(2, h / 2, String.format("Ongoing jobs (%d)", ongoingJobs.size()));
          g.putString(2, h - 1 - LOG_QUEUE_SIZE - 1, String.format("Log (%d)", logQueue.size()));
          //print client info
          g.setBackgroundColor(TextColor.ANSI.BLACK);
          int r = 0;
          for (Map.Entry<String, Map<String, Number>> entry : clients.entrySet()) {
            long elapsed = (System.currentTimeMillis() - (Long) entry.getValue().getOrDefault(STAT_LAST_CONTACT_DATE_NAME, Double.NaN)) / 1000;
            g.setForegroundColor(TextColor.ANSI.WHITE);
            g.putString(1, 1 + r, String.format("%16.16s", entry.getKey()));
            g.setForegroundColor(TextColor.ANSI.WHITE);
            if (elapsed > 300) {
              g.putString(17 + 1, 1 + r, ">5m");
            } else {
              g.putString(17 + 1, 1 + r, String.format("%3ds", elapsed));
            }
            if (entry.getValue().containsKey(Worker.STAT_CPU_SYSTEM_NAME)) {
              g.putString(17 + 1 + 7 + 1, 1 + r,
                      String.format("%4.2f", entry.getValue().get(Worker.STAT_CPU_SYSTEM_NAME))
              );
            }
            if (entry.getValue().containsKey(Worker.STAT_MAX_MEM_NAME)) {
              g.putString(17 + 1 + 7 + 1 + 4 + 1, 1 + r, String.format("%.1f/%.1fGB",
                      ((Double) entry.getValue().get(Worker.STAT_MAX_MEM_NAME) - (Double) entry.getValue().get(Worker.STAT_FREE_MEM_NAME)) / 1024d / 1024d / 1024d,
                      (Double) entry.getValue().get(Worker.STAT_MAX_MEM_NAME) / 1024d / 1024d / 1024d)
              );
            }
            r = r + 1;
          }
          //print log
          synchronized (logQueue) {
            for (int i = 0; i < logQueue.size(); i++) {
              LogRecord logRecord = (LogRecord) logQueue.toArray()[i];
              if (logRecord.getLevel().equals(Level.SEVERE)) {
                g.setForegroundColor(TextColor.ANSI.RED);
              } else if (logRecord.getLevel().equals(Level.WARNING)) {
                g.setForegroundColor(TextColor.ANSI.YELLOW);
              } else if (logRecord.getLevel().equals(Level.INFO)) {
                g.setForegroundColor(TextColor.ANSI.GREEN);
              } else {
                g.setForegroundColor(TextColor.ANSI.WHITE);
              }
              g.setCharacter(1, h - 1 - LOG_QUEUE_SIZE + i, Symbols.BULLET);
              g.setForegroundColor(TextColor.ANSI.WHITE);
              g.putString(3, h - 1 - LOG_QUEUE_SIZE + i, l(String.format("%1$td/%1$tm %1$tH:%1$tM:%1$tS %2$s",
                      new Date(logRecord.getMillis()),
                      logRecord.getMessage()
              ), w-4));
            }
          }
          //refresh
          try {
            screen.refresh();
          } catch (IOException ex) {
            L.log(Level.WARNING, String.format("Cannot update screen: %s", ex), ex);
            continue;
          }
          try {
            Thread.sleep(UI_INTERVAL);
          } catch (InterruptedException ex) {
            //ignore
          }
        }
        screen.clear();
        try {
          screen.stopScreen();
        } catch (IOException ex) {
          //ignore
        }
        mainExecutor.shutdownNow();
        System.exit(0);
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
            mainExecutor.submit(getClientRunnable(socket));
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
      public void run() {        ObjectOutputStream oos = null;
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
            L.warning(String.format("Client %s:%d did not correctly replied to the challenge!", socket.getInetAddress(), socket.getPort(), randomData));
            throw new SecurityException("Client %s:%d did not correctly replied to the challenge!");
          }
          L.finer(String.format("Client %s:%d completed andshake correctly with \"%s\".", socket.getInetAddress(), socket.getPort(), randomData));
          //get name
          String clientName = (String) ois.readObject();
          //get updates
          int dataItemsCount = 0;
          Multimap<Job, Map<String, Object>> jobData = (Multimap<Job, Map<String, Object>>) ois.readObject();
          for (Job job : jobData.keySet()) {
            List<String> streamKey = DistributedUtils.jobKeys(job);
            PrintStream ps = streams.get(streamKey);
            Map<String, Queue> jobHistoricData = ongoingJobs.get(job);
            if (ps == null) {
              L.fine(String.format("Building new stream for %s.", streamKey));
              ps = build(streamKey);
              streams.put(streamKey, ps);
            }
            for (Map<String, Object> dataItem : jobData.get(job)) {
              dataItemsCount = dataItemsCount + 1;
              DistributedUtils.writeData(ps, job, dataItem);
              for (Map.Entry<String, Object> entry : dataItem.entrySet()) {
                Queue q = jobHistoricData.getOrDefault(entry.getKey(), EvictingQueue.create(2));
                q.add(entry.getValue());
              }
            }
          }
          L.fine(String.format("Client %s:%d sent %d data items from %d jobs.", socket.getInetAddress(), socket.getPort(), dataItemsCount, jobData.keySet().size()));
          //get results
          Map<Job, List<List<Node>>> newCompletedJobs = (Map<Job, List<List<Node>>>) ois.readObject();
          L.fine(String.format("Client %s:%d sent %d results.", socket.getInetAddress(), socket.getPort(), newCompletedJobs.size()));
          synchronized (completedJobs) {
            completedJobs.putAll(newCompletedJobs);
            ongoingJobs.keySet().removeAll(newCompletedJobs.keySet());
          }
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
              for (Job newJob : newJobs) {
                ongoingJobs.put(newJob, new HashMap<String, Queue>());
              }
              L.info(String.format("Sending %d jobs to client %s:%d (%d remaining).", newJobs.size(), socket.getInetAddress(), socket.getPort(), toDoJobs.size()));
              oos.writeObject(newJobs);
            }
          } else {
            oos.writeObject(Collections.EMPTY_LIST);
          }
          //get stats
          Map<String, Number> stats = (Map<String, Number>) ois.readObject();
          stats.put(STAT_LAST_CONTACT_DATE_NAME, System.currentTimeMillis());
          clients.put(clientName, stats);
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
  
  private String l(String s, int w) {
    return s.substring(0, Math.min(s.length(), w));
  }

}
