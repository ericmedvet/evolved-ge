/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import it.units.malelab.ege.Node;
import it.units.malelab.ege.Pair;
import it.units.malelab.ege.distance.Distance;
import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.evolver.event.OperatorApplicationEvent;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author eric
 */
public class DynamicLocalityAnalysisLogger<G extends Genotype, T> extends AbstractGenerationLogger<G, T> {

  private final PrintStream ps;
  private final Distance<G> genotypeDistance;
  private final Distance<Node<T>> phenotypeDistance;
  private final Multimap<String, Pair<Double, Double>> distances;
  private boolean headerWritten;

  public DynamicLocalityAnalysisLogger(PrintStream ps, Distance<G> genotypeDistance, Distance<Node<T>> phenotypeDistance, String prefix) {
    super(null, prefix);
    this.ps = ps;
    this.genotypeDistance = genotypeDistance;
    this.phenotypeDistance = phenotypeDistance;
    eventClasses.add(OperatorApplicationEvent.class);
    distances = LinkedHashMultimap.create();
    headerWritten = false;
  }

  @Override
  public void listen(EvolutionEvent<G, T> event) {
    if (event instanceof GenerationEvent) {
      int generation = ((GenerationEvent) event).getGeneration();
      List<Individual<G, T>> population = new ArrayList<>(((GenerationEvent) event).getPopulation());
      Map<String, Object> indexes = computeIndexes(generation, population);
      for (String operatorName : distances.keySet()) {
        List<Pair<Double, Double>> pairs = new ArrayList<>(distances.get(operatorName));
        double[] gds = new double[pairs.size()];
        double[] pds = new double[pairs.size()];
        for (int i = 0; i < pairs.size(); i++) {
          gds[i] = pairs.get(i).getFirst();
          pds[i] = pairs.get(i).getSecond();
        }
        double corr = Double.NaN;
        try {
          corr = ((new PearsonsCorrelation()).correlation(gds, pds));
        } catch (MathIllegalArgumentException e) {
          //ignore: leave at NaN
        }
        indexes.put("opCorr" + operatorName, corr);
      }
      SortedMap<String, Object> sortedIndexes = new TreeMap<>(indexes);
      if (!headerWritten) {
        for (String key : sortedIndexes.keySet()) {
          ps.print(key);
          if (!sortedIndexes.lastKey().equals(key)) {
            ps.print(";");
          }
        }
        ps.println();
      }
      for (Map.Entry<String, Object> entry : sortedIndexes.entrySet()) {
        ps.print(entry.getValue());
        if (!sortedIndexes.lastKey().equals(entry.getKey())) {
          ps.print(";");
        }
      }
      ps.println();
      distances.clear();
    } else if (event instanceof OperatorApplicationEvent) {
      OperatorApplicationEvent<G, T> e = ((OperatorApplicationEvent) event);
      //assume 1 child and 1/2 parents
      double gd = genotypeDistance.d(e.getParents().get(0).getGenotype(), e.getChildren().get(0).getGenotype());
      double pd = phenotypeDistance.d(e.getParents().get(0).getPhenotype(), e.getChildren().get(0).getPhenotype());
      if (e.getParents().size() > 1) {
        for (int i = 1; i < e.getParents().size(); i++) {
          double localGd = genotypeDistance.d(e.getParents().get(i).getGenotype(), e.getChildren().get(0).getGenotype());
          double localPd = phenotypeDistance.d(e.getParents().get(i).getPhenotype(), e.getChildren().get(0).getPhenotype());
          if (localGd < gd) {
            gd = localGd;
            pd = localPd;
          }
        }
      }
      distances.get(e.getOperator().getClass().getSimpleName()).add(new Pair<>(gd, pd));
    }
  }

}
