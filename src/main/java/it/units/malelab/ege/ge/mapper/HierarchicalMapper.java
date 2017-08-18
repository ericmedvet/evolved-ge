/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.mapper;

import it.units.malelab.ege.core.mapper.AbstractMapper;
import it.units.malelab.ege.core.mapper.MappingException;
import com.google.common.collect.Range;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Grammar;
import static it.units.malelab.ege.ge.mapper.StandardGEMapper.BIT_USAGES_INDEX_NAME;
import it.units.malelab.ege.util.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class HierarchicalMapper<T> extends AbstractMapper<BitsGenotype, T> {

  private final static boolean RECURSIVE_DEFAULT = false;

  private final boolean recursive;
  private final Map<T, List<Integer>> shortestOptionIndexesMap;

  public HierarchicalMapper(Grammar<T> grammar) {
    this(grammar, RECURSIVE_DEFAULT);
  }

  public HierarchicalMapper(Grammar<T> grammar, boolean recursive) {
    super(grammar);
    this.recursive = recursive;
    Map<T, List<Integer>> optionJumpsToTerminalMap = new LinkedHashMap<>();
    for (Map.Entry<T, List<List<T>>> rule : grammar.getRules().entrySet()) {
      List<Integer> optionsJumps = new ArrayList<>();
      for (List<T> option : rule.getValue()) {
        optionsJumps.add(Integer.MAX_VALUE);
      }
      optionJumpsToTerminalMap.put(rule.getKey(), optionsJumps);
    }
    while (true) {
      boolean completed = true;
      for (Map.Entry<T, List<Integer>> entry : optionJumpsToTerminalMap.entrySet()) {
        for (int i = 0; i < entry.getValue().size(); i++) {
          List<T> option = grammar.getRules().get(entry.getKey()).get(i);
          if (Collections.disjoint(option, grammar.getRules().keySet())) {
            entry.getValue().set(i, 1);
          } else {
            int maxJumps = Integer.MIN_VALUE;
            for (T optionSymbol : option) {
              List<Integer> optionSymbolJumps = optionJumpsToTerminalMap.get(optionSymbol);
              if (optionSymbolJumps == null) {
                maxJumps = Math.max(0, maxJumps);
              } else {
                int minJumps = Integer.MAX_VALUE;
                for (int jumps : optionSymbolJumps) {
                  minJumps = Math.min(minJumps, jumps);
                }
                minJumps = (minJumps == Integer.MAX_VALUE) ? minJumps : (minJumps + 1);
                maxJumps = Math.max(minJumps, maxJumps);
              }
            }
            entry.getValue().set(i, maxJumps);
            if (maxJumps == Integer.MAX_VALUE) {
              completed = false;
            }
          }
        }
      }
      if (completed) {
        break;
      }
    }
    //build shortestOptionIndexMap
    shortestOptionIndexesMap = new LinkedHashMap<>();
    for (Map.Entry<T, List<List<T>>> rule : grammar.getRules().entrySet()) {
      int minJumps = Integer.MAX_VALUE;
      for (int i = 0; i < optionJumpsToTerminalMap.get(rule.getKey()).size(); i++) {
        int localJumps = optionJumpsToTerminalMap.get(rule.getKey()).get(i);
        if (localJumps < minJumps) {
          minJumps = localJumps;
        }
      }
      List<Integer> indexes = new ArrayList<>();
      for (int i = 0; i < optionJumpsToTerminalMap.get(rule.getKey()).size(); i++) {
        if (optionJumpsToTerminalMap.get(rule.getKey()).get(i) == minJumps) {
          indexes.add(i);
        }
      }
      shortestOptionIndexesMap.put(rule.getKey(), indexes);
    }
  }

  private class EnhancedSymbol<T> {

    private final T symbol;
    private final Range<Integer> range;

    public EnhancedSymbol(T symbol, Range<Integer> range) {
      this.symbol = symbol;
      this.range = range;
    }

    public T getSymbol() {
      return symbol;
    }

    public Range<Integer> getRange() {
      return range;
    }

  }

  @Override
  public Node<T> map(BitsGenotype genotype, Map<String, Object> report) throws MappingException {
    int[] bitUsages = new int[genotype.size()];
    Node<T> tree;
    if (recursive) {
      tree = mapRecursively(grammar.getStartingSymbol(), Range.closedOpen(0, genotype.size()), genotype, bitUsages);
    } else {
      tree = mapIteratively(genotype, bitUsages);
    }
    report.put(BIT_USAGES_INDEX_NAME, bitUsages);
    //convert
    return tree;
  }

  protected List<Range<Integer>> getChildrenSlices(Range<Integer> range, List<T> symbols) {
    List<Range<Integer>> ranges;
    if (symbols.size() > (range.upperEndpoint() - range.lowerEndpoint())) {
      ranges = new ArrayList<>(symbols.size());
      for (T symbol : symbols) {
        ranges.add(Range.closedOpen(range.lowerEndpoint(), range.lowerEndpoint()));
      }
    } else {
      ranges = Utils.slices(range, symbols.size());
    }
    return ranges;
  }

  protected List<Range<Integer>> getOptionSlices(Range<Integer> range, List<List<T>> options) {
    return Utils.slices(range, options.size());
  }

  private Node<T> extractFromEnhanced(Node<EnhancedSymbol<T>> enhancedNode) {
    Node<T> node = new Node<>(enhancedNode.getContent().getSymbol());
    for (Node<EnhancedSymbol<T>> enhancedChild : enhancedNode.getChildren()) {
      node.getChildren().add(extractFromEnhanced(enhancedChild));
    }
    return node;
  }
  
  protected double optionSliceWeigth(BitsGenotype slice) {
    return (double) slice.count() / (double) slice.size();
  }

  private List<T> chooseOption(BitsGenotype genotype, Range<Integer> range, List<List<T>> options) {
    if (options.size() == 1) {
      return options.get(0);
    }
    double max = Double.NEGATIVE_INFINITY;
    List<BitsGenotype> slices = genotype.slices(getOptionSlices(range, options));
    List<Integer> bestOptionIndexes = new ArrayList<>();
    for (int i = 0; i < options.size(); i++) {
      double value = optionSliceWeigth(slices.get(i));
      if (value == max) {
        bestOptionIndexes.add(i);
      } else if (value > max) {
        max = value;
        bestOptionIndexes.clear();
        bestOptionIndexes.add(i);
      }
    }
    //for avoiding choosing always the 1st option in case of tie, choose depending on count of 1s in genotype
    if (bestOptionIndexes.size() == 1) {
      return options.get(bestOptionIndexes.get(0));
    }
    return options.get(bestOptionIndexes.get(genotype.slice(range).count() % bestOptionIndexes.size()));
  }

  public Node<T> mapIteratively(BitsGenotype genotype, int[] bitUsages) throws MappingException {
    Node<EnhancedSymbol<T>> enhancedTree = new Node<>(new EnhancedSymbol<>(grammar.getStartingSymbol(), Range.closedOpen(0, genotype.size())));
    while (true) {
      Node<EnhancedSymbol<T>> nodeToBeReplaced = null;
      for (Node<EnhancedSymbol<T>> node : enhancedTree.leafNodes()) {
        if (grammar.getRules().keySet().contains(node.getContent().getSymbol())) {
          nodeToBeReplaced = node;
          break;
        }
      }
      if (nodeToBeReplaced == null) {
        break;
      }
      //get genotype
      T symbol = nodeToBeReplaced.getContent().getSymbol();
      Range<Integer> symbolRange = nodeToBeReplaced.getContent().getRange();
      List<List<T>> options = grammar.getRules().get(symbol);
      //get option
      List<T> symbols;
      if ((symbolRange.upperEndpoint() - symbolRange.lowerEndpoint()) < options.size()) {
        int count = (symbolRange.upperEndpoint() - symbolRange.lowerEndpoint() > 0) ? genotype.slice(symbolRange).count() : genotype.count();
        int index = shortestOptionIndexesMap.get(symbol).get(count % shortestOptionIndexesMap.get(symbol).size());
        symbols = options.get(index);
      } else {
        symbols = chooseOption(genotype, symbolRange, options);
        for (int i = symbolRange.lowerEndpoint(); i < symbolRange.upperEndpoint(); i++) {
          bitUsages[i] = bitUsages[i] + 1;
        }
      }
      //add children
      List<Range<Integer>> childRanges = getChildrenSlices(symbolRange, symbols);
      for (int i = 0; i < symbols.size(); i++) {
        Range<Integer> childRange = childRanges.get(i);
        if (childRanges.get(i).equals(symbolRange) && (childRange.upperEndpoint() - childRange.lowerEndpoint() > 0)) {
          childRange = Range.closedOpen(symbolRange.lowerEndpoint(), symbolRange.upperEndpoint() - 1);
        }
        Node<EnhancedSymbol<T>> newChild = new Node<>(new EnhancedSymbol<>(
                symbols.get(i),
                childRange
        ));
        nodeToBeReplaced.getChildren().add(newChild);
      }
    }
    //convert
    return extractFromEnhanced(enhancedTree);
  }

  public Node<T> mapRecursively(T symbol, Range<Integer> range, BitsGenotype genotype, int[] bitUsages) throws MappingException {
    Node<T> node = new Node<>(symbol);
    if (grammar.getRules().keySet().contains(symbol)) {
      //a non-terminal node
      //update usage
      for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
        bitUsages[i] = bitUsages[i] + 1;
      }
      List<List<T>> options = grammar.getRules().get(symbol);
      //get option
      List<T> symbols;
      if ((range.upperEndpoint() - range.lowerEndpoint()) < options.size()) {
        int count = (range.upperEndpoint() - range.lowerEndpoint() > 0) ? genotype.slice(range).count() : genotype.count();
        int index = shortestOptionIndexesMap.get(symbol).get(count % shortestOptionIndexesMap.get(symbol).size());
        symbols = options.get(index);
      } else {
        symbols = chooseOption(genotype, range, options);
        for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
          bitUsages[i] = bitUsages[i] + 1;
        }
      }
      //add children
      List<Range<Integer>> childRanges = getChildrenSlices(range, symbols);
      for (int i = 0; i < symbols.size(); i++) {
        Range<Integer> childRange = childRanges.get(i);
        if (childRanges.get(i).equals(range) && (childRange.upperEndpoint() - childRange.lowerEndpoint() > 0)) {
          childRange = Range.closedOpen(range.lowerEndpoint(), range.upperEndpoint() - 1);
          childRanges.set(i, childRange);
        }
      }
      for (int i = 0; i < symbols.size(); i++) {
        node.getChildren().add(mapRecursively(symbols.get(i), childRanges.get(i), genotype, bitUsages));
      }
    }
    return node;
  }

}
