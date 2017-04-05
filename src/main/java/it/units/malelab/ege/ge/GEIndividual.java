/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.grammar.Node;
import it.units.malelab.ege.ge.genotype.Genotype;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class GEIndividual<G extends Genotype, T, F extends Fitness> extends Individual<T, F> {
  
  private final G genotype;

  public GEIndividual(G genotype, Node<T> phenotype, F fitness, int birthDate, List<Individual<T, F>> parents, Map<String, Object> otherInfo) {
    super(phenotype, fitness, birthDate, parents, otherInfo);
    this.genotype = genotype;
  }

  public G getGenotype() {
    return genotype;
  }
  
}
