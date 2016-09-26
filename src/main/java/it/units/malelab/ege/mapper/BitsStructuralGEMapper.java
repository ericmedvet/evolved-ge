/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Range;
import it.units.malelab.ege.BitsGenotype;
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
public class BitsStructuralGEMapper<T> extends AbstractMapper<BitsGenotype, T> {

  private final Grammar<Pair<T, Integer>> nonRecursiveGrammar;
  private final List<Pair<T, Integer>> nonTerminals;
  private final List<Integer> nonTerminalSizes;
  private final List<Integer> nonTerminalCodonsNumbers;
  private int overallSize;

  public BitsStructuralGEMapper(int maxDepth, Grammar<T> grammar) {
    super(grammar);
    nonRecursiveGrammar = Utils.resolveRecursiveGrammar(grammar, maxDepth);
    Map<Pair<T, Integer>, Range<Integer>> codonsRangesMap = new LinkedHashMap<>();
    int startingIndex = 0;
    for (Pair<T, Integer> p : nonRecursiveGrammar.getRules().keySet()) {
      int maximumExpansions = maximumExpansions(p, nonRecursiveGrammar);
      codonsRangesMap.put(p, Range.closedOpen(startingIndex, startingIndex + maximumExpansions));
      startingIndex = startingIndex + maximumExpansions;
    }
    nonTerminals = new ArrayList<>(codonsRangesMap.keySet());
    nonTerminalSizes = new ArrayList<>();
    nonTerminalCodonsNumbers = new ArrayList<>();
    overallSize = 0;
    for (Pair<T, Integer> nonTerminal : nonTerminals) {
      Range<Integer> range = codonsRangesMap.get(nonTerminal);
      int nonTerminalCodonsNumber = range.upperEndpoint() - range.lowerEndpoint();
      int codonSize = (int) Math.max(Math.ceil(Math.log10(nonRecursiveGrammar.getRules().get(nonTerminal).size()) / Math.log10(2)), 1);
      nonTerminalSizes.add(nonTerminalCodonsNumber * codonSize);
      nonTerminalCodonsNumbers.add(nonTerminalCodonsNumber);
      overallSize = overallSize + nonTerminalCodonsNumber * codonSize;
    }
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
  public Node<T> map(BitsGenotype genotype) throws MappingException {
    //transform genotypes in ints
    if (genotype.size() < overallSize) {
      throw new MappingException(String.format("Short genotype (%d<%d)", genotype.size(), overallSize));
    }
    Map<Pair<T, Integer>, List<Integer>> codons = new LinkedHashMap<>();
    List<BitsGenotype> nonTerminalGenotypes = genotype.slices(nonTerminalSizes);
    for (int i = 0; i < nonTerminals.size(); i++) {
      int codonSize = (int) Math.max(Math.ceil(Math.log10(nonRecursiveGrammar.getRules().get(nonTerminals.get(i)).size()) / Math.log10(2)), 1);
      List<BitsGenotype> codonGenotypes = nonTerminalGenotypes.get(i).slices(nonTerminalCodonsNumbers.get(i));
      List<Integer> nonTerminalCodons = new ArrayList<>(nonTerminalCodonsNumbers.get(i));
      for (int j = 0; j < nonTerminalCodonsNumbers.get(i); j++) {
        nonTerminalCodons.add(codonGenotypes.get(j).compress(codonSize).toInt());
      }
      codons.put(nonTerminals.get(i), nonTerminalCodons);
    }
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
      int codonValue = codons.get(nodeToBeReplaced.getContent()).get(expandedSymbols.count(nodeToBeReplaced.getContent()));
      List<List<Pair<T, Integer>>> options = nonRecursiveGrammar.getRules().get(nodeToBeReplaced.getContent());
      int optionIndex = codonValue % options.size();
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

  public List<Integer> getNonTerminalSizes() {
    return nonTerminalSizes;
  }

}
