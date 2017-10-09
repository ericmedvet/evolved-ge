/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.cfggp.initializer;

import it.units.malelab.ege.core.Grammar;
import it.units.malelab.ege.core.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class FullTreeFactory<T> extends GrowTreeFactory<T> {

  public FullTreeFactory(int maxDepth, Grammar<T> grammar) {
    super(maxDepth, grammar);
  }

  @Override
  public Node<T> build(Random random, T symbol, int maxDepth, int depth) {
    if (depth == maxDepth) {
      return null;
    }
    Node<T> tree = new Node<>(symbol);
    if (grammar.getRules().containsKey(symbol)) {
      List<List<T>> options = grammar.getRules().get(symbol);
      List<List<T>> availableOptions;
      if (depth < maxDepth - 1) {
        availableOptions = new ArrayList<>();
        for (List<T> option : options) {
          boolean hasTerminal = false;
          for (T optionSymbol : option) {
            if (!grammar.getRules().containsKey(optionSymbol)) {
              hasTerminal = true;
              break;
            }
          }
          if (!hasTerminal) {
            availableOptions.add(option);
          }
        }
        if (availableOptions.isEmpty()) {
          //if no available options, use all options: the reason is that in many
          //cases, there are decoration symbols in the grammar making the
          //non-terminal-only case very rare
          availableOptions.addAll(options);
        }
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
        if (availableOptions.isEmpty()) {
          //here all options are too deep
          return null;
        }
      }
      int optionIndex = random.nextInt(availableOptions.size());
      for (T childSymbol : availableOptions.get(optionIndex)) {
        Node<T> child;
        if (grammar.getRules().containsKey(childSymbol)) {
          child = build(random, childSymbol, maxDepth, depth + 1);
          if (child == null) {
            return null;
          }
        } else {
          child = new Node(childSymbol);
        }
        tree.getChildren().add(child);
      }
    }
    return tree;
  }

}
