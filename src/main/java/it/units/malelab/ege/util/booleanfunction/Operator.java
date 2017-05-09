/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util.booleanfunction;

/**
 *
 * @author eric
 */
public enum Operator implements Element {
  
  AND(".and"),
  OR(".or"),
  NOT(".not"),
  IF(".if");
  
  private final String string;

  private Operator(String string) {
    this.string = string;
  }

  @Override
  public String toString() {
    return string;
  }
  
}
