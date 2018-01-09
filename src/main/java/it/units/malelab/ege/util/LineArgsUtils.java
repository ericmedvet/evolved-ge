/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class LineArgsUtils {
  
  private final static String PIECES_SEP = "-";
  private final static String OPTIONS_SEP = ",";
  private final static String KEYVAL_SEP = "=";
  
  public static String p(String s, int n) {
    String[] pieces = s.split(PIECES_SEP);
    if (n < pieces.length) {
      return pieces[n];
    }
    return null;
  }

  public static int i(String s) {
    return Integer.parseInt(s);
  }

  public static String a(String[] args, String name, String defaultValue) {
    for (String arg : args) {
      String[] pieces = arg.split(KEYVAL_SEP);
      if (pieces[0].equals(name)) {
        return pieces[1];
      }
    }
    return defaultValue;
  }
  
  public static List<String> l(String s) {
    List<String> l = new ArrayList<>();
    String[] pieces = s.split(OPTIONS_SEP);
    for (String piece : pieces) {
      l.add(piece);
    }
    return l;
  }
  
  public static List<Integer> i(List<String> strings) {
    List<Integer> ints = new ArrayList<>();
    for (String string : strings) {
      ints.add(Integer.parseInt(string));
    }
    return ints;
  }

}
