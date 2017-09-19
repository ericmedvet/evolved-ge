/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import java.util.Map;

/**
 *
 * @author eric
 */
public interface JobDataListener {
  
  public void listen(Job job, Map<String, Object> data);
  
}
