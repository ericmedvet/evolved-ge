/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.BenchmarkProblems;
import it.units.malelab.ege.Node;
import it.units.malelab.ege.distance.Distance;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.OperatorApplicationEvent;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.symbolicregression.MathUtils;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
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
  private final Map<String, Object> constants;
  private final boolean writeHeader;

  private boolean headerWritten;
  private final Set<Class<? extends EvolutionEvent>> eventClasses;
  private final List<String> genotypeDistanceNames;
  private final List<String> phenotypeDistanceNames;
  private final List<String> constantNames;

  private final static int PS_BUFFER = 100 * 100;

  public DynamicLocalityAnalysisLogger(PrintStream ps, Map<String, Distance<G>> genotypeDistances, Map<String, Distance<Node<T>>> phenotypeDistances, Map<String, Object> constants, boolean writeHeader) {
    this.ps = new PrintStream(new BufferedOutputStream(ps, PS_BUFFER));
    this.genotypeDistances = genotypeDistances;
    this.phenotypeDistances = phenotypeDistances;
    this.constants = constants;
    this.writeHeader = writeHeader;
    eventClasses = new LinkedHashSet<>();
    eventClasses.add(OperatorApplicationEvent.class);
    genotypeDistanceNames = Collections.synchronizedList(new ArrayList<>(genotypeDistances.keySet()));
    phenotypeDistanceNames = Collections.synchronizedList(new ArrayList<>(phenotypeDistances.keySet()));
    constantNames = Collections.synchronizedList(new ArrayList<>(constants.keySet()));
  }

  @Override
  public void listen(EvolutionEvent<G, T> event) {
    StringBuilder sb = new StringBuilder();
    printHeader();
    for (String name : constantNames) {
      sb.append(String.format("%s;", constants.get(name)));
    }
    OperatorApplicationEvent<G, T> e = ((OperatorApplicationEvent) event);
    //assume 1 child and 1 or 2 parents
    sb.append(String.format("%d;%d;%d;%d;%d;%d;%d",
            e.getGeneration(),
            e.getParents().get(0).getGenotype().size(),
            e.getParents().size() > 1 ? e.getParents().get(1).getGenotype().size() : null,
            (Node.EMPTY_TREE.equals(e.getParents().get(0).getPhenotype())) ? null : e.getParents().get(0).getPhenotype().size(),
            ((e.getParents().size() == 1) || Node.EMPTY_TREE.equals(e.getParents().get(1).getPhenotype())) ? null : e.getParents().get(1).getPhenotype().size(),
            e.getChildren().get(0).getGenotype().size(),
            (Node.EMPTY_TREE.equals(e.getChildren().get(0).getPhenotype())) ? null : e.getChildren().get(0).getPhenotype().size()
    ));
    for (String name : genotypeDistanceNames) {
      Double d00 = genotypeDistances.get(name).d(e.getParents().get(0).getGenotype(), e.getChildren().get(0).getGenotype());
      Double d10 = e.getParents().size() > 1 ? genotypeDistances.get(name).d(e.getParents().get(1).getGenotype(), e.getChildren().get(0).getGenotype()) : null;
      sb.append(String.format(";%f;%f", d00, d10));
    }
    for (String name : phenotypeDistanceNames) {
      Double d00 = (Node.EMPTY_TREE.equals(e.getParents().get(0).getPhenotype()) || Node.EMPTY_TREE.equals(e.getChildren().get(0).getPhenotype())) ? null : phenotypeDistances.get(name).d(e.getParents().get(0).getPhenotype(), e.getChildren().get(0).getPhenotype());
      Double d10 = ((e.getParents().size() == 1) || Node.EMPTY_TREE.equals(e.getParents().get(1).getPhenotype()) || Node.EMPTY_TREE.equals(e.getChildren().get(0).getPhenotype())) ? null : phenotypeDistances.get(name).d(e.getParents().get(1).getPhenotype(), e.getChildren().get(0).getPhenotype());
      sb.append(String.format(";%f;%f", d00, d10));
    }
    sb.append(String.format(";%s", e.getOperator().getClass().getSimpleName()));
    sb.append("\n");
    print(sb.toString());
  }

  private synchronized void print(String string) {
    ps.print(string);
  }

  private synchronized void printHeader() {
    if (writeHeader && !headerWritten) {
      StringBuilder sb = new StringBuilder();
      for (String name : constantNames) {
        sb.append(String.format("%s;", name));
      }
      sb.append("generation");
      sb.append(";p0genoSize;p1genoSize;p0phenoSize;p1phenoSize");
      sb.append(";c0genoSize;c0phenoSize");
      for (String name : genotypeDistanceNames) {
        sb.append(String.format(";pc00genoDist%s;pc10genoDist%s", name, name));
      }
      for (String name : phenotypeDistanceNames) {
        sb.append(String.format(";pc00phenoDist%s;pc10phenoDist%s", name, name));
      }
      sb.append(";operator");
      sb.append("\n");
      headerWritten = true;
      print(sb.toString());
    }
  }

  @Override
  public Set<Class<? extends EvolutionEvent>> getEventClasses() {
    return eventClasses;
  }

}
