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
  private final Map<Problem<String, NumericFitness>, Distance<Node<String>>> problems;
  private final List<BitsGenotype> genotypes;
  private final Property[] properties;
  
  private final double[] genotypeDistances;
  
  public static enum Property {REDUNDANCY, NON_UNIFORMITY, NON_LOCALITY};

  public MappingPropertiesFitness(int genotypeSize, int n, int maxMappingDepth, Random random, List<Problem<String, NumericFitness>> problems, Property... properties) {
    this.maxMappingDepth = maxMappingDepth;
    this.problems = new LinkedHashMap<>();
    for (Problem<String, NumericFitness> problem: problems) {
      this.problems.put(problem, new CachedDistance<>(new LeavesEdit<String>()));
    }
    this.properties = properties;
    //build genotypes
    GeneticOperator<BitsGenotype> mutation = new ProbabilisticMutation(0.01d);
    BitsGenotypeFactory factory = new BitsGenotypeFactory(genotypeSize);
    Set<BitsGenotype> set = new LinkedHashSet<>();
    for (int i = 0; i<Math.floor(Math.sqrt(n)); i++) {
      set.addAll(consecutiveMutations(factory.build(random), (int)Math.floor(Math.sqrt(n)), mutation, random));
    }
    while (set.size() < n) {
      set.add(factory.build(random));
    }
    genotypes = new ArrayList<>(set);
    //pre compute geno dists
    Distance<Sequence<Boolean>> genotypeDistance = new Hamming<Boolean>();
    genotypeDistances = computeDistances(genotypes, (Distance) genotypeDistance);
  }
  
  private List<BitsGenotype> consecutiveMutations(BitsGenotype g, int n, GeneticOperator<BitsGenotype> mutation, Random random) {
    Set<BitsGenotype> set = new LinkedHashSet<>();
    while (set.size()<n) {
      set.add(g);
      g = mutation.apply(Collections.singletonList(g), random).get(0);
    }
    return new ArrayList<>(set);
  }

  @Override
  public MultiObjectiveFitness compute(Node<String> mapperRawPhenotype) {
    Map<Property, double[]> propertyValues = new LinkedHashMap<>();
    for (Property property : properties) {
      propertyValues.put(property, new double[problems.size()]);
    }
    int i = 0;
    for (Problem<String, NumericFitness> problem : problems.keySet()) {
      List<Node<String>> phenotypes = new ArrayList<>();
      Multiset<Node<String>> groups = LinkedHashMultiset.create();
      //build mapper
      RecursiveMapper<String> mapper = new RecursiveMapper<>(mapperRawPhenotype, maxMappingDepth, EXPRESSIVENESS_DEPTH, problem.getGrammar());
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
      if (propertyValues.keySet().contains(Property.REDUNDANCY)) {
        propertyValues.get(Property.REDUNDANCY)[i] = 1d - (double)groups.elementSet().size() / (double)genotypes.size();
      }
      if (propertyValues.keySet().contains(Property.NON_UNIFORMITY)) {
        double[] groupSizes = new double[groups.elementSet().size()];
        int c = 0;
        for (Node<String> phenotype : groups.elementSet()) {
          groupSizes[c] = (double) groups.count(phenotype);
          c = c + 1;
        }
        propertyValues.get(Property.NON_UNIFORMITY)[i] = Math.sqrt(StatUtils.variance(groupSizes)) / StatUtils.mean(groupSizes);
      }
      if (propertyValues.keySet().contains(Property.NON_LOCALITY)) {
        double[] phenotypeDistances = computeDistances(phenotypes, problems.get(problem));
        double locality = 1d-(1d+(new PearsonsCorrelation().correlation(genotypeDistances, phenotypeDistances)))/2d;
        propertyValues.get(Property.NON_LOCALITY)[i] = Double.isNaN(locality)?1d:locality;
      }
      i = i+1;
    }
    Double[] meanValues = new Double[properties.length];
    for (int j = 0; j<properties.length; j++) {
      meanValues[j] = StatUtils.mean(propertyValues.get(properties[j]));
    }
    MultiObjectiveFitness mof = new MultiObjectiveFitness(meanValues);
    return mof;
  }

  @Override
  public MultiObjectiveFitness worstValue() {
    Double[] worstValues = new Double[properties.length];
    for (int i = 0; i<properties.length; i++) {
      worstValues[i] = Double.POSITIVE_INFINITY;
    }
    MultiObjectiveFitness mof = new MultiObjectiveFitness(worstValues);
    return mof;
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
