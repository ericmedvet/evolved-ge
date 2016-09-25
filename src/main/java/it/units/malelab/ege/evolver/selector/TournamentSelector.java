/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.selector;

import it.units.malelab.ege.evolver.selector.Selector;
import it.units.malelab.ege.Utils;
import it.units.malelab.ege.evolver.Individual;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class TournamentSelector implements Selector {
  
  private final int size;
  private final Random random;

  public TournamentSelector(int size, Random random) {
    this.size = size;
    this.random = random;
  }

  @Override
  public <T> Individual<T> select(List<Individual<T>> population) {
    List<Individual<T>> individuals = new ArrayList<>();
    for (int i = 0; i<Math.min(population.size(), size); i++) {
      individuals.add(population.get(random.nextInt(population.size())));
    }
    Utils.sortByFitness(individuals);
    return individuals.get(0);
  }
  
}
