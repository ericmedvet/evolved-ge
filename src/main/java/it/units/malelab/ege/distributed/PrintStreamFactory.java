/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class PrintStreamFactory {
  
  private final String baseDirName;
  private final Map<List<String>, PrintStream> streams;
  
  private final static Logger L = Logger.getLogger(PrintStreamFactory.class.getName());

  public PrintStreamFactory(String baseDirName) {
    this.baseDirName = baseDirName;
    streams = Collections.synchronizedMap(new HashMap<List<String>, PrintStream>());
  }
  
  
  public PrintStream get(List<String> keys, String fileName) {
    PrintStream ps = streams.get(keys);
    if (ps==null) {
      String initials = "";
      for (String key : keys) {
        initials = initials+key.substring(0, 1);
      }
      String currentDate = String.format("%1$td%1$tm%1$tH%1$tM%1$tS", Calendar.getInstance().getTime().getTime());
      fileName = fileName+"."+initials+"."+currentDate+".txt";
      fileName = fileName.replaceAll("[^a-zA-Z0-9._]", "");
      fileName = baseDirName+File.separator+fileName;
      try {
        ps = new PrintStream(fileName);
      } catch (FileNotFoundException ex) {
        L.severe(String.format("Cannot create file \"%s\": %s", fileName, ex));
        ps = System.out;
      }
      DistributedUtils.writeHeader(ps, keys);
      streams.put(keys, ps);
    }
    return ps;
  }
  
}
