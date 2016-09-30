/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import it.units.malelab.ege.Node;
import it.units.malelab.ege.Pair;
import it.units.malelab.ege.Utils;
import it.units.malelab.ege.evolver.genotype.SGEGenotype;
import it.units.malelab.ege.grammar.Grammar;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class SGEMapper<T> extends AbstractMapper<SGEGenotype<T>, T> {

  private final Grammar<Pair<T, Integer>> nonRecursiveGrammar;
  private final Map<Pair<T, Integer>, List<Integer>> genesBound;

  public SGEMapper(int maxDepth, Grammar<T> grammar) {
    super(grammar);
    nonRecursiveGrammar = Utils.resolveRecursiveGrammar(grammar, maxDepth);
    genesBound = new LinkedHashMap<>();
    for (Map.Entry<Pair<T, Integer>, List<List<Pair<T, Integer>>>> entry : nonRecursiveGrammar.getRules().entrySet()) {
      List<Integer> bounds = new ArrayList<>();
      for (int i = 0; i<maximumExpansions(entry.getKey(), nonRecursiveGrammar); i++) {
        bounds.add(entry.getValue().size());
      }
      genesBound.put(entry.getKey(), bounds);
    }
  }

  public Node<T> map(SGEGenotype<T> genotype) throws MappingException {
    //map
    Multiset<Pair<T, Integer>> expandedSymbols = LinkedHashMultiset.create();
    Node<Pair<T, Integer>> tree = new Node<>(nonRecursiveGrammar.getStartingSymbol());
    while (true) {
      Node<Pair<T, Integer>> nodeToBeReplaced = null;
      for (Node<Pair<T, Integer>> node : tree.leaves()) {
        if (nonRecursiveGrammar.getRules().keySet().contains(node.getContent())) {
          nodeToBeReplaced = node;
          break;
        }
      }
      if (nodeToBeReplaced == null) {
        break;
      }
      //get codon
      List<Integer> values = genotype.getGenes().get(nodeToBeReplaced.getContent());
      int value = values.get(expandedSymbols.count(nodeToBeReplaced.getContent()) % values.size());
      List<List<Pair<T, Integer>>> options = nonRecursiveGrammar.getRules().get(nodeToBeReplaced.getContent());
      int optionIndex = value % options.size();
      //add children
      for (Pair<T, Integer> symbol : options.get(optionIndex)) {
        Node<Pair<T, Integer>> newChild = new Node<>(symbol);
        nodeToBeReplaced.getChildren().add(newChild);
      }
      expandedSymbols.add(nodeToBeReplaced.getContent());
    }
    return transform(tree);
  }
  
  private Node<T> transform(Node<Pair<T, Integer>> pairNode) {
    Node<T> node = new Node<>(pairNode.getContent().getFirst());
    for (Node<Pair<T, Integer>> pairChild : pairNode.getChildren()) {
      node.getChildren().add(transform(pairChild));
    }
    return node;
  }

  private <E> int maximumExpansions(E nonTerminal, Grammar<E> g) {
    //assume non recursive grammar
    if (nonTerminal.equals(g.getStartingSymbol())) {
      return 1;
    }
    int count = 0;
    for (Map.Entry<E, List<List<E>>> rule : g.getRules().entrySet()) {
      int maxCount = Integer.MIN_VALUE;
      for (List<E> option : rule.getValue()) {
        int optionCount = 0;
        for (E optionSymbol : option) {
          if (optionSymbol.equals(nonTerminal)) {
            optionCount = optionCount + 1;
          }
        }
        maxCount = Math.max(maxCount, optionCount);
      }
      if (maxCount > 0) {
        count = count + maxCount * maximumExpansions(rule.getKey(), g);
      }
    }
    return count;
  }

  public Map<Pair<T, Integer>, List<Integer>> getGenesBound() {
    return genesBound;
  }

}
