/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.problem.symbolicregression;

/**
 *
 * @author eric
 */
public enum Operator implements Element {
  
  ADDITION("+"),
  SUBTRACTION("-"),
  DIVISION("/"),
  MULTIPLICATION("*"),
  LOG("log"),
  EXP("exp"),
  SIN("sin"),
  COS("cos"),
  INVERSE("1/"),
  OPPOSITE("_"),
  SQRT("√");
  
  private final String string;

  private Operator(String string) {
    this.string = string;
  }

  @Override
  public String toString() {
    return string;
  }
  
}
