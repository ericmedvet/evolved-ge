/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed.worker;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 *
 * @author eric
 */
public class StatsRunnable implements Runnable {

  private final Worker worker;

  private final static OperatingSystemMXBean OS = ManagementFactory.getOperatingSystemMXBean();
  
  public final static String STAT_CPU_SYSTEM_NAME = "cpu.system";
  public final static String STAT_CPU_PROCESS_NAME = "cpu.process";
  public final static String STAT_FREE_MEM_NAME = "memory.free";
  public final static String STAT_MAX_MEM_NAME = "memory.max";

  public StatsRunnable(Worker worker) {
    this.worker = worker;
  }

  @Override
  public void run() {
    worker.getStats().put(STAT_CPU_SYSTEM_NAME, OS.getSystemLoadAverage());
    worker.getStats().put(STAT_MAX_MEM_NAME, Runtime.getRuntime().maxMemory());
    worker.getStats().put(STAT_FREE_MEM_NAME, Runtime.getRuntime().freeMemory());
  }

}
