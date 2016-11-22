/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver;

import it.units.malelab.ege.evolver.fitness.Fitness;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.Node;
import it.units.malelab.ege.evolver.operator.GeneticOperator;
import java.util.ArrayList;
import java.util.List;

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

  public Individual(G genotype, Node<T> phenotype, Fitness fitness, int birthDate, GeneticOperator<G> operator, List<Individual<G, T>> parents) {
    this.genotype = genotype;
    this.phenotype = phenotype;
    this.fitness = fitness;
    this.birthDate = birthDate;
    this.operator = operator;
    this.parents = new ArrayList<>();
    if (parents!=null) {
      this.parents.addAll(parents);
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
  
}
