/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import it.units.malelab.ege.core.listener.collector.Collector;
import java.io.IOException;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author eric
 */
public class DistributedUtils {

  private final static Logger L = Logger.getLogger(DistributedUtils.class.getName());

  public static byte[] encrypt(String s, String keyString) {
    return cipher(s.getBytes(), keyString, Cipher.ENCRYPT_MODE);
  }

  public static String decrypt(byte[] bytes, String keyString) {
    return new String(cipher(bytes, keyString, Cipher.DECRYPT_MODE));
  }
  
  public static byte[] cipher(byte[] bytes, String keyString, int mode) {
    try {
      Cipher c = Cipher.getInstance("AES");
      byte[] keyBytes = new byte[16];
      byte[] shortKeyBytes = Base64.getEncoder().encode(keyString.getBytes());
      System.arraycopy(shortKeyBytes, 0, keyBytes, 0, shortKeyBytes.length);
      Key key = new SecretKeySpec(keyBytes, "AES");
      c.init(mode, key);
      return c.doFinal(bytes);
    } catch (NoSuchAlgorithmException ex) {
      L.log(Level.SEVERE, String.format("Cannot encrypt: %s", ex), ex);
    } catch (NoSuchPaddingException ex) {
      L.log(Level.SEVERE, String.format("Cannot encrypt: %s", ex), ex);
    //} catch (IOException ex) {
      L.log(Level.SEVERE, String.format("Cannot encrypt: %s", ex), ex);
    } catch (InvalidKeyException ex) {
      L.log(Level.SEVERE, String.format("Cannot encrypt: %s", ex), ex);
    } catch (IllegalBlockSizeException ex) {
      L.log(Level.SEVERE, String.format("Cannot encrypt: %s", ex), ex);
    } catch (BadPaddingException ex) {
      L.log(Level.SEVERE, String.format("Cannot encrypt: %s", ex), ex);
    }
    return bytes;
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
