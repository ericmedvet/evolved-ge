/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.master;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import it.units.malelab.ege.distributed.Job;
import static it.units.malelab.ege.distributed.master.Master.GENERATION_NAME;
import it.units.malelab.ege.distributed.worker.StatsRunnable;
import it.units.malelab.ege.util.Utils;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
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
        int cx = 1, cy = 1;
        int ojx = 1, ojy = h / 2 + 1;
        int lx = 1, ly = h - 1 - LOG_QUEUE_SIZE;
        int jix = w / 2 + 1, jiy = cy;
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
        g.putString(ojx + 1, ojy - 1, String.format("Ongoing jobs (%d)", master.getCurrentJobsData().keySet().size()));
        g.putString(lx + 1, ly - 1, "Log");
        //print client info
        g.setBackgroundColor(TextColor.ANSI.BLACK);
        int y = cy;
        //TODO add table headers
        for (ClientInfo clientInfo : master.getClientInfos().values()) {
          Long elapsed = null;
          if (clientInfo.getLastContactDate() != null) {
            elapsed = (System.currentTimeMillis() - clientInfo.getLastContactDate().getTime()) / 1000;
          }
          g.setForegroundColor(TextColor.ANSI.WHITE);
          g.putString(cx, y, String.format("%16.16s", clientInfo.getLastMessage().getName()));
          if (elapsed <= clientInfo.getLastMessage().getInterval()) {
            g.setForegroundColor(TextColor.ANSI.GREEN);
            g.putString(cx + 17, y, String.format("%2ds", elapsed));
          } else if (elapsed < clientInfo.getLastMessage().getInterval() * 2) {
            g.setForegroundColor(TextColor.ANSI.YELLOW);
            g.putString(cx + 17, y, String.format("%2ds", elapsed));
          } else {
            g.setForegroundColor(TextColor.ANSI.RED);
            if (elapsed < 60) {
              g.putString(cx + 17, y, String.format("%2ds", elapsed));
            } else {
              g.putString(cx + 17, y, ">1m");
            }
          }
          g.setForegroundColor(TextColor.ANSI.WHITE);
          g.putString(cx + 17 + 3 + 1, y, String.format("%2d %2d/%2d %4.2f %.1f/%.1fGB",
                  clientInfo.getLastMessage().getFreeThreads(),
                  clientInfo.getLastMessage().getMaxThreads(),
                  clientInfo.getLastMessage().getStats().get(StatsRunnable.STAT_CPU_SYSTEM_NAME),
                  clientInfo.getLastMessage().getStats().get(StatsRunnable.STAT_FREE_MEM_NAME),
                  clientInfo.getLastMessage().getStats().get(StatsRunnable.STAT_MAX_MEM_NAME)
          ));
          y = y + 1;
        }
        //print job info
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.putString(jix, jiy, l(String.format("All/todo/running/done: %3d/%3d/%3d/%3d",
                master.getToDoJobs().size() + master.getCurrentJobsData().size() + master.getCompletedJobsResults().size(),
                master.getToDoJobs().size(), master.getCurrentJobsData().size(), master.getCompletedJobsResults().size()
        ), w - 1 - jiy));
        Map<String, Map<Object, int[]>> allKeyCounts = new TreeMap<>();
        synchronized (master.getToDoJobs()) {
          for (Job job : master.getToDoJobs()) {
            for (Map.Entry<String, Object> jobKeyEntry : ((Map<String, Object>) job.getKeys()).entrySet()) {
              inc(jobKeyEntry.getKey(), jobKeyEntry.getValue(), 0, allKeyCounts);
            }
          }
        }
        synchronized (master.getCurrentJobsData()) {
          for (Job job : master.getCurrentJobsData().keySet()) {
            for (Map.Entry<String, Object> jobKeyEntry : ((Map<String, Object>) job.getKeys()).entrySet()) {
              inc(jobKeyEntry.getKey(), jobKeyEntry.getValue(), 1, allKeyCounts);
            }
          }
        }
        synchronized (master.getCompletedJobsResults()) {
          for (Job job : master.getCompletedJobsResults().keySet()) {
            for (Map.Entry<String, Object> jobKeyEntry : ((Map<String, Object>) job.getKeys()).entrySet()) {
              inc(jobKeyEntry.getKey(), jobKeyEntry.getValue(), 2, allKeyCounts);
            }
          }
        }
        y = jiy + 2;
        for (String keyName : allKeyCounts.keySet()) {
          g.setForegroundColor(TextColor.ANSI.WHITE);
          g.putString(jix, y, keyName + ":");
          y = y + 1;
          int x = jix + 2;
          for (Object keyValue : allKeyCounts.get(keyName).keySet()) {
            int[] counts = allKeyCounts.get(keyName).get(keyValue);
            double completionRate = (double) counts[2] / (double) (counts[0] + counts[1] + counts[2]);
            if (x >= w - 1) {
              x = jix + 2;
              y = y + 1;
            }
            if (completionRate == 1) {
              g.setForegroundColor(TextColor.ANSI.GREEN);
            } else if (counts[1] > 0) {
              g.setForegroundColor(TextColor.ANSI.YELLOW);
            } else {
              g.setForegroundColor(TextColor.ANSI.RED);
            }
            if (completionRate == 0) {
              g.setCharacter(x, y, '-');
            } else if (completionRate < .25) {
              g.setCharacter(x, y, Symbols.BLOCK_SPARSE);
            } else if (completionRate < .50) {
              g.setCharacter(x, y, Symbols.BLOCK_MIDDLE);
            } else if (completionRate < .75) {
              g.setCharacter(x, y, Symbols.BLOCK_DENSE);
            } else {
              g.setCharacter(x, y, Symbols.BLOCK_SOLID);
            }
            x = x + 1;
          }
          y = y + 1;
        }
        //print ongoing job info
        y = ojy;
        int x = ojx;
        //  legend
        g.setForegroundColor(TextColor.ANSI.BLUE);
        for (Map.Entry<String, String> formattedKeyEntry : master.getJobKeyFormats().entrySet()) {
          String columnName = String.format(formattedKeyEntry.getValue(), formattedKeyEntry.getKey());
          g.putString(x, y, columnName);
          x = x + columnName.length() + 1;
        }
        for (Map.Entry<String, String> formattedKeyEntry : master.getCollectorKeyFormats().entrySet()) {
          String columnName = Utils.formatName(formattedKeyEntry.getKey(), formattedKeyEntry.getValue(), true);
          g.putString(x, y, columnName);
          x = x + columnName.length() + 2;
        }
        y = y + 1;
        //  data
        for (Job job : master.getCurrentJobsData().keySet()) {
          x = ojx;
          g.setForegroundColor(TextColor.ANSI.CYAN);
          for (Map.Entry<String, String> formattedKeyEntry : master.getJobKeyFormats().entrySet()) {
            String s = String.format(formattedKeyEntry.getValue(), job.getKeys().get(formattedKeyEntry.getKey()).toString());
            g.putString(x, y, s);
            x = x + s.length() + 1;
          }
          g.setForegroundColor(TextColor.ANSI.WHITE);
          Map<String, Object>[] allData = master.getCurrentJobsData().get(job).toArray(new Map[0]);
          Map<String, Object> currentData = allData[allData.length - 1];
          Map<String, Object> previousData = Collections.EMPTY_MAP;
          if (allData.length>1) {
            previousData = allData[allData.length-2];
          }
          if (!currentData.containsKey(GENERATION_NAME)) {
            continue;
          }
          for (Map.Entry<String, String> formattedKeyEntry : master.getCollectorKeyFormats().entrySet()) {
            String name = formattedKeyEntry.getKey();
            String format = formattedKeyEntry.getValue();
            String formatted;
            if (currentData.containsKey(name)) {
              Object lastValue = previousData.get(name);
              formatted = String.format(format, currentData.get(name));
              if (x + formatted.length() + 1 < w - 1) {
                g.setForegroundColor(TextColor.ANSI.WHITE);
                g.putString(x, y, formatted);
              }
              if ((lastValue != null) && (currentData.get(name) instanceof Number)) {
                double currentNumber = ((Number) currentData.get(name)).doubleValue();
                double lastNumber = ((Number) lastValue).doubleValue();
                if (currentNumber > lastNumber) {
                  g.setForegroundColor(TextColor.ANSI.RED);
                  g.setCharacter(x + formatted.length(), y, Symbols.ARROW_UP);
                } else if (currentNumber < lastNumber) {
                  g.setForegroundColor(TextColor.ANSI.GREEN);
                  g.setCharacter(x + formatted.length(), y, Symbols.ARROW_DOWN);
                } else {
                  g.setForegroundColor(TextColor.ANSI.YELLOW);
                  g.setCharacter(x + formatted.length(), y, '=');
                }
              }
            } else {
              formatted = String.format(format, (Object[]) null);
            }
            x = x + formatted.length() + 2;
          }
          y = y + 1;
        }
        //print log
        synchronized (master.getLogQueue()) {
          for (int i = 0; i < master.getLogQueue().size(); i++) {
            LogRecord logRecord = (LogRecord) master.getLogQueue().toArray()[i];
            if (logRecord.getLevel().equals(Level.SEVERE)) {
              g.setForegroundColor(TextColor.ANSI.RED);
            } else if (logRecord.getLevel().equals(Level.WARNING)) {
              g.setForegroundColor(TextColor.ANSI.YELLOW);
            } else if (logRecord.getLevel().equals(Level.INFO)) {
              g.setForegroundColor(TextColor.ANSI.GREEN);
            } else {
              g.setForegroundColor(TextColor.ANSI.WHITE);
            }
            g.setCharacter(lx, ly + i, Symbols.BULLET);
            g.setForegroundColor(TextColor.ANSI.WHITE);
            g.putString(lx + 2, ly + i, l(String.format("%1$td/%1$tm %1$tH:%1$tM:%1$tS %2$s",
                    new Date(logRecord.getMillis()),
                    logRecord.getMessage()
            ), w - 4));
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

  private void inc(String keyName, Object keyValue, int index, Map<String, Map<Object, int[]>> map) {
    Map<Object, int[]> valueCounts = map.get(keyName);
    if (valueCounts == null) {
      valueCounts = new TreeMap<>();
      map.put(keyName, valueCounts);
    }
    int[] counts = valueCounts.get(keyValue);
    if (counts == null) {
      counts = new int[3];
      valueCounts.put(keyValue, counts);
    }
    counts[index] = counts[index] + 1;
  }

  private String l(String s, int w) {
    return s.substring(0, Math.min(s.length(), w));
  }

}
