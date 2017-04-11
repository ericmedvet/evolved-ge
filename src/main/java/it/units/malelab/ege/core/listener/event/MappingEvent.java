/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.event;

import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.listener.event.TimeEvent;
import java.util.Map;

/**
 *
 * @author eric
 */
public class MappingEvent<G, T, F extends Fitness> extends TimeEvent<G, T, F> {
  
  private final G genotype;
  private final Node<T> phenotype;

  public MappingEvent(G genotype, Node<T> phenotype, long elapsedNanos, int generation, Evolver<G, T, F> evolver, Map<String, Object> data) {
    super(elapsedNanos, generation, evolver, data);
    this.genotype = genotype;
    this.phenotype = phenotype;
  }

  public G getGenotype() {
    return genotype;
  }

  public Node<T> getPhenotype() {
    return phenotype;
  }
  
}
