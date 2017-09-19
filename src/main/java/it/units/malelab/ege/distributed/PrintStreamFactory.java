/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import java.io.PrintStream;
import java.util.List;

/**
 *
 * @author eric
 */
public interface PrintStreamFactory {
  
  public PrintStream build(List<String> keys);
  
}
