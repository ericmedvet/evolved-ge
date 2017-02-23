/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver;

import it.units.malelab.ege.evolver.fitness.Fitness;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.grammar.Node;
import it.units.malelab.ege.evolver.operator.GeneticOperator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class Individual<G extends Genotype, T> {
  
  private final G genotype;
  private final Node<T> phenotype;
  private final Fitness fitness;
  private final int birthDate;
  private final GeneticOperator<G> operator;
  private final List<Individual<G, T>> parents;
  private final Map<String, Object> otherInfo;

  public Individual(G genotype, Node<T> phenotype, Fitness fitness, int birthDate, GeneticOperator<G> operator, List<Individual<G, T>> parents, Map<String, Object> otherInfo) {
    this.genotype = genotype;
    this.phenotype = phenotype;
    this.fitness = fitness;
    this.birthDate = birthDate;
    this.operator = operator;
    this.parents = new ArrayList<>();
    if (parents!=null) {
      this.parents.addAll(parents);
    }
    this.otherInfo = new LinkedHashMap<>();
    if (otherInfo!=null) {
      this.otherInfo.putAll(otherInfo);
    }
  }

  public G getGenotype() {
    return genotype;
  }

  public Node<T> getPhenotype() {
    return phenotype;
  }

  public Fitness getFitness() {
    return fitness;
  }

  public int getBirthDate() {
    return birthDate;
  }

  public GeneticOperator<G> getOperator() {
    return operator;
  }

  public List<Individual<G, T>> getParents() {
    return parents;
  }

  public Map<String, Object> getOtherInfo() {
    return otherInfo;
  }  

}
