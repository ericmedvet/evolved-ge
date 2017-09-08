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
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author eric
 */
public class MappingPropertiesFitness implements FitnessComputer<String, MultiObjectiveFitness> {

  private final static int MAX_MAPPING_DEPTH = 10;
  private final static int EXPRESSIVENESS_DEPTH = 2;

  private final List<Problem<String, NumericFitness>> problems;
  private final List<BitsGenotype> genotypes;
  private final Distance<Node<String>> phenotypeDistance = new CachedDistance<>(new LeavesEdit<String>());
  private final double[] genotypeDistances;

  public MappingPropertiesFitness(int genotypeSize, int n, Random random, Problem<String, NumericFitness>... problems) {
    this.problems = Arrays.asList(problems);
    //build genotypes
    BitsGenotypeFactory factory = new BitsGenotypeFactory(genotypeSize);
    Set<BitsGenotype> set = new LinkedHashSet<>();
    while (set.size() < n) {
      set.add(factory.build(random));
    }
    genotypes = new ArrayList<>(set);
    //pre compute geno dists
    Distance<Sequence<Boolean>> genotypeDistance = new Hamming<Boolean>();
    genotypeDistances = computeDistances(genotypes, (Distance) genotypeDistance);
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
      RecursiveMapper<String> mapper = new RecursiveMapper<>(mapperRawPhenotype, MAX_MAPPING_DEPTH, EXPRESSIVENESS_DEPTH, problems.get(i).getGrammar());
      //map
      for (BitsGenotype genotype : genotypes) {
        Node<String> phenotype = Node.EMPTY_TREE;
        try {
          phenotype = mapper.map(genotype, Collections.EMPTY_MAP);
        } catch (MappingException ex) {
          //ignore
          System.out.println("SHOULDN'T HAPPEN!");
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
      double locality = -(new PearsonsCorrelation().correlation(genotypeDistances, phenotypeDistances));
      propertyValues.get("nonLocality")[i] = Double.isNaN(locality)?Double.POSITIVE_INFINITY:locality;
      
      //System.out.printf("\t\t%d: %4.2f %4.2f %4.2f%n", i,
      //        propertyValues.get("redundancy")[i], propertyValues.get("nonUniformity")[i], propertyValues.get("nonLocality")[i]);
      
    }
    Double[] avgPropertyValues = new Double[3];
    avgPropertyValues[0] = StatUtils.mean(propertyValues.get("redundancy"));
    avgPropertyValues[1] = StatUtils.mean(propertyValues.get("nonUniformity"));
    avgPropertyValues[2] = StatUtils.mean(propertyValues.get("nonLocality"));
    MultiObjectiveFitness mof = new MultiObjectiveFitness(avgPropertyValues);

    //System.out.printf("\t%4.2f %4.2f %4.2f%n", mof.getValue()[0], mof.getValue()[1], mof.getValue()[2]);

    return mof;
  }

  @Override
  public MultiObjectiveFitness worstValue() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
