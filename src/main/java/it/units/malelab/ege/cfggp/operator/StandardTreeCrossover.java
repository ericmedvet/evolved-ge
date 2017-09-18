/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.cfggp.operator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.operator.AbstractCrossover;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class StandardTreeCrossover<T> extends AbstractCrossover<Node<T>> {

  private final int maxDepth;

  public StandardTreeCrossover(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  @Override
  public List<Node<T>> apply(List<Node<T>> parents, Random random) {
    //build maps of leaf-subtrees
    Node<T> child1 = new Node<>(parents.get(0));
    Node<T> child2 = new Node<>(parents.get(1));
    child1.propagateParentship();
    child2.propagateParentship();
    Multimap<T, Node<T>> child1subtrees = ArrayListMultimap.create();
    Multimap<T, Node<T>> child2subtrees = ArrayListMultimap.create();
    populateMultimap(child1, child1subtrees);
    populateMultimap(child2, child2subtrees);
    //build common non-terminals
    List<T> nonTerminals = new ArrayList<>();
    nonTerminals.addAll(child1subtrees.keySet());
    nonTerminals.retainAll(child2subtrees.keySet());
    if (nonTerminals.isEmpty()) {
      return null;
    }
    Collections.shuffle(nonTerminals, random);
    //iterate (just once, if successfully) on non-terminals
    boolean done = false;
    for (T chosenNonTerminal : nonTerminals) {
      List<Node<T>> subtrees1 = new ArrayList<>(child1subtrees.get(chosenNonTerminal));
      List<Node<T>> subtrees2 = new ArrayList<>(child2subtrees.get(chosenNonTerminal));
      Collections.shuffle(subtrees1, random);
      Collections.shuffle(subtrees2, random);
      for (Node<T> subtree1 : subtrees1) {
        for (Node<T> subtree2 : subtrees2) {
          if ((subtree1.getAncestors().size() + subtree2.depth() <= maxDepth) && (subtree2.getAncestors().size() + subtree1.depth() <= maxDepth)) {
            List<Node<T>> swappingChildren = new ArrayList<>(subtree1.getChildren());
            subtree1.getChildren().clear();
            subtree1.getChildren().addAll(subtree2.getChildren());
            subtree2.getChildren().clear();
            subtree2.getChildren().addAll(swappingChildren);
            done = true;
            break;
          }
        }
        if (done) {
          break;
        }
      }
      if (done) {
        break;
      }
    }
    if (!done) {
      return null;
    }
    //return
    List<Node<T>> children = new ArrayList<>(2);
    children.add(child1);
    children.add(child2);
    return children;
  }

  private void populateMultimap(Node<T> node, Multimap<T, Node<T>> multimap) {
    if (node.getChildren().isEmpty()) {
      return;
    }
    multimap.put(node.getContent(), node);
    for (Node<T> child : node.getChildren()) {
      populateMultimap(child, multimap);
    }

  }

}
