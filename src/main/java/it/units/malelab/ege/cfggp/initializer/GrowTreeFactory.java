/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.cfggp.initializer;

import it.units.malelab.ege.core.Factory;
import it.units.malelab.ege.core.Grammar;
import it.units.malelab.ege.core.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class GrowTreeFactory<T> implements Factory<Node<T>> {

  private final int maxDepth;
  protected final Grammar<T> grammar;

  public GrowTreeFactory(int maxDepth, Grammar<T> grammar) {
    this.maxDepth = maxDepth;
    this.grammar = grammar;
  }

  @Override
  public Node<T> build(Random random) {
    Node<T> tree = null;
    while (tree == null) {
      tree = build(random, grammar.getStartingSymbol(), maxDepth, 0);
    }
    return tree;
  }

  public Node<T> build(Random random, T symbol, int maxDepth, int depth) {
    if (depth==maxDepth) {
      return null;
    }
    Node<T> tree = new Node<>(symbol);
    if (grammar.getRules().containsKey(symbol)) {
      List<List<T>> options = grammar.getRules().get(symbol);
      List<List<T>> availableOptions;
      if (depth < maxDepth - 1) {
        availableOptions = options;
      } else {
        availableOptions = new ArrayList<>();
        for (List<T> option : options) {
          boolean allTerminals = true;
          for (T optionSymbol : option) {
            if (grammar.getRules().containsKey(optionSymbol)) {
              allTerminals = false;
              break;
            }
          }
          if (allTerminals) {
            availableOptions.add(option);
          }
        }
      }
      if (availableOptions.isEmpty()) {
        return null;
      }
      int optionIndex = random.nextInt(availableOptions.size());
      for (T childSymbol : availableOptions.get(optionIndex)) {
        Node<T> child = build(random, childSymbol, maxDepth, depth + 1);
        if (child == null) {
          return null;
        }
        tree.getChildren().add(child);
      }
    }
    return tree;
  }

}
