/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Range;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.grammar.Node;
import it.units.malelab.ege.util.Pair;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.grammar.Grammar;
import static it.units.malelab.ege.mapper.StandardGEMapper.BIT_USAGES_INDEX_NAME;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class BitsSGEMapper<T> extends AbstractMapper<BitsGenotype, T> {

  private final Grammar<Pair<T, Integer>> nonRecursiveGrammar;
  private final List<Pair<T, Integer>> nonTerminals;
  private final List<Integer> nonTerminalSizes;
  private final List<Integer> nonTerminalCodonsNumbers;
  private int overallSize;
  private int maxDepth;

  public BitsSGEMapper(int maxDepth, Grammar<T> grammar) {
    super(grammar);
    this.maxDepth = maxDepth;
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
  public Node<T> map(BitsGenotype genotype, Map<String, Object> report) throws MappingException {
    int[] bitUsages = new int[genotype.size()];
    //transform genotypes in ints
    if (genotype.size() < overallSize) {
      throw new MappingException(String.format("Short genotype (%d<%d)", genotype.size(), overallSize));
    }
    Map<Pair<T, Integer>, List<Range<Integer>>> codonRanges = new LinkedHashMap<>();
    List<Range<Integer>> nonTerminalRanges = Utils.slices(Range.closedOpen(0, genotype.size()), nonTerminalSizes);
    for (int i = 0; i < nonTerminals.size(); i++) {
      //int codonSize = (int) Math.max(Math.ceil(Math.log10(nonRecursiveGrammar.getRules().get(nonTerminals.get(i)).size()) / Math.log10(2)), 1);
      List<Range<Integer>> boundaries = Utils.slices(nonTerminalRanges.get(i), nonTerminalCodonsNumbers.get(i));
      codonRanges.put(nonTerminals.get(i), boundaries);
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
      Range<Integer> range = codonRanges.get(nodeToBeReplaced.getContent()).get(expandedSymbols.count(nodeToBeReplaced.getContent()));
      List<List<Pair<T, Integer>>> options = nonRecursiveGrammar.getRules().get(nodeToBeReplaced.getContent());
      int codonSize = (int) Math.max(Math.ceil(Math.log10(options.size()) / Math.log10(2)), 1);
      int codonValue = genotype.slice(range).compress(codonSize).toInt();
      int optionIndex = codonValue % options.size();
      //update usages
      for (int i = range.lowerEndpoint(); i<range.upperEndpoint(); i++) {
        bitUsages[i] = bitUsages[i]+1;
      }
      //add children
      for (Pair<T, Integer> p : options.get(optionIndex)) {
        Node<Pair<T, Integer>> newChild = new Node<>(p);
        nodeToBeReplaced.getChildren().add(newChild);
      }
      expandedSymbols.add(nodeToBeReplaced.getContent());
    }
    report.put(BIT_USAGES_INDEX_NAME, bitUsages);
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

  @Override
  public String toString() {
    return "BitsSGEMapper{" + "maxDepth=" + maxDepth + '}';
  }

}
