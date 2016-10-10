/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.Node;
import it.units.malelab.ege.distance.Distance;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.OperatorApplicationEvent;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eric
 */
public class DynamicLocalityAnalysisLogger<G extends Genotype, T> implements EvolutionListener<G, T> {

  private final PrintStream ps;
  private final Map<String, Distance<G>> genotypeDistances;
  private final Map<String, Distance<Node<T>>> phenotypeDistances;
  private Map<String, Object> constants;
  private boolean headerWritten;
  private final Set<Class<? extends EvolutionEvent>> eventClasses;
  private final List<String> genotypeDistanceNames;
  private final List<String> phenotypeDistanceNames;
  private final List<String> constantNames;
  
  private final static int PS_BUFFER = 100*100;

  public DynamicLocalityAnalysisLogger(PrintStream ps, Map<String, Distance<G>> genotypeDistances, Map<String, Distance<Node<T>>> phenotypeDistances, Map<String, Object> constants) {
    this.ps = new PrintStream(new BufferedOutputStream(ps, PS_BUFFER));
    this.genotypeDistances = genotypeDistances;
    this.phenotypeDistances = phenotypeDistances;
    this.constants = constants;
    eventClasses = new LinkedHashSet<>();
    eventClasses.add(OperatorApplicationEvent.class);
    genotypeDistanceNames = new ArrayList<>(genotypeDistances.keySet());
    phenotypeDistanceNames = new ArrayList<>(phenotypeDistances.keySet());
    constantNames = new ArrayList<>(constants.keySet());
  }

  @Override
  public synchronized void listen(EvolutionEvent<G, T> event) {
    if (!headerWritten) {
      ps.print("generation");
      ps.print(";p0genoSize;p1genoSize;p0phenoSize;p1phenoSize");
      ps.print(";c0genoSize;c0phenoSize");
      for (String name : genotypeDistanceNames) {
        ps.printf(";pc00genoDist%s;pc10genoDist%s", name, name);
      }
      for (String name : phenotypeDistanceNames) {
        ps.printf(";pc00phenoDist%s;pc10phenoDist%s", name, name);
      }
      ps.print(";operator");
      for (String name : constantNames) {
        ps.printf(";%s", name);
      }
      ps.println();
      headerWritten = true;
    }
    OperatorApplicationEvent<G, T> e = ((OperatorApplicationEvent) event);
    //assume 1 child and 1 or 2 parents
    ps.printf("%d;%d;%d;%d;%d;%d;%d",
            e.getGeneration(),
            e.getParents().get(0).getGenotype().size(),
            e.getParents().size() > 1 ? e.getParents().get(1).getGenotype().size() : null,
            (Node.EMPTY_TREE.equals(e.getParents().get(0).getPhenotype()))?null:e.getParents().get(0).getPhenotype().size(),
            ((e.getParents().size() == 1)||Node.EMPTY_TREE.equals(e.getParents().get(1).getPhenotype())) ? null:e.getParents().get(1).getPhenotype().size(),
            e.getChildren().get(0).getGenotype().size(),
            (Node.EMPTY_TREE.equals(e.getChildren().get(0).getPhenotype()))?null:e.getChildren().get(0).getPhenotype().size()
    );
    for (String name : genotypeDistanceNames) {
      ps.printf(";%f;%f",
              genotypeDistances.get(name).d(e.getParents().get(0).getGenotype(), e.getChildren().get(0).getGenotype()),
              e.getParents().size() > 1 ? genotypeDistances.get(name).d(e.getParents().get(1).getGenotype(), e.getChildren().get(0).getGenotype()) : null
      );
    }
    for (String name : phenotypeDistanceNames) {
      Double d00 = (Node.EMPTY_TREE.equals(e.getParents().get(0).getPhenotype())||Node.EMPTY_TREE.equals(e.getChildren().get(0).getPhenotype()))?null:phenotypeDistances.get(name).d(e.getParents().get(0).getPhenotype(), e.getChildren().get(0).getPhenotype());
      Double d10 = ((e.getParents().size() == 1)||Node.EMPTY_TREE.equals(e.getParents().get(1).getPhenotype())||Node.EMPTY_TREE.equals(e.getChildren().get(0).getPhenotype()))?null:phenotypeDistances.get(name).d(e.getParents().get(1).getPhenotype(), e.getChildren().get(0).getPhenotype());
      ps.printf(";%f;%f", d00, d10);
    }
    ps.printf(";%s", e.getOperator().getClass().getSimpleName());
    for (String name : constantNames) {
      ps.printf(";%s", constants.get(name));
    }
    ps.println();
  }

  @Override
  public Set<Class<? extends EvolutionEvent>> getEventClasses() {
    return eventClasses;
  }

  public void setConstants(Map<String, Object> constants) {
    this.constants = constants;
  }

}
