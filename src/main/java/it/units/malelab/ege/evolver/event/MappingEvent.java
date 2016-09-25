/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.Node;
import it.units.malelab.ege.evolver.Evolver;

/**
 *
 * @author eric
 */
public class MappingEvent<T> extends TimeEvent<T> {
  
  private final Genotype genotype;
  private final Node<T> phenotype;

  public MappingEvent(Genotype genotype, Node<T> phenotype, long elapsedNanos, int generation, Evolver<T> evolver) {
    super(elapsedNanos, generation, evolver);
    this.genotype = genotype;
    this.phenotype = phenotype;
  }

  public Genotype getGenotype() {
    return genotype;
  }

  public Node<T> getPhenotype() {
    return phenotype;
  }
  
}
