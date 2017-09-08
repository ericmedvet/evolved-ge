/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.mapper;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.Sequence;
import it.units.malelab.ege.core.fitness.FitnessComputer;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.mapper.MappingException;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.ge.operator.ProbabilisticMutation;
import it.units.malelab.ege.util.distance.CachedDistance;
import it.units.malelab.ege.util.distance.Distance;
import it.units.malelab.ege.util.distance.Hamming;
import it.units.malelab.ege.util.distance.LeavesEdit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author eric
 */
public class MappingPropertiesFitness implements FitnessComputer<String, MultiObjectiveFitness> {

  private final static int EXPRESSIVENESS_DEPTH = 2;

  private final int maxMappingDepth;
  private final List<Problem<String, NumericFitness>> problems;
  private final List<BitsGenotype> genotypes;
  private final Distance<Node<String>> phenotypeDistance = new CachedDistance<>(new LeavesEdit<String>());
  
  private final double[] genotypeDistances;

  public MappingPropertiesFitness(int genotypeSize, int n, int maxMappingDepth, Random random, List<Problem<String, NumericFitness>> problems) {
    this.maxMappingDepth = maxMappingDepth;
    this.problems = new ArrayList<>(problems);
    //build genotypes
    GeneticOperator<BitsGenotype> mutation = new ProbabilisticMutation(random, 0.01d);
    BitsGenotypeFactory factory = new BitsGenotypeFactory(genotypeSize);
    Set<BitsGenotype> set = new LinkedHashSet<>();
    for (int i = 0; i<Math.floor(Math.sqrt(n)); i++) {
      set.addAll(consecutiveMutations(factory.build(random), (int)Math.floor(Math.sqrt(n)), mutation));
    }
    while (set.size() < n) {
      set.add(factory.build(random));
    }
    genotypes = new ArrayList<>(set);
    //pre compute geno dists
    Distance<Sequence<Boolean>> genotypeDistance = new Hamming<Boolean>();
    genotypeDistances = computeDistances(genotypes, (Distance) genotypeDistance);
  }
  
  private List<BitsGenotype> consecutiveMutations(BitsGenotype g, int n, GeneticOperator<BitsGenotype> mutation) {
    Set<BitsGenotype> set = new LinkedHashSet<>();
    while (set.size()<n) {
      set.add(g);
      g = mutation.apply(Collections.singletonList(g)).get(0);
    }
    return new ArrayList<>(set);
  }

  @Override
  public MultiObjectiveFitness compute(Node<String> mapperRawPhenotype) {
    Map<String, double[]> propertyValues = new LinkedHashMap<>();
    propertyValues.put("redundancy", new double[problems.size()]);
    propertyValues.put("nonUniformity", new double[problems.size()]);
    propertyValues.put("nonLocality", new double[problems.size()]);
    for (int i = 0; i < problems.size(); i++) {
      List<Node<String>> phenotypes = new ArrayList<>();
      Multiset<Node<String>> groups = LinkedHashMultiset.create();
      //build mapper
      RecursiveMapper<String> mapper = new RecursiveMapper<>(mapperRawPhenotype, maxMappingDepth, EXPRESSIVENESS_DEPTH, problems.get(i).getGrammar());
      //map
      for (BitsGenotype genotype : genotypes) {
        Node<String> phenotype = Node.EMPTY_TREE;
        try {
          phenotype = mapper.map(genotype, Collections.EMPTY_MAP);
        } catch (MappingException ex) {
          //ignore
        }
        phenotypes.add(phenotype);
        groups.add(phenotype);
      }
      //compute properties
      propertyValues.get("redundancy")[i] = 1d - (double)groups.elementSet().size() / (double)genotypes.size();
      double[] groupSizes = new double[groups.elementSet().size()];
      int c = 0;
      for (Node<String> phenotype : groups.elementSet()) {
        groupSizes[c] = (double) groups.count(phenotype);
        c = c + 1;
      }
      propertyValues.get("nonUniformity")[i] = Math.sqrt(StatUtils.variance(groupSizes)) / StatUtils.mean(groupSizes);
      double[] phenotypeDistances = computeDistances(phenotypes, phenotypeDistance);
      double locality = 1d-(1d+(new PearsonsCorrelation().correlation(genotypeDistances, phenotypeDistances)))/2d;
      propertyValues.get("nonLocality")[i] = Double.isNaN(locality)?2d:locality;
    }
    Double[] avgPropertyValues = new Double[3];
    avgPropertyValues[0] = StatUtils.mean(propertyValues.get("redundancy"));
    avgPropertyValues[1] = StatUtils.mean(propertyValues.get("nonUniformity"));
    avgPropertyValues[2] = StatUtils.mean(propertyValues.get("nonLocality"));
    MultiObjectiveFitness mof = new MultiObjectiveFitness(avgPropertyValues);
    return mof;
  }

  @Override
  public MultiObjectiveFitness worstValue() {
    return new MultiObjectiveFitness(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
  }

  private <E> double[] computeDistances(List<E> elements, Distance<E> distance) {
    double[] dists = new double[elements.size() * (elements.size() - 1) / 2];
    int c = 0;
    for (int i = 0; i < elements.size() - 1; i++) {
      for (int j = i + 1; j < elements.size(); j++) {
        dists[c] = distance.d(elements.get(i), elements.get(j));
        c = c + 1;
      }
    }
    return dists;
  }

}
