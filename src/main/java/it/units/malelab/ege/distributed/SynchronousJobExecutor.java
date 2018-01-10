/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distributed;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.listener.AbstractListener;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.collector.Collector;
import it.units.malelab.ege.core.listener.event.EvolutionEndEvent;
import it.units.malelab.ege.core.listener.event.EvolutionEvent;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import it.units.malelab.ege.distributed.master.Master;
import it.units.malelab.ege.distributed.worker.JobRunnable;
import it.units.malelab.ege.util.Utils;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class SynchronousJobExecutor implements JobExecutor {

  private final ExecutorService executor;
  private final PrintStreamFactory printStreamFactory;
  private final String baseResultFileName;

  private final static Logger L = Logger.getLogger(SynchronousJobExecutor.class.getName());

  public SynchronousJobExecutor(ExecutorService executor, String baseResultDirName, String baseResultFileName) {
    this.executor = executor;
    this.baseResultFileName = baseResultFileName;
    printStreamFactory = new PrintStreamFactory(baseResultDirName);
  }

  @Override
  public Future<List<Node>> submit(final Job job) {
    Evolver evolver = JobRunnable.buildEvolver(job);
    List<EvolverListener> listeners = new ArrayList<>();
    listeners.add(new AbstractListener(GenerationEvent.class) {
      @Override
      public void listen(EvolutionEvent event) {
        //compute data
        Map<String, Object> data = new LinkedHashMap<>();
        int generation = ((GenerationEvent) event).getGeneration();
        data.put(Master.GENERATION_NAME, generation);
        data.put(Master.LOCAL_TIME_NAME, Calendar.getInstance().getTime().getTime());
        for (Collector collector : (List<Collector>) job.getCollectors()) {
          data.putAll(collector.collect((GenerationEvent) event));
        }
        PrintStream ps = printStreamFactory.get(DistributedUtils.jobKeys(job), baseResultFileName);
        DistributedUtils.writeData(ps, job, Collections.singletonList(data));
      }
    });
    listeners.add(new CollectorGenerationLogger(
            Collections.EMPTY_MAP, System.out, true, 10, " ", " | ", (Collector[]) job.getCollectors().toArray()
    ));
    //prepare random
    Integer randomSeed = (Integer) job.getKeys().get(Master.RANDOM_SEED_NAME);
    Random random = new Random();
    if (randomSeed != null) {
      random = new Random(randomSeed.longValue());
    }
    try {
      List<Node> finalBestRank = evolver.solve(executor, random, listeners);
      return Utils.future(finalBestRank);
    } catch (InterruptedException ex) {
      L.log(Level.SEVERE, String.format("Interrupted job: %s %s", job.getId(), job.getKeys()), ex);
    } catch (ExecutionException ex) {
      L.log(Level.SEVERE, String.format("Exception in job: %s %s", job.getId(), job.getKeys()), ex);
    }
    return Utils.future(null);
  }

  public ExecutorService getExecutor() {
    return executor;
  }

}
