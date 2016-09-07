/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.distance;

/**
 *
 * @author eric
 */
public interface Distance<T> {
  
  public double d(T t1, T t2);
  
}
