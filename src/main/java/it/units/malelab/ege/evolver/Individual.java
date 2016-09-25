/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver;

import it.units.malelab.ege.evolver.fitness.Fitness;
import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.Node;

/**
 *
 * @author eric
 */
public class Individual<T> {
  
  private final Genotype genotype;
  private final Node<T> phenotype;
  private final Fitness fitness;

  public Individual(Genotype genotype, Node<T> phenotype, Fitness fitness) {
    this.genotype = genotype;
    this.phenotype = phenotype;
    this.fitness = fitness;
  }

  public Genotype getGenotype() {
    return genotype;
  }

  public Node<T> getPhenotype() {
    return phenotype;
  }

  public Fitness getFitness() {
    return fitness;
  }
  
}
