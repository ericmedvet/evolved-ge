/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import it.units.malelab.ege.core.Node;
import java.util.List;
import java.util.concurrent.Future;

/**
 *
 * @author eric
 */
public interface JobExecutor {
  
  public Future<List<Node>> submit(final Job job);
  
}
