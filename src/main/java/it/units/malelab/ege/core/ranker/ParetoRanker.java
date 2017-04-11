/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.ranker;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author eric
 */
public class ParetoRanker<G, T, F extends MultiObjectiveFitness> implements Ranker<Individual<G, T,F>>, Comparator<MultiObjectiveFitness> {

  @Override
  public List<List<Individual<G, T, F>>> rank(List<Individual<G, T, F>> individuals) {
    List<List<Individual<G, T, F>>> ranks = new ArrayList<>();
    List<Individual<G, T, F>> localIndividuals = new ArrayList<>(individuals);
    while (!localIndividuals.isEmpty()) {
      int[] counts = dominanceCounts(localIndividuals);
      List<Individual<G, T, F>> paretoFront = new ArrayList<>();
      for (int i = 0; i<counts.length; i++) {
        if (counts[i]==0) {
          paretoFront.add(localIndividuals.get(i));
        }
      }
      localIndividuals.removeAll(paretoFront);
      ranks.add(paretoFront);
    }
    return ranks;
  }
  
  private int[] dominanceCounts(List<Individual<G, T, F>> individuals) {
    int[] counts = new int[individuals.size()];
    for (int i = 0; i<individuals.size(); i++) {
      for (int j = i+1; j<individuals.size(); j++) {
        int outcome = compare(individuals.get(i).getFitness(), individuals.get(j).getFitness());
        if (outcome<0) {
          counts[j] = counts[j]+1;
        } else if (outcome>0) {
          counts[i] = counts[i]+1;
        }
      }
    }
    return counts;
  }
  
  @Override
  public int compare(MultiObjectiveFitness f1, MultiObjectiveFitness f2) {
    int better = 0;
    int worse = 0;
    for (int i = 0; i<f1.getValue().length; i++) {
      int outcome = f1.getValue()[i].compareTo(f2.getValue()[i]);
      better = better+((outcome<0)?1:0);
      worse = worse+((outcome>0)?1:0);
    }
    if (better>0&&worse==0) {
      return -1;
    }
    if (worse>0&&better==0) {
      return 1;
    }
    return 0;
  }
  
}
