/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.event;

import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.operator.GeneticOperator;
import java.util.List;

/**
 *
 * @author eric
 */
public class OperatorApplicationEvent<T> extends TimeEvent<T> {
  
  private final List<Individual<T>> parents;
  private final List<Individual<T>> children;
  private final GeneticOperator operator;

  public OperatorApplicationEvent(List<Individual<T>> parents, List<Individual<T>> children, GeneticOperator operator, long elapsedNanos, int generation, Evolver<T> evolver) {
    super(elapsedNanos, generation, evolver);
    this.parents = parents;
    this.children = children;
    this.operator = operator;
  }
  
  public List<Individual<T>> getParents() {
    return parents;
  }

  public List<Individual<T>> getChildren() {
    return children;
  }

  public GeneticOperator getOperator() {
    return operator;
  }
  
}
