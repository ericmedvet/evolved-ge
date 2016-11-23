/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.Node;
import it.units.malelab.ege.evolver.Evolver;
import java.util.Map;

/**
 *
 * @author eric
 */
public class MappingEvent<G extends Genotype, T> extends TimeEvent<G, T> {
  
  private final G genotype;
  private final Node<T> phenotype;

  public MappingEvent(G genotype, Node<T> phenotype, long elapsedNanos, int generation, Evolver<G, T> evolver, Map<String, Object> data) {
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
