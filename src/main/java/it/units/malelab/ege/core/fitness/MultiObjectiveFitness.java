/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.fitness;

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

}
