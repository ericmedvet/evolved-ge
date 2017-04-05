/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.listener.event;

import it.units.malelab.ege.core.Evolver;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.event.TimeEvent;
import it.units.malelab.ege.ge.GEIndividual;
import it.units.malelab.ege.ge.genotype.Genotype;
import it.units.malelab.ege.ge.operator.GeneticOperator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class OperatorApplicationEvent<G extends Genotype, T, F extends Fitness> extends TimeEvent<T, F> {
  
  private final List<GEIndividual<G, T, F>> parents;
  private final List<GEIndividual<G, T, F>> children;
  private final GeneticOperator operator;

  public OperatorApplicationEvent(List<GEIndividual<G, T, F>> parents, List<GEIndividual<G, T, F>> children, GeneticOperator operator, long elapsedNanos, int generation, Evolver<T, F> evolver, Map<String, Object> data) {
    super(elapsedNanos, generation, evolver, data);
    this.parents = parents;
    this.children = children;
    this.operator = operator;
  }

  public List<GEIndividual<G, T, F>> getParents() {
    return parents;
  }

  public List<GEIndividual<G, T, F>> getChildren() {
    return children;
  }

  public GeneticOperator getOperator() {
    return operator;
  }

}
