/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.fitness;

/**
 *
 * @author eric
 */
public class MultiObjectiveFitness implements Fitness<Comparable[]> {
  
  private final Comparable[] values;

  public MultiObjectiveFitness(Comparable... values) {
    this.values = values;
  }    

  @Override
  public Comparable[] getValue() {
    return values;
  }

  @Override
  public int compareTo(Fitness o) {
    //pareto dominance
    if (!(o instanceof MultiObjectiveFitness)) {
      return -1;
    }
    MultiObjectiveFitness mof = (MultiObjectiveFitness)o;
    if (mof.getValue().length!=values.length) {
      return -1;
    }
    int better = 0;
    int worse = 0;
    for (int i = 0; i<values.length; i++) {
      int outcome = values[i].compareTo(mof.getValue()[i]);
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
