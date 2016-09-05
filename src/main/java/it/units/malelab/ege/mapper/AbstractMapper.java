/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import it.units.malelab.ege.grammar.Grammar;

/**
 *
 * @author eric
 */
public abstract class AbstractMapper implements Mapper {

  protected final Grammar grammar;

  public AbstractMapper(Grammar grammar) {
    this.grammar = grammar;
  }

  public Grammar getGrammar() {
    return grammar;
  }
  
}
