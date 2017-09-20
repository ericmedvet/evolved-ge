/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import it.units.malelab.ege.core.listener.collector.Collector;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class DistributedUtils {

  public static byte[] encrypt(String s, String key) {
    return s.getBytes();
  }

  public static String decrypt(byte[] bytes, String key) {
    return new String(bytes);
  }

  public static String reverse(String s) {
    StringBuilder sb = new StringBuilder();
    sb.append(s);
    sb.reverse();
    return sb.toString();
  }

  public static List<String> jobKeys(Job job) {
    List<String> keys = new ArrayList<>();
    keys.addAll(job.getKeys().keySet());
    keys.add(Master.GENERATION_NAME);
    keys.add(Master.LOCAL_TIME_NAME);
    for (Collector collector : (List<Collector>) job.getCollectors()) {
      keys.addAll(collector.getFormattedNames().keySet());
    }
    return keys;
  }

  public static void writeHeader(PrintStream ps, List<String> keys) {
    for (int i = 0; i < keys.size() - 1; i++) {
      ps.print(keys.get(i) + ";");
    }
    ps.println(keys.get(keys.size() - 1));
  }

  public static void writeData(PrintStream ps, Job job, Map<String, Object> data) {
    List<String> keys = jobKeys(job);
    Map<String, Object> allData = new HashMap<>(data);
    allData.putAll(job.getKeys());
    for (int i = 0; i < keys.size() - 1; i++) {
      ps.print(allData.get(keys.get(i)) + ";");
    }
    ps.println(allData.get(keys.get(keys.size() - 1)));
  }

}
