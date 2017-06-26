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

  private final static boolean DEBUG = true;
  private final static boolean RECURSIVE_DEFAULT = false;
    
  private final boolean recursive;
  private final Map<T, Integer> shortestOptionIndexMap;

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
    shortestOptionIndexMap = new LinkedHashMap<>();
    for (Map.Entry<T, List<List<T>>> rule : grammar.getRules().entrySet()) {
      int minJumpsOptionIndex = 0;
      int minJumps = Integer.MAX_VALUE;
      for (int i = 0; i < optionJumpsToTerminalMap.get(rule.getKey()).size(); i++) {
        int jumps = optionJumpsToTerminalMap.get(rule.getKey()).get(i);
        if (jumps < minJumps) {
          minJumps = jumps;
          minJumpsOptionIndex = i;
        }
      }
      shortestOptionIndexMap.put(rule.getKey(), minJumpsOptionIndex);
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
    int[] bitUsages = new int[genotype.length()];
    Node<T> tree;
    if (recursive) {
      tree = mapRecursively(grammar.getStartingSymbol(), Range.closedOpen(0, genotype.length()), genotype, bitUsages);
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
      double value = (double) slices.get(i).count() / (double) slices.get(i).length();
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
      }
      return options.get(bestOptionIndexes.get(0));
    }
    if (DEBUG) {
      System.out.printf(" (%d on %d)%n",
              bestOptionIndexes.get(genotype.count() % bestOptionIndexes.size()),
              bestOptionIndexes.size()
      );
    }
    return options.get(bestOptionIndexes.get(genotype.count() % bestOptionIndexes.size()));
  }
  
  public Node<T> mapIteratively(BitsGenotype genotype, int[] bitUsages) throws MappingException {
    Node<EnhancedSymbol<T>> enhancedTree = new Node<>(new EnhancedSymbol<>(grammar.getStartingSymbol(), Range.closedOpen(0, genotype.length())));
    while (true) {
      Node<EnhancedSymbol<T>> nodeToBeReplaced = null;
      for (Node<EnhancedSymbol<T>> node : enhancedTree.leaves()) {
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
        symbols = options.get(shortestOptionIndexMap.get(symbol));
        if (DEBUG) {
          System.out.printf("\tOPTION:%n");
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
        symbols = options.get(shortestOptionIndexMap.get(symbol));
        if (DEBUG) {
          System.out.printf("\tOPTION:%n");
        }
      } else {
        symbols = chooseOption(genotype, range, options);
        for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
          bitUsages[i] = bitUsages[i] + 1;
        }
      }
      //add children
      List<Range<Integer>> childRanges = getSlices(range, symbols);
      if (DEBUG) {
        System.out.print("\tSPLIT :");
      }
      for (int i = 0; i < symbols.size(); i++) {
        Range<Integer> childRange = childRanges.get(i);
        if (childRanges.get(i).equals(range) && (childRange.upperEndpoint() - childRange.lowerEndpoint() > 0)) {
          childRange = Range.closedOpen(range.lowerEndpoint(), range.upperEndpoint() - 1);
          childRanges.set(i, childRange);
        }
        if (DEBUG) {
          System.out.printf(" %s", genotype.slice(childRange));
          if (childRanges.get(i).equals(range) && (childRange.upperEndpoint() - childRange.lowerEndpoint() > 0)) {
            System.out.print("*");
          }
        }
      }
      if (DEBUG) {
        System.out.println();
      }
      for (int i = 0; i < symbols.size(); i++) {
        node.getChildren().add(mapRecursively(symbols.get(i), childRanges.get(i), genotype, bitUsages));
      }
    }
    return node;
  }


  public static void main(String[] args) throws IOException, MappingException {
    //HierarchicalMapper mapper = new HierarchicalMapper(Utils.parseFromFile(new File("grammars/symbolic-regression-xy-nums.bnf")));
    HierarchicalMapper mapper = new WeightedHierarchicalMapper(3, Utils.parseFromFile(new File("grammars/symbolic-regression-xy-nums.bnf")));
    //System.out.println(mapper.map(new BitsGenotype("011010010000110101011000000000111100011001111101"), new HashMap()));
    //BitsGenotype g = new BitsGenotype("111100011001011101011010010000110101111000000000");
    //BitsGenotype g = new BitsGenotype("011010010000110101011000000000111100011001111101"); //125
    BitsGenotype g = new BitsGenotype("011010010000110101011000000000111100011000000101"); //125
    System.out.println(mapper.mapRecursively(
            mapper.getGrammar().getStartingSymbol(),
            Range.closedOpen(0, g.length()),
            g,
            new int[g.length()]
    ));
    if (true) {
      return;
    }
    HierarchicalMapper m1 = new HierarchicalMapper(mapper.getGrammar(), true);
    HierarchicalMapper m2 = new HierarchicalMapper(mapper.getGrammar(), false);
    long millis1 = 0;
    long millis2 = 0;
    List<BitsGenotype> genos = new ArrayList<>();
    BitsGenotypeFactory bgf = new BitsGenotypeFactory(1024);
    Random r = new Random(1);
    Map<String, Object> report = new HashMap<>();
    for (int i = 0; i<10000; i++) {
      genos.add(bgf.build(r));
    }
    for (BitsGenotype geno : genos) {
      if (!m1.map(geno, report).equals(m2.map(geno, report))) {
        System.out.printf("Different result for %s%n", geno);
      }
    }
    millis1 = System.currentTimeMillis();
    for (BitsGenotype geno : genos) {
      m1.map(geno, report);
    }
    millis1 = System.currentTimeMillis()-millis1;
    millis2 = System.currentTimeMillis();
    for (BitsGenotype geno : genos) {
      m2.map(geno, report);
    }
    millis2 = System.currentTimeMillis()-millis2;
    System.out.printf("%d vs %d%n", millis1, millis2);
  }
  
}
