/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.mapper.element;

/**
 *
 * @author eric
 */
public enum Function implements Element {

  LENGTH("length"),
  SIZE("size"),
  COUNT("count"),
  COUNT_R("count_r"),
  INT("int"),
  ROTATE_SX("rotate_sx"),
  ROTATE_DX("rotate_dx"),
  SUBSTRING("substring"),
  SPLIT("split"),
  SPLIT_W("split_w"),
  LIST("list"),
  CONCAT("concat"),
  REPEAT("repeat"),
  APPLY("apply"),
  OP_ADD("+"),
  OP_SUBTRACT("-"),
  OP_MULT("*"),
  OP_DIVIDE("/"),
  OP_REMAINDER("%");

  private final String grammarName;

  private Function(String grammarName) {
    this.grammarName = grammarName;
  }

  public String getGrammarName() {
    return grammarName;
  }

}
