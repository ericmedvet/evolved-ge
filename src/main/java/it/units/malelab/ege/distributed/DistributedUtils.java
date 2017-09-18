/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

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
  
}
