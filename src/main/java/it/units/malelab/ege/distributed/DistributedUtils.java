/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import it.units.malelab.ege.core.listener.collector.Collector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    for (Collector collector : (List<Collector>)job.getCollectors()) {
      keys.addAll(collector.getFormattedNames().keySet());
    }
    return keys;
  }
  
}
