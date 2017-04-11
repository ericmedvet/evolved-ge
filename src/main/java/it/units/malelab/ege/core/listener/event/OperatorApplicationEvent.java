/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener.event;

import it.units.malelab.ege.core.evolver.Evolver;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.event.TimeEvent;
import it.units.malelab.ege.core.operator.GeneticOperator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class OperatorApplicationEvent<G, T, F extends Fitness> extends TimeEvent<G, T, F> {
  
  private final List<Individual<G, T, F>> parents;
  private final List<Individual<G, T, F>> children;
  private final GeneticOperator operator;

  public OperatorApplicationEvent(List<Individual<G, T, F>> parents, List<Individual<G, T, F>> children, GeneticOperator operator, long elapsedNanos, int generation, Evolver<G, T, F> evolver, Map<String, Object> data) {
    super(elapsedNanos, generation, evolver, data);
    this.parents = parents;
    this.children = children;
    this.operator = operator;
  }

  public List<Individual<G, T, F>> getParents() {
    return parents;
  }

  public List<Individual<G, T, F>> getChildren() {
    return children;
  }

  public GeneticOperator getOperator() {
    return operator;
  }

}
