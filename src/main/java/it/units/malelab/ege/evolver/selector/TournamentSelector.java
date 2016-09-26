/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.Utils;
import it.units.malelab.ege.evolver.Individual;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class TournamentSelector<G extends Genotype, T> implements Selector<G, T> {
  
  private final int size;
  private final Random random;

  public TournamentSelector(int size, Random random) {
    this.size = size;
    this.random = random;
  }

  @Override
  public Individual<G, T> select(List<Individual<G, T>> population) {
    List<Individual<G, T>> individuals = new ArrayList<>();
    for (int i = 0; i<Math.min(population.size(), size); i++) {
      int index = random.nextInt(population.size());
      individuals.add(population.get(index));
    }
    Utils.sortByFitness(individuals);
    return individuals.get(0);
  }
  
}
