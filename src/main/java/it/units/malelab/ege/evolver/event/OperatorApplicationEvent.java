/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.operator.GeneticOperator;
import java.util.List;

/**
 *
 * @author eric
 */
public class OperatorApplicationEvent<G extends Genotype, T> extends TimeEvent<G, T> {
  
  private final List<Individual<G, T>> parents;
  private final List<Individual<G, T>> children;
  private final GeneticOperator operator;

  public OperatorApplicationEvent(List<Individual<G, T>> parents, List<Individual<G, T>> children, GeneticOperator operator, long elapsedNanos, int generation, Evolver<G, T> evolver) {
    super(elapsedNanos, generation, evolver);
    this.parents = parents;
    this.children = children;
    this.operator = operator;
  }
  
  public List<Individual<G, T>> getParents() {
    return parents;
  }

  public List<Individual<G, T>> getChildren() {
    return children;
  }

  public GeneticOperator getOperator() {
    return operator;
  }
  
}
