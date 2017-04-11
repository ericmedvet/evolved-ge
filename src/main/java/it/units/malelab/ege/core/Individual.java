/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core;

import it.units.malelab.ege.core.fitness.Fitness;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class Individual<G, T, F extends Fitness> {

  private final G genotype;
  private final Node<T> phenotype;
  private final F fitness;
  private final int birthDate;
  private final List<Individual<G, T, F>> parents;
  private final Map<String, Object> otherInfo;

  public Individual(G genotype, Node<T> phenotype, F fitness, int birthDate, List<Individual<G, T, F>> parents, Map<String, Object> otherInfo) {
    this.genotype = genotype;
    this.phenotype = phenotype;
    this.fitness = fitness;
    this.birthDate = birthDate;
    this.parents = new ArrayList<>();
    if (parents != null) {
      this.parents.addAll(parents);
    }
    this.otherInfo = new LinkedHashMap<>();
    if (otherInfo != null) {
      this.otherInfo.putAll(otherInfo);
    }
  }

  public G getGenotype() {
    return genotype;
  }

  public Node<T> getPhenotype() {
    return phenotype;
  }

  public F getFitness() {
    return fitness;
  }

  public int getBirthDate() {
    return birthDate;
  }

  public List<Individual<G, T, F>> getParents() {
    return parents;
  }

  public Map<String, Object> getOtherInfo() {
    return otherInfo;
  }

  @Override
  public String toString() {
    return "Individual{" + "genotype=" + genotype + ", phenotype=" + phenotype + ", fitness=" + fitness + '}';
  }

}
