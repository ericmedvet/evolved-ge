/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.collector.Collector;
import it.units.malelab.ege.core.listener.event.EvolutionEvent;
import it.units.malelab.ege.core.listener.event.EvolutionStartEvent;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import it.units.malelab.ege.core.listener.event.MappingEvent;
import it.units.malelab.ege.core.listener.event.OperatorApplicationEvent;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.util.Pair;
import it.units.malelab.ege.util.distance.Distance;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author eric
 */
public class PropertiesListener<G, T, F extends Fitness> extends AbstractListener<G, T, F> implements Collector<G, T, F> {

  private boolean collecting = false;
  private final Map<String, List<Pair<Double, Double>>> distances;
  private final Map<String, Multiset<CountType>> counts;

  private static enum CountType {
    MAPPING, INVALID, OPERATOR_APPLICATION, REDUNDANT, BETTER_FITNESS
  };
  private final static String NO_OPERATOR = "_ALL";

  private final Comparator<F> fitnessComparator;
  private final Distance<G> genotypeDistance;
  private final Distance<Node<T>> phenotypeDistance;
  private final Map<Class<? extends GeneticOperator>, String> operatorNames;

  public PropertiesListener(
          Comparator<F> fitnessComparator,
          Distance<G> genotypeDistance,
          Distance<Node<T>> phenotypeDistance,
          Map<Class<? extends GeneticOperator>, String> operatorNames) {
    super(MappingEvent.class, OperatorApplicationEvent.class);
    this.fitnessComparator = fitnessComparator;
    this.genotypeDistance = genotypeDistance;
    this.phenotypeDistance = phenotypeDistance;
    this.operatorNames = operatorNames;
    distances = new LinkedHashMap<>();
    counts = new LinkedHashMap<>();
  }

  @Override
  public synchronized void listen(EvolutionEvent<G, T, F> event) {
    if (!collecting) {
      collecting = true;
      //reset data
      counts.put(NO_OPERATOR, (Multiset) HashMultiset.create());
      for (String operatorName : operatorNames.values()) {
        counts.put(operatorName, (Multiset) HashMultiset.create());
        distances.put(operatorName, new ArrayList<Pair<Double, Double>>());
      }
    }
    if (event instanceof MappingEvent) {
      MappingEvent<G, T, F> mappingEvent = (MappingEvent<G, T, F>) event;
      counts.get(NO_OPERATOR).add(CountType.MAPPING);
      if (Node.EMPTY_TREE.equals(mappingEvent.getPhenotype())) {
        counts.get(NO_OPERATOR).add(CountType.INVALID);
      }
    }
    if (event instanceof OperatorApplicationEvent) {
      OperatorApplicationEvent<G, T, F> operatorApplicationEvent = (OperatorApplicationEvent<G, T, F>) event;
      String operatorName = getOperatorName(operatorApplicationEvent.getOperator());
      if (operatorName != null) {
        counts.get(operatorName).add(CountType.OPERATOR_APPLICATION);
        //compute distances
        double minGenotypeDistance = Double.POSITIVE_INFINITY;
        double minPhenotypeDistance = Double.POSITIVE_INFINITY;
        for (Individual<G, T, F> parent : operatorApplicationEvent.getParents()) {
          for (Individual<G, T, F> child : operatorApplicationEvent.getChildren()) {
            double localGenotypeDistance = genotypeDistance.d(parent.getGenotype(), child.getGenotype());
            if (minGenotypeDistance > localGenotypeDistance) {
              minGenotypeDistance = localGenotypeDistance;
            }
            double localPhenotypeDistance = phenotypeDistance.d(parent.getPhenotype(), child.getPhenotype());
            if (minPhenotypeDistance > localPhenotypeDistance) {
              minPhenotypeDistance = localPhenotypeDistance;
            }
          }
        }
        //check redundancy
        if ((minGenotypeDistance > 0) && (minPhenotypeDistance == 0)) {
          counts.get(operatorName).add(CountType.REDUNDANT);
        }
        //add sample for locality
        distances.get(operatorName).add(new Pair<>(minGenotypeDistance, minPhenotypeDistance));
        //compute best fitnesses
        F bestParentFitness = null;
        for (Individual<G, T, F> parent : operatorApplicationEvent.getParents()) {
          if ((bestParentFitness == null) || (fitnessComparator.compare(parent.getFitness(), bestParentFitness) < 0)) {
            bestParentFitness = parent.getFitness();
          }
        }
        F bestChildFitness = null;
        for (Individual<G, T, F> child : operatorApplicationEvent.getChildren()) {
          if ((bestChildFitness == null) || (fitnessComparator.compare(child.getFitness(), bestChildFitness) < 0)) {
            bestChildFitness = child.getFitness();
          }
        }
        //check evolvability
        if (fitnessComparator.compare(bestChildFitness, bestParentFitness)<0) {
          counts.get(operatorName).add(CountType.BETTER_FITNESS);
        }
      }
    }
  }
  
  private String getOperatorName(GeneticOperator<G> geneticOperator) {
    for (Map.Entry<Class<? extends GeneticOperator>, String> entry : operatorNames.entrySet()) {
      if (entry.getKey().isAssignableFrom(geneticOperator.getClass())) {
        return entry.getValue();
      }
    }
    return null;
  }

  @Override
  public Map<String, String> getFormattedNames() {
    LinkedHashMap<String, String> formattedNames = new LinkedHashMap<>();
    formattedNames.put("properties.mapping.invalidity", "%4.2f");
    formattedNames.put("properties.mapping.count", "%4d");
    for (String operatorName : operatorNames.values()) {
      formattedNames.put("properties.operator."+operatorName+".redundancy", "%4.2f");
      formattedNames.put("properties.operator."+operatorName+".locality", "%+4.2f");
      formattedNames.put("properties.operator."+operatorName+".evolvability", "%4.2f");
      formattedNames.put("properties.operator."+operatorName+".count", "%4d");
    }
    return formattedNames;
  }

  @Override
  public Map<String, Object> collect(GenerationEvent<G, T, F> generationEvent) {
    collecting = false;
    //compute aggregates
    LinkedHashMap<String, Object> values = new LinkedHashMap<>();
    values.put("properties.mapping.invalidity", (double)counts.get(NO_OPERATOR).count(CountType.INVALID)/(double)counts.get(NO_OPERATOR).count(CountType.MAPPING));
    values.put("properties.mapping.count", counts.get(NO_OPERATOR).count(CountType.MAPPING));
    for (String operatorName : operatorNames.values()) {
      values.put("properties.operator."+operatorName+".redundancy",
              (double)counts.get(operatorName).count(CountType.REDUNDANT)/(double)counts.get(operatorName).count(CountType.OPERATOR_APPLICATION)
      );
      values.put("properties.operator."+operatorName+".locality",
              pearsonCorrelation(distances.get(operatorName))
      );
      values.put("properties.operator."+operatorName+".evolvability",
              (double)counts.get(operatorName).count(CountType.BETTER_FITNESS)/(double)counts.get(operatorName).count(CountType.OPERATOR_APPLICATION)
      );
      values.put("properties.operator."+operatorName+".count",
              counts.get(operatorName).count(CountType.OPERATOR_APPLICATION)
      );
    }
    return values;
  }
  
  private double pearsonCorrelation(List<Pair<Double, Double>> values) {
    if (values.isEmpty()) {
      return Double.NaN;
    }
    double[] x = new double[values.size()];
    double[] y = new double[values.size()];
    for (int i = 0; i<values.size(); i++) {
      x[i] = values.get(i).getFirst();
      y[i] = values.get(i).getSecond();
    }
    return new PearsonsCorrelation().correlation(x, y);
  }

}
