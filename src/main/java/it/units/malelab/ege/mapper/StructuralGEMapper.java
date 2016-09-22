/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Range;
import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.Node;
import it.units.malelab.ege.Pair;
import it.units.malelab.ege.Utils;
import it.units.malelab.ege.grammar.Grammar;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class StructuralGEMapper<T> extends AbstractMapper<T> {

  private final Map<Pair<T, Integer>, Range<Integer>> codonsRangesMap;
  private final Grammar<Pair<T, Integer>> nonRecursiveGrammar;
  private final int numberOfCodons;

  public StructuralGEMapper(int maxDepth, Grammar<T> grammar) {
    super(grammar);
    nonRecursiveGrammar = Utils.resolveRecursiveGrammar(grammar, maxDepth);
    codonsRangesMap = new LinkedHashMap<>();
    int startingIndex = 0;
    for (Pair<T, Integer> p : nonRecursiveGrammar.getRules().keySet()) {
      int maximumExpansions = maximumExpansions(p, nonRecursiveGrammar);
      codonsRangesMap.put(p, Range.closedOpen(startingIndex, startingIndex + maximumExpansions));
      startingIndex = startingIndex + maximumExpansions;
    }
    numberOfCodons = startingIndex + 1;
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

  @Override
  public Node<T> map(Genotype genotype) throws MappingException {
    if (genotype.size() < numberOfCodons) {
      throw new MappingException(String.format("Short genotype (%d<%d)", genotype.size(), numberOfCodons));
    }
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
      int codonIndex = codonsRangesMap.get(nodeToBeReplaced.getContent()).lowerEndpoint() + expandedSymbols.count(nodeToBeReplaced.getContent());
      int codonValue = genotype.getIndexedEqualSlice(codonIndex, numberOfCodons).toInt();
      List<List<Pair<T, Integer>>> options = nonRecursiveGrammar.getRules().get(nodeToBeReplaced.getContent());
      int optionIndex = codonValue%options.size();
      //add children
      for (Pair<T, Integer> p : options.get(optionIndex)) {
        Node<Pair<T, Integer>> newChild = new Node<>(p);
        nodeToBeReplaced.getChildren().add(newChild);
      }
      expandedSymbols.add(nodeToBeReplaced.getContent());
    }
    //transform tree
    return transform(tree);
  }
  
  private Node<T> transform(Node<Pair<T, Integer>> pairNode) {
    Node<T> node = new Node<>(pairNode.getContent().getFirst());
    for (Node<Pair<T, Integer>> pairChild : pairNode.getChildren()) {
      node.getChildren().add(transform(pairChild));
    }
    return node;
  }
  
  public List<Range<Integer>> getCodonsRanges() {
    List<Range<Integer>> ranges = new ArrayList<>();
    ranges.addAll(codonsRangesMap.values());
    return ranges;
  }

}
