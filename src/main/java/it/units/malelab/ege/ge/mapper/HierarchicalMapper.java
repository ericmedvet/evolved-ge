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
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import static it.units.malelab.ege.ge.mapper.StandardGEMapper.BIT_USAGES_INDEX_NAME;
import it.units.malelab.ege.util.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author eric
 */
public class HierarchicalMapper<T> extends AbstractMapper<BitsGenotype, T> {

  private final static boolean DEBUG = false;
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
    if (DEBUG) {
      System.out.printf("Mapping %s%n", genotype);
    }
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

  protected List<Range<Integer>> getSlices(Range<Integer> range, List<T> symbols) {
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

  private Node<T> extractFromEnhanced(Node<EnhancedSymbol<T>> enhancedNode) {
    Node<T> node = new Node<>(enhancedNode.getContent().getSymbol());
    for (Node<EnhancedSymbol<T>> enhancedChild : enhancedNode.getChildren()) {
      node.getChildren().add(extractFromEnhanced(enhancedChild));
    }
    return node;
  }

  private <K> K chooseOption(BitsGenotype genotype, Range<Integer> range, List<K> options) {
    if (options.size() == 1) {
      return options.get(0);
    }
    double max = Double.NEGATIVE_INFINITY;
    List<BitsGenotype> slices = genotype.slices(Utils.slices(range, options.size()));
    if (DEBUG) {
      System.out.printf("\tOPTION:");
      for (BitsGenotype slice : slices) {
        System.out.printf(" %s", slice);
      }
    }
    List<Integer> bestOptionIndexes = new ArrayList<>();
    for (int i = 0; i < options.size(); i++) {
      double value = (double) slices.get(i).count() / (double) slices.get(i).size();
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
      if (DEBUG) {
        System.out.printf(" (%d)%n", bestOptionIndexes.get(0));
        System.out.print("$");
        int boi = bestOptionIndexes.get(0);
        for (int si = 0; si < slices.size(); si++) {
          System.out.printf("%s\\gft{%s}%s%s",
                  si == boi ? "\\textbf{" : "",
                  slices.get(si).toString().split(":")[1],
                  si == boi ? "}" : "",
                  si == slices.size() - 1 ? "" : "\\: "
          );
        }
        System.out.println("$\\\\");
      }
      return options.get(bestOptionIndexes.get(0));
    }
    if (DEBUG) {
      System.out.printf(" (%d on %d)%n",
              bestOptionIndexes.get(genotype.slice(range).count() % bestOptionIndexes.size()),
              bestOptionIndexes.size()
      );
      System.out.print("$");
      int boi = bestOptionIndexes.get(genotype.slice(range).count() % bestOptionIndexes.size());
      for (int si = 0; si < slices.size(); si++) {
        System.out.printf("%s\\gft{%s}%s%s",
                si == boi ? "\\textbf{" : "",
                slices.get(si).toString().split(":")[1],
                si == boi ? "}" : "",
                si == slices.size() - 1 ? "" : "\\: "
        );
      }
      System.out.println("$\\\\");
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
      if (DEBUG) {
        System.out.printf("%s%n", symbol);
      }
      //get option
      List<T> symbols;
      if ((symbolRange.upperEndpoint() - symbolRange.lowerEndpoint()) < options.size()) {
        int count = (symbolRange.upperEndpoint() - symbolRange.lowerEndpoint() > 0) ? genotype.slice(symbolRange).count() : genotype.count();
        int index = shortestOptionIndexesMap.get(symbol).get(count % shortestOptionIndexesMap.get(symbol).size());
        symbols = options.get(index);
        if (DEBUG) {
          System.out.printf("\tOPTION: (count=%d on %d)%n", count, shortestOptionIndexesMap.get(symbol).size());
        }
      } else {
        symbols = chooseOption(genotype, symbolRange, options);
        for (int i = symbolRange.lowerEndpoint(); i < symbolRange.upperEndpoint(); i++) {
          bitUsages[i] = bitUsages[i] + 1;
        }
      }
      //add children
      List<Range<Integer>> childRanges = getSlices(symbolRange, symbols);
      if (DEBUG) {
        System.out.print("\tSPLIT :");
      }
      for (int i = 0; i < symbols.size(); i++) {
        Range<Integer> childRange = childRanges.get(i);
        if (childRanges.get(i).equals(symbolRange) && (childRange.upperEndpoint() - childRange.lowerEndpoint() > 0)) {
          childRange = Range.closedOpen(symbolRange.lowerEndpoint(), symbolRange.upperEndpoint() - 1);
        }
        if (DEBUG) {
          System.out.printf(" %s", genotype.slice(childRange));
          if (childRanges.get(i).equals(symbolRange) && (childRange.upperEndpoint() - childRange.lowerEndpoint() > 0)) {
            System.out.print("*");
          }
        }
        Node<EnhancedSymbol<T>> newChild = new Node<>(new EnhancedSymbol<>(
                symbols.get(i),
                childRange
        ));
        nodeToBeReplaced.getChildren().add(newChild);
      }
      if (DEBUG) {
        System.out.println();
      }
    }
    //convert
    return extractFromEnhanced(enhancedTree);
  }

  public Node<T> mapRecursively(T symbol, Range<Integer> range, BitsGenotype genotype, int[] bitUsages) throws MappingException {
    Node<T> node = new Node<>(symbol);
    if (DEBUG) {
      System.out.printf("%s with %s%n", symbol, genotype.slice(range));
    }
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
        if (DEBUG) {
          System.out.printf("\tOPTION: (count=%d on %d)%n", count, shortestOptionIndexesMap.get(symbol).size());
          System.out.printf("$\\gft{%s}$\\\\%n",
                  genotype.slice(range).toString().split(":")[1]
          );
        }
      } else {
        symbols = chooseOption(genotype, range, options);
        for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
          bitUsages[i] = bitUsages[i] + 1;
        }
      }
      //add children
      List<Range<Integer>> childRanges = getSlices(range, symbols);
      if (DEBUG && false) {
        System.out.print("\tSPLIT :");
      }
      for (int i = 0; i < symbols.size(); i++) {
        Range<Integer> childRange = childRanges.get(i);
        if (childRanges.get(i).equals(range) && (childRange.upperEndpoint() - childRange.lowerEndpoint() > 0)) {
          childRange = Range.closedOpen(range.lowerEndpoint(), range.upperEndpoint() - 1);
          childRanges.set(i, childRange);
        }
        if (DEBUG && false) {
          System.out.printf(" %s", genotype.slice(childRange));
          if (childRanges.get(i).equals(range) && (childRange.upperEndpoint() - childRange.lowerEndpoint() > 0)) {
            System.out.print("*");
          }
        }
      }
      if (DEBUG && false) {
        System.out.println();
        System.out.print("$");
        for (int ci = 0; ci < childRanges.size(); ci++) {
          if (childRanges.get(ci).upperEndpoint() - childRanges.get(ci).lowerEndpoint() == 0) {
            System.out.printf("\\emptyset%s",
                    ci == childRanges.size() - 1 ? "" : "\\: "
            );
          } else {
            System.out.printf("\\gft{%s}%s",
                    genotype.slice(childRanges.get(ci)).toString().split(":")[1],
                    ci == childRanges.size() - 1 ? "" : "\\: "
            );
          }
        }
        System.out.println("$");
      }
      for (int i = 0; i < symbols.size(); i++) {
        node.getChildren().add(mapRecursively(symbols.get(i), childRanges.get(i), genotype, bitUsages));
      }
    }
    return node;
  }

  public static void main(String[] args) throws IOException {
    Grammar grammar = Utils.parseFromFile(new File("grammars/symbolic-regression-xy-nums.bnf"));
    Mapper hge = new HierarchicalMapper(grammar);
    Mapper whge = new WeightedHierarchicalMapper(2, grammar);
    Mapper ge = new StandardGEMapper(8, 10, grammar);
    Mapper pige = new PiGEMapper(16, 10, grammar);
    BitsGenotypeFactory bgf = new BitsGenotypeFactory(48);
    Random r = new Random(1);
    Map<String, Object> report = new HashMap<>();
    while (true) {
      BitsGenotype g = bgf.build(r);
      g = new BitsGenotype("111001111111000010100001011100010100110100000111");
      try {
        List p1 = Utils.contents(hge.map(g, report).leafNodes());
        List p2 = Utils.contents(whge.map(g, report).leafNodes());
        List p3 = Utils.contents(ge.map(g, report).leafNodes());
        List p4 = Utils.contents(pige.map(g, report).leafNodes());
        //System.out.printf("%3d %3d %3d %3d%n", p1.size(), p2.size(), p3.size(), p4.size());
        if (p1.size() == 9 && p2.size() == 13 && p3.size() >= 9 && p4.size() >= 9) {
          System.out.printf("%s%n%s%n%s%n%s%n%s%n", g, p1, p2, p3, p4);
          break;
        }
      } catch (MappingException e) {
        continue;
      }
    }
  }

}
