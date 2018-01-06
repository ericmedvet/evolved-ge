/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver.geneoptimalmixing.fos;

import it.units.malelab.ege.core.ConstrainedSequence;
import it.units.malelab.ege.core.Sequence;
import it.units.malelab.ege.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author eric
 */
public class UPGMAMutualInformationTree implements FOSBuilder {

  private final int minSubsetSize;

  public UPGMAMutualInformationTree(int minSubsetSize) {
    this.minSubsetSize = minSubsetSize;
  }

  @Override
  public Set<Set<Integer>> build(List<ConstrainedSequence> sequences, Random random) {
    int minMaxIndex = Integer.MAX_VALUE;
    for (Sequence sequence : sequences) {
      minMaxIndex = Math.min(minMaxIndex, sequence.size());
    }
    //compute mutual information table
    Map<Pair<Set<Integer>, Set<Integer>>, Double> dMap = computeInitialDistanceMap(minMaxIndex, sequences);
    //prepare
    Set<Set<Integer>> fos = new LinkedHashSet<>();
    Set<Set<Integer>> currentSubsets = new LinkedHashSet<>();
    //build base subsets
    for (int index = 0; index < minMaxIndex; index++) {
      Set<Integer> newSubset = Collections.singleton(index);
      currentSubsets.add(newSubset);
      if (newSubset.size() >= minSubsetSize) {
        fos.add(newSubset);
      }
    }
    //iterate (see pag 2 of http://nwwwn.cs.technion.ac.il/users/wwwb/cgi-bin/tr-get.cgi/2007/CS/CS-2007-06.pdf)
    while (currentSubsets.size() > 1) {
      Set<Integer> newSubset = new LinkedHashSet<>();
      Pair<Set<Integer>, Set<Integer>> chosenSubsets = choosePair(currentSubsets, dMap, random);
      newSubset.addAll(chosenSubsets.getFirst());
      newSubset.addAll(chosenSubsets.getSecond());
      currentSubsets.remove(chosenSubsets.getFirst());
      currentSubsets.remove(chosenSubsets.getSecond());
      currentSubsets.add(newSubset);
      if (newSubset.size() >= minSubsetSize) {
        fos.add(newSubset);
      }
      //new distance in map
      for (Set<Integer> subset : currentSubsets) {
        if (!subset.equals(newSubset)) {
          double d = (double)chosenSubsets.getFirst().size()/(double)newSubset.size()*dMap.get(new Pair<>(subset, chosenSubsets.getFirst()));
          d = d+(double)chosenSubsets.getSecond().size()/(double)newSubset.size()*dMap.get(new Pair<>(subset, chosenSubsets.getSecond()));
          dMap.put (new Pair<>(subset, newSubset), d);
          dMap.put (new Pair<>(newSubset, subset), d);
        }        
      }
    }
    return fos;
  }

  protected Map<Pair<Set<Integer>, Set<Integer>>, Double> computeInitialDistanceMap(int minMaxIndex, List<ConstrainedSequence> sequences) {
    Map<Pair<Set<Integer>, Set<Integer>>, Double> map = new LinkedHashMap<>();
    List<List<Object>> domains = new ArrayList<>();
    for (int i = 0; i< minMaxIndex; i++) {
      domains.add(new ArrayList<>(sequences.get(0).domain(i)));
    }
    for (int i = 0; i< minMaxIndex; i++) {
      for (int j = i+1; j<minMaxIndex; j++) {
        Object[] aValues = new Object[sequences.size()];
        Object[] bValues = new Object[sequences.size()];
        for (int s = 0; s< sequences.size(); s++) {
          aValues[s] = sequences.get(s).get(i);
          bValues[s] = sequences.get(s).get(j);
        }
        double localMi = computeMI(aValues, domains.get(i), bValues, domains.get(j));
        map.put(new Pair<>(Collections.singleton(i), Collections.singleton(j)), localMi);
        map.put(new Pair<>(Collections.singleton(j), Collections.singleton(i)), localMi);
      }
    }
    return map;
  }

  protected Pair<Set<Integer>, Set<Integer>> choosePair(
          Set<Set<Integer>> subsets,
          Map<Pair<Set<Integer>, Set<Integer>>, Double> miMap,
          Random random) {
    List<Set<Integer>> list = new ArrayList<>(subsets);
    int bestI = 0;
    int bestJ = 1;
    double bestMi = Double.NEGATIVE_INFINITY;
    for (int i = 0; i<list.size(); i++) {
      for (int j = i+1; j<list.size(); j++) {
        Pair<Set<Integer>, Set<Integer>> keyPair = new Pair<>(list.get(i), list.get(j));
        Double currentMi = miMap.get(keyPair);
        if (currentMi>bestMi) {
          bestMi = currentMi;
          bestI = i;
          bestJ = j;
        }
      }
    }
    return new Pair<>(list.get(bestI), list.get(bestJ));
  }
  
  private double computeMI(Object[] aValues, List<Object> aDomain, Object[] bValues, List<Object> bDomain) {
    int[] a = new int[aValues.length];
    for (int i = 0; i<aValues.length; i++) {
      a[i] = aDomain.indexOf(aValues[i]);
    }
    int[] b = new int[bValues.length];
    for (int i = 0; i<bValues.length; i++) {
      b[i] = bDomain.indexOf(aValues[i]);
    }
    return computeMI(a, aDomain.size(), b, bDomain.size());
  }

  /**
   * *
   * Computes MI between variables t and a. Assumes that a.length == t.length.
   * From: https://stackoverflow.com/a/24955625/1003056
   *
   * @param a candidate variable a
   * @param avals number of values a can take (max(a) == avals)
   * @param t target variable
   * @param tvals number of values a can take (max(t) == tvals)
   * @return
   */
  private double computeMI(int[] a, int avals, int[] t, int tvals) {
    double numinst = a.length;
    double oneovernuminst = 1 / numinst;
    double sum = 0;

    // longs are required here because of big multiples in calculation
    long[][] crosscounts = new long[avals][tvals];
    long[] tcounts = new long[tvals];
    long[] acounts = new long[avals];
    // Compute counts for the two variables
    for (int i = 0; i < a.length; i++) {
      int av = a[i];
      int tv = t[i];
      acounts[av]++;
      tcounts[tv]++;
      crosscounts[av][tv]++;
    }

    for (int tv = 0; tv < tvals; tv++) {
      for (int av = 0; av < avals; av++) {
        if (crosscounts[av][tv] != 0) {
          // Main fraction: (n|x,y|)/(|x||y|)
          double sumtmp = (numinst * crosscounts[av][tv]) / (acounts[av] * tcounts[tv]);
          // Log bit (|x,y|/n) and update product
          sum += oneovernuminst * crosscounts[av][tv] * Math.log(sumtmp) * Math.log(2);
        }
      }

    }

    return sum;
  }

}
