/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.master;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import it.units.malelab.ege.core.listener.collector.Collector;
import static it.units.malelab.ege.distributed.master.Master.GENERATION_NAME;
import it.units.malelab.ege.distributed.worker.StatsRunnable;
import it.units.malelab.ege.util.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class UIRunnable implements Runnable {

  private final Screen screen;
  private final Master master;

  private final static int UI_INTERVAL = 500;
  public final static int LOG_QUEUE_SIZE = 5;
  private final static Logger L = Logger.getLogger(UIRunnable.class.getName());

  public UIRunnable(Screen screen, Master master) {
    this.screen = screen;
    this.master = master;
  }

  @Override
  public void run() {
    try {
      TerminalSize terminalSize = screen.getTerminalSize();
      while (true) {
        //draw
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
        int cx = 1, cy = 1; //clients
        int ojx = 1, ojy = h / 2 + 1; //ongoing jobs
        int lx = 1, ly = h - 1 - LOG_QUEUE_SIZE; //logs
        int jix = w / 2 + 1, jiy = cy; //jobs
        //draw lines
        TextGraphics g = screen.newTextGraphics();
        g.setForegroundColor(TextColor.ANSI.BLUE);
        g.drawLine(cx - 1, cy - 1, w - 1, cy - 1, Symbols.SINGLE_LINE_HORIZONTAL);
        g.drawLine(cx - 1, ojy - 1, w - 1, ojy - 1, Symbols.SINGLE_LINE_HORIZONTAL);
        g.drawLine(cx - 1, ly - 1, w - 1, ly - 1, Symbols.SINGLE_LINE_HORIZONTAL);
        g.drawLine(cx - 1, h - 1, w - 1, h - 1, Symbols.SINGLE_LINE_HORIZONTAL);
        g.drawLine(cx - 1, cy - 1, cx - 1, h - 1, Symbols.SINGLE_LINE_VERTICAL);
        g.drawLine(w - 1, cy - 1, w - 1, h - 1, Symbols.SINGLE_LINE_VERTICAL);
        g.drawLine(jix - 1, cy - 1, jix - 1, h / 2, Symbols.SINGLE_LINE_VERTICAL);
        g.setCharacter(cx - 1, cy - 1, Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
        g.setCharacter(w - 1, cy - 1, Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
        g.setCharacter(cy - 1, h - 1, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
        g.setCharacter(w - 1, h - 1, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
        g.setCharacter(jix - 1, cy - 1, Symbols.SINGLE_LINE_T_DOWN);
        g.setCharacter(jix - 1, ojy - 1, Symbols.SINGLE_LINE_T_UP);
        g.setCharacter(cx - 1, ojy - 1, Symbols.SINGLE_LINE_T_RIGHT);
        g.setCharacter(w - 1, ojy - 1, Symbols.SINGLE_LINE_T_LEFT);
        g.setCharacter(cx - 1, ly - 1, Symbols.SINGLE_LINE_T_RIGHT);
        g.setCharacter(w - 1, ly - 1, Symbols.SINGLE_LINE_T_LEFT);
        //draw strings
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.setBackgroundColor(TextColor.ANSI.BLUE);
        g.putString(cx + 1, cy - 1, "Workers");
        g.putString(jix - 1, cy - 1, "Job summary");
        g.putString(ojx + 1, ojy - 1, "Ongoing jobs");
        g.putString(lx + 1, ly - 1, "Log");
        printClients(g, cx, cy, Math.round(w / 2) - 1, Math.round(h / 2) - 1);
        printJobs(g, jix, jiy, Math.round(w / 2) - 1, Math.round(h / 2) - 1);
        printOngoingJobs(g, ojx, ojy, w, Math.round(h / 2) - 1 - LOG_QUEUE_SIZE - 1);
        printLogs(g, lx, ly, w, LOG_QUEUE_SIZE);
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
    } catch (Throwable t) {
      t.printStackTrace();
    }

    screen.clear();
    try {
      screen.stopScreen();
    } catch (IOException ex) {
      //ignore
    }
    master.shutdown();
    System.exit(0);
  }

  private void printLogs(TextGraphics g, int x0, int y0, int w, int h) {
    //print log
    synchronized (master.getLogs()) {
      for (int y = 0; y < master.getLogs().size(); y++) {
        LogRecord logRecord = (LogRecord) master.getLogs().toArray()[y];
        if (logRecord.getLevel().equals(Level.SEVERE)) {
          g.setForegroundColor(TextColor.ANSI.RED);
        } else if (logRecord.getLevel().equals(Level.WARNING)) {
          g.setForegroundColor(TextColor.ANSI.YELLOW);
        } else if (logRecord.getLevel().equals(Level.INFO)) {
          g.setForegroundColor(TextColor.ANSI.GREEN);
        } else {
          g.setForegroundColor(TextColor.ANSI.WHITE);
        }
        putString(g, 0, y, x0, y0, w, h, String.format("%1$td/%1$tm %1$tH:%1$tM:%1$tS",
                new Date(logRecord.getMillis())
        ));
        g.setForegroundColor(TextColor.ANSI.WHITE);
        putString(g, 15, y, x0, y0, w, h, String.format("%s", logRecord.getMessage()));
      }
    }
  }

  private void printOngoingJobs(TextGraphics g, int x0, int y0, int w, int h) {
    //print ongoing job info
    int x = 0;
    int y = 0;
    //build columns
    Map<String, String> jobKeyFormats = new LinkedHashMap<>();
    Map<String, String> collectorKeyFormats = new LinkedHashMap<>();
    collectorKeyFormats.put(GENERATION_NAME, "%3d");
    int nOngoingJobs = 0;
    List<JobInfo> jobInfos = new ArrayList<>();
    synchronized (master.getJobs()) {
      jobInfos.addAll(master.getJobs().values());
    };
    for (JobInfo jobInfo : jobInfos) {
      if (!jobInfo.getStatus().equals(JobInfo.Status.ONGOING)) {
        continue;
      }
      nOngoingJobs = nOngoingJobs + 1;
      //update job keys format    
      for (Map.Entry<String, Object> keyEntry : ((Map<String, Object>) jobInfo.getJob().getKeys()).entrySet()) {
        String currentFormat = String.format("%%%1$d.%1$ds", keyEntry.getValue().toString().length());
        if (!jobKeyFormats.containsKey(keyEntry.getKey())) {
          jobKeyFormats.put(keyEntry.getKey(), currentFormat);
        } else {
          String existingFormat = jobKeyFormats.get(keyEntry.getKey());
          if (Utils.formatSize(currentFormat) > Utils.formatSize(existingFormat)) {
            jobKeyFormats.put(keyEntry.getKey(), currentFormat);
          }
        }
      }
      //update collector format
      for (Collector collector : (List<Collector>) jobInfo.getJob().getCollectors()) {
        for (Map.Entry<String, String> formattedName : ((Map<String, String>) collector.getFormattedNames()).entrySet()) {
          if (!collectorKeyFormats.containsKey(formattedName.getKey())) {
            collectorKeyFormats.put(formattedName.getKey(), formattedName.getValue());
          } else {
            if (Utils.formatSize(formattedName.getValue()) > Utils.formatSize(collectorKeyFormats.get(formattedName.getKey()))) {
              collectorKeyFormats.put(formattedName.getKey(), formattedName.getValue());
            }
          }
        }
      }
    }
    if (nOngoingJobs == 0) {
      return;
    }
    //legend
    g.setForegroundColor(TextColor.ANSI.BLUE);
    putString(g, x, y, x0, y0, w, h, String.format("%-16.16s", "Worker"));
    x = x + 17;
    for (Map.Entry<String, String> formattedKeyEntry : jobKeyFormats.entrySet()) {
      String columnName = String.format(formattedKeyEntry.getValue(), formattedKeyEntry.getKey());
      putString(g, x, y, x0, y0, w, h, columnName);
      x = x + columnName.length() + 1;
    }
    for (Map.Entry<String, String> formattedKeyEntry : collectorKeyFormats.entrySet()) {
      String columnName = Utils.formatName(formattedKeyEntry.getKey(), formattedKeyEntry.getValue(), true);
      putString(g, x, y, x0, y0, w, h, columnName);
      x = x + columnName.length() + 2;
    }
    y = y + 1;
    //  data
    for (JobInfo jobInfo : jobInfos) {
      if (!jobInfo.getStatus().equals(JobInfo.Status.ONGOING)) {
        continue;
      }
      x = 0;
      g.setForegroundColor(TextColor.ANSI.WHITE);
      putString(g, x, y, x0, y0, w, h, String.format("%-16.16s", jobInfo.getClientName()));
      x = x + 17;
      g.setForegroundColor(TextColor.ANSI.CYAN);
      for (Map.Entry<String, String> formattedKeyEntry : jobKeyFormats.entrySet()) {
        String s = String.format(formattedKeyEntry.getValue(), jobInfo.getJob().getKeys().get(formattedKeyEntry.getKey()).toString());
        putString(g, x, y, x0, y0, w, h, s);
        x = x + s.length() + 1;
      }
      if (!jobInfo.getData().isEmpty()) {
        g.setForegroundColor(TextColor.ANSI.WHITE);
        Map<String, Object> currentData = jobInfo.getData().get(jobInfo.getData().size() - 1);
        Map<String, Object> previousData = Collections.EMPTY_MAP;
        if (jobInfo.getData().size() > 1) {
          previousData = jobInfo.getData().get(jobInfo.getData().size() - 2);
        }
        for (Map.Entry<String, String> formattedKeyEntry : collectorKeyFormats.entrySet()) {
          String name = formattedKeyEntry.getKey();
          String format = formattedKeyEntry.getValue();
          String formatted;
          if (currentData.containsKey(name)) {
            Object lastValue = previousData.get(name);
            formatted = String.format(format, currentData.get(name));
            g.setForegroundColor(TextColor.ANSI.WHITE);
            putString(g, x, y, x0, y0, w, h, formatted);
            if ((lastValue != null) && (currentData.get(name) instanceof Number)) {
              double currentNumber = ((Number) currentData.get(name)).doubleValue();
              double lastNumber = ((Number) lastValue).doubleValue();
              if (currentNumber > lastNumber) {
                g.setForegroundColor(TextColor.ANSI.RED);
                putString(g, x + formatted.length(), y, x0, y0, w, h, "" + Symbols.ARROW_UP);
              } else if (currentNumber < lastNumber) {
                g.setForegroundColor(TextColor.ANSI.GREEN);
                putString(g, x + formatted.length(), y, x0, y0, w, h, "" + Symbols.ARROW_DOWN);
              } else {
                g.setForegroundColor(TextColor.ANSI.YELLOW);
                putString(g, x + formatted.length(), y, x0, y0, w, h, "=");
              }
            }
          } else {
            formatted = String.format(format, (Object[]) null);
          }
          x = x + formatted.length() + 2;
        }
      }
      y = y + 1;
    }
  }

  private void printJobs(TextGraphics g, int x0, int y0, int w, int h) {
    //count jobs
    int nToDoJobs = 0;
    int nOngoingJobs = 0;
    int nDoneJobs = 0;
    for (JobInfo jobInfo : master.getJobs().values()) {
      if (jobInfo.getStatus().equals(JobInfo.Status.TO_DO)) {
        nToDoJobs = nToDoJobs + 1;
      } else if (jobInfo.getStatus().equals(JobInfo.Status.ONGOING)) {
        nOngoingJobs = nOngoingJobs + 1;
      } else if (jobInfo.getStatus().equals(JobInfo.Status.DONE)) {
        nDoneJobs = nDoneJobs + 1;
      }
    }
    //print job info
    g.setForegroundColor(TextColor.ANSI.WHITE);
    putString(g, 0, 0, x0, y0, w, h, String.format("All/todo/running/done: %3d/%3d/%3d/%3d",
            master.getJobs().size(), nToDoJobs, nOngoingJobs, nDoneJobs
    ));
    Map<String, Map<Object, Multiset<JobInfo.Status>>> allKeyCounts = new TreeMap<>();
    synchronized (master.getJobs()) {
      for (JobInfo jobInfo : master.getJobs().values()) {
        for (Map.Entry<String, Object> jobKeyEntry : ((Map<String, Object>) jobInfo.getJob().getKeys()).entrySet()) {
          inc(jobKeyEntry.getKey(), jobKeyEntry.getValue(), jobInfo.getStatus(), allKeyCounts);
        }
      }
    }
    int y = 2;
    for (String keyName : allKeyCounts.keySet()) {
      g.setForegroundColor(TextColor.ANSI.WHITE);
      putString(g, 0, y, x0, y0, w, h, keyName + ":");
      y = y + 1;
      int x = 2;
      for (Object keyValue : allKeyCounts.get(keyName).keySet()) {
        Multiset<JobInfo.Status> statuses = allKeyCounts.get(keyName).get(keyValue);
        double completionRate = (double) statuses.count(JobInfo.Status.DONE) / (double) (statuses.size());
        if (x >= w - 1) {
          x = 2;
          y = y + 1;
        }
        if (completionRate == 1) {
          g.setForegroundColor(TextColor.ANSI.GREEN);
        } else if (statuses.count(JobInfo.Status.ONGOING) > 0) {
          g.setForegroundColor(TextColor.ANSI.YELLOW);
        } else {
          g.setForegroundColor(TextColor.ANSI.RED);
        }
        if (completionRate == 0) {
          putString(g, x, y, x0, y0, w, h, "-");
        } else if (completionRate < .25) {
          putString(g, x, y, x0, y0, w, h, "" + Symbols.BLOCK_SPARSE);
        } else if (completionRate < .50) {
          putString(g, x, y, x0, y0, w, h, "" + Symbols.BLOCK_MIDDLE);
        } else if (completionRate < .75) {
          putString(g, x, y, x0, y0, w, h, "" + Symbols.BLOCK_DENSE);
        } else {
          putString(g, x, y, x0, y0, w, h, "" + Symbols.BLOCK_SOLID);
        }
        x = x + 1;
      }
      y = y + 1;
    }
  }

  private void printClients(TextGraphics g, int x0, int y0, int w, int h) {
    //print client info
    g.setBackgroundColor(TextColor.ANSI.BLACK);
    g.setForegroundColor(TextColor.ANSI.BLUE);
    putString(g, 0, 0, x0, y0, w, h, String.format("%-16.16s %3.3s %2.2s %5.5s %4.4s %s",
            "Worker", "T", "Js", "Threads", "Load", "Memory"
    ));
    int y = 1;
    for (Map.Entry<String, ClientInfo> clientEntry : master.getClients().entrySet()) {
      String clientName = clientEntry.getKey();
      ClientInfo clientInfo = clientEntry.getValue();
      Long elapsed = null;
      if (clientInfo.getLastContactDate() != null) {
        elapsed = (System.currentTimeMillis() - clientInfo.getLastContactDate().getTime()) / 1000;
      }
      g.setForegroundColor(TextColor.ANSI.WHITE);
      putString(g, 0, y, x0, y0, w, h, String.format("%-16.16s", clientInfo.getLastMessage().getName()));
      if (elapsed <= clientInfo.getLastMessage().getInterval()) {
        g.setForegroundColor(TextColor.ANSI.GREEN);
        putString(g, 17, y, x0, y0, w, h, String.format("%2ds", elapsed));
      } else if (elapsed < clientInfo.getLastMessage().getInterval() * 2) {
        g.setForegroundColor(TextColor.ANSI.YELLOW);
        putString(g, 17, y, x0, y0, w, h, String.format("%2ds", elapsed));
      } else {
        g.setForegroundColor(TextColor.ANSI.RED);
        if (elapsed < 60) {
          putString(g, 17, y, x0, y0, w, h, String.format("%2ds", elapsed));
        } else {
          putString(g, 17, y, x0, y0, w, h, ">1m");
        }
      }
      g.setForegroundColor(TextColor.ANSI.WHITE);
      putString(g, 17 + 3 + 1, y, x0, y0, w, h, String.format("%2d %2d/%2d %4.2f %.1f/%.1fGB",
              master.getClientJobIds(clientName).size(),
              clientInfo.getLastMessage().getFreeThreads(),
              clientInfo.getLastMessage().getMaxThreads(),
              clientInfo.getLastMessage().getStats().get(StatsRunnable.STAT_CPU_SYSTEM_NAME),
              clientInfo.getLastMessage().getStats().containsKey(StatsRunnable.STAT_FREE_MEM_NAME) ? clientInfo.getLastMessage().getStats().get(StatsRunnable.STAT_FREE_MEM_NAME).doubleValue() / 1024d / 1024d / 1024d : null,
              clientInfo.getLastMessage().getStats().containsKey(StatsRunnable.STAT_MAX_MEM_NAME) ? clientInfo.getLastMessage().getStats().get(StatsRunnable.STAT_MAX_MEM_NAME).doubleValue() / 1024d / 1024d / 1024d : null
      ));
      y = y + 1;
    }
  }

  private void inc(String keyName, Object keyValue, JobInfo.Status status, Map<String, Map<Object, Multiset<JobInfo.Status>>> map) {
    Map<Object, Multiset<JobInfo.Status>> valueCounts = map.get(keyName);
    if (valueCounts == null) {
      valueCounts = new TreeMap<>();
      map.put(keyName, valueCounts);
    }
    Multiset<JobInfo.Status> statuses = valueCounts.get(keyValue);
    if (statuses == null) {
      statuses = HashMultiset.create();
      valueCounts.put(keyValue, statuses);
    }
    statuses.add(status);
  }

  private void putString(TextGraphics g, int x, int y, int x0, int y0, int w, int h, String s) {
    if ((x >= w - 1) || (y >= h)) {
      return;
    }
    if (x + s.length() >= w - 1) {
      s = s.substring(0, w - x - 2);
    }
    g.putString(x0 + x, y0 + y, s);
  }

}
