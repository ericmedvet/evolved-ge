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
public interface ComparableFitness<T> extends Fitness<T>, Comparable<ComparableFitness<T>> {
  
}
