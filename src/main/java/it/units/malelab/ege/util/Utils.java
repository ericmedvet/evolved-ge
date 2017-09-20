/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util;

import com.google.common.collect.Range;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Grammar;
import it.units.malelab.ege.core.Sequence;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.event.EvolutionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author eric
 */
public class Utils {

  public static Grammar<String> parseFromFile(File file) throws FileNotFoundException, IOException {
    return parseFromFile(file, "UTF-8");
  }

  public static Grammar<String> parseFromFile(File file, String charset) throws FileNotFoundException, IOException {
    Grammar<String> grammar = new Grammar<>();
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
    String line;
    while ((line = br.readLine()) != null) {
      String[] components = line.split(Pattern.quote(Grammar.RULE_ASSIGNMENT_STRING));
      String toReplaceSymbol = components[0].trim();
      String[] optionStrings = components[1].split(Pattern.quote(Grammar.RULE_OPTION_SEPARATOR_STRING));
      if (grammar.getStartingSymbol() == null) {
        grammar.setStartingSymbol(toReplaceSymbol);
      }
      List<List<String>> options = new ArrayList<>();
      for (String optionString : optionStrings) {
        List<String> symbols = new ArrayList<>();
        for (String symbol : optionString.split("\\s+")) {
          if (!symbol.trim().isEmpty()) {
            symbols.add(symbol.trim());
          }
        }
        if (!symbols.isEmpty()) {
          options.add(symbols);
        }
      }
      grammar.getRules().put(toReplaceSymbol, options);
    }
    br.close();
    return grammar;
  }

  public static double mean(double[] values) {
    if (values.length == 0) {
      return Double.NaN;
    }
    double mean = 0;
    for (double value : values) {
      mean = mean + value;
    }
    return mean / (double) values.length;
  }

  public static <T> Grammar<Pair<T, Integer>> resolveRecursiveGrammar(Grammar<T> grammar, int maxDepth) {
    Grammar<Pair<T, Integer>> resolvedGrammar = new Grammar<>();
    //build tree from recursive
    Node<T> tree = expand(grammar.getStartingSymbol(), grammar, 0, maxDepth);
    //decorate tree with depth
    Node<Pair<T, Integer>> decoratedTree = decorateTreeWithDepth(tree);
    decoratedTree.propagateParentship();
    //rewrite grammar
    resolvedGrammar.getRules().put(new Pair<>(grammar.getStartingSymbol(), 0), new ArrayList<List<Pair<T, Integer>>>());
    resolvedGrammar.setStartingSymbol(new Pair<>(grammar.getStartingSymbol(), 0));
    while (true) {
      Pair<T, Integer> toFillDecoratedNonTerminal = null;
      for (Pair<T, Integer> decoratedNonTerminal : resolvedGrammar.getRules().keySet()) {
        if (resolvedGrammar.getRules().get(decoratedNonTerminal).isEmpty()) {
          toFillDecoratedNonTerminal = decoratedNonTerminal;
          break;
        }
      }
      if (toFillDecoratedNonTerminal == null) {
        break;
      }
      //look for this non-terminal in the tree
      List<Pair<T, Integer>> decoratedSymbols = contents(findNodeWithContent(decoratedTree, toFillDecoratedNonTerminal).getChildren());
      Map<T, Pair<T, Integer>> map = new LinkedHashMap<>();
      for (Pair<T, Integer> pair : decoratedSymbols) {
        map.put(pair.getFirst(), pair);
      }
      //process original rule
      List<List<T>> options = grammar.getRules().get(toFillDecoratedNonTerminal.getFirst());
      for (List<T> option : options) {
        if (map.keySet().containsAll(option)) {
          List<Pair<T, Integer>> decoratedOption = new ArrayList<>(option.size());
          for (T symbol : option) {
            Pair<T, Integer> decoratedSymbol = map.get(symbol);
            decoratedOption.add(decoratedSymbol);
            if (!resolvedGrammar.getRules().keySet().contains(decoratedSymbol) && grammar.getRules().keySet().contains(symbol)) {
              resolvedGrammar.getRules().put(decoratedSymbol, new ArrayList<List<Pair<T, Integer>>>());
            }
          }
          resolvedGrammar.getRules().get(toFillDecoratedNonTerminal).add(decoratedOption);
        }
      }
      if (resolvedGrammar.getRules().get(toFillDecoratedNonTerminal).isEmpty()) {
        throw new IllegalArgumentException(String.format("Cannot expand this grammar with this maxDepth, due to rule for %s.", toFillDecoratedNonTerminal));
      }
    }
    return resolvedGrammar;
  }

  public static <T> Node<T> expand(T symbol, Grammar<T> grammar, int depth, int maxDepth) {
    //TODO something not good here on text.bnf
    if (depth > maxDepth) {
      return null;
    }
    Node<T> node = new Node<>(symbol);
    List<List<T>> options = grammar.getRules().get(symbol);
    if (options == null) {
      return node;
    }
    Set<Node<T>> children = new LinkedHashSet<>();
    for (List<T> option : options) {
      Set<Node<T>> optionChildren = new LinkedHashSet<>();
      boolean nullNode = false;
      for (T optionSymbol : option) {
        Node<T> child = expand(optionSymbol, grammar, depth + 1, maxDepth);
        if (child == null) {
          nullNode = true;
          break;
        }
        optionChildren.add(child);
      }
      if (!nullNode) {
        children.addAll(optionChildren);
      }
    }
    if (children.isEmpty()) {
      return null;
    }
    for (Node<T> child : children) {
      node.getChildren().add(child);
    }
    node.propagateParentship();
    return node;
  }

  private static <T> Node<T> findNodeWithContent(Node<T> tree, T content) {
    if (tree.getContent().equals(content)) {
      return tree;
    }
    Node<T> foundNode = null;
    for (Node<T> child : tree.getChildren()) {
      foundNode = findNodeWithContent(child, content);
      if (foundNode != null) {
        break;
      }
    }
    return foundNode;
  }

  private static <T> Node<Pair<T, Integer>> decorateTreeWithDepth(Node<T> tree) {
    Node<Pair<T, Integer>> decoratedTree = new Node<>(new Pair<>(tree.getContent(), count(contents(tree.getAncestors()), tree.getContent())));
    for (Node<T> child : tree.getChildren()) {
      decoratedTree.getChildren().add(decorateTreeWithDepth(child));
    }
    return decoratedTree;
  }

  public static <T> int count(Collection<T> ts, T matchT) {
    int count = 0;
    for (T t : ts) {
      if (t.equals(matchT)) {
        count = count + 1;
      }
    }
    return count;
  }

  public static <T> List<T> contents(List<Node<T>> nodes) {
    List<T> contents = new ArrayList<>(nodes.size());
    for (Node<T> node : nodes) {
      contents.add(node.getContent());
    }
    return contents;
  }

  public static <T> void prettyPrintTree(Node<T> node, PrintStream ps) {
    ps.printf("%" + (1 + node.getAncestors().size() * 2) + "s-%s%n", "", node.getContent());
    for (Node<T> child : node.getChildren()) {
      prettyPrintTree(child, ps);
    }
  }

  public static void broadcast(final EvolutionEvent event, List<? extends EvolverListener> listeners, final ExecutorService executor) {
    for (final EvolverListener listener : listeners) {
      if (listener.getEventClasses().contains(event.getClass())) {
        executor.submit(new Runnable() {
          @Override
          public void run() {
            listener.listen(event);
          }
        });
      }
    }
  }

  public static <T> List<T> getAll(List<Future<List<T>>> futures) throws InterruptedException, ExecutionException {
    List<T> results = new ArrayList<>();
    for (Future<List<T>> future : futures) {
      results.addAll(future.get());
    }
    return results;
  }

  public static <T> T selectRandom(Map<T, Double> options, Random random) {
    double sum = 0;
    for (Double rate : options.values()) {
      sum = sum + rate;
    }
    double d = random.nextDouble() * sum;
    for (Map.Entry<T, Double> option : options.entrySet()) {
      if (d < option.getValue()) {
        return option.getKey();
      }
      d = d - option.getValue();
    }
    return (T) options.keySet().toArray()[0];
  }

  public static <K, V> Map<K, V> sameValueMap(V value, K... keys) {
    Map<K, V> map = new LinkedHashMap<>();
    for (K key : keys) {
      map.put(key, value);
    }
    return map;
  }

  public static void printIndividualAncestry(Individual<?, ?, ?> individual, PrintStream ps) {
    printIndividualAncestry(individual, ps, 0);
  }

  private static void printIndividualAncestry(Individual<?, ?, ?> individual, PrintStream ps, int pad) {
    for (int i = 0; i < pad; i++) {
      ps.print(" ");
    }
    ps.printf("'%20.20s' (%3d w/ %10.10s) f=%5.5s%n",
            individual.getPhenotype().leafNodes(),
            individual.getBirthDate(),
            individual.getFitness());
    for (Individual<?, ?, ?> parent : individual.getParents()) {
      printIndividualAncestry(parent, ps, pad + 2);
    }
  }

  public static List<Range<Integer>> slices(Range<Integer> range, int pieces) {
    List<Integer> sizes = new ArrayList<>(pieces);
    for (int i = 0; i < pieces; i++) {
      sizes.add(1);
    }
    return slices(range, sizes);
  }

  public static List<Range<Integer>> slices(Range<Integer> range, List<Integer> sizes) {
    int length = range.upperEndpoint() - range.lowerEndpoint();
    int sumOfSizes = 0;
    for (int size : sizes) {
      sumOfSizes = sumOfSizes + size;
    }
    if (sumOfSizes > length) {
      List<Integer> originalSizes = new ArrayList<>(sizes);
      sizes = new ArrayList<>(sizes.size());
      int oldSumOfSizes = sumOfSizes;
      sumOfSizes = 0;
      for (int originalSize : originalSizes) {
        int newSize = (int) Math.round((double) originalSize / (double) oldSumOfSizes);
        sizes.add(newSize);
        sumOfSizes = sumOfSizes + newSize;
      }
    }
    int minSize = (int) Math.floor((double) length / (double) sumOfSizes);
    int missing = length - minSize * sumOfSizes;
    int[] rangeSize = new int[sizes.size()];
    for (int i = 0; i < rangeSize.length; i++) {
      rangeSize[i] = minSize * sizes.get(i);
    }
    int c = 0;
    while (missing > 0) {
      rangeSize[c % rangeSize.length] = rangeSize[c % rangeSize.length] + 1;
      c = c + 1;
      missing = missing - 1;
    }
    List<Range<Integer>> ranges = new ArrayList<>(sizes.size());
    int offset = range.lowerEndpoint();
    for (int i = 0; i < rangeSize.length; i++) {
      ranges.add(Range.closedOpen(offset, offset + rangeSize[i]));
      offset = offset + rangeSize[i];
    }
    return ranges;
  }

  public static class MapBuilder<K, V> {

    private LinkedHashMap<K, V> map;

    public MapBuilder() {
      map = new LinkedHashMap<>();
    }

    public MapBuilder<K, V> put(K key, V value) {
      map.put(key, value);
      return this;
    }

    public Map<K, V> build() {
      return map;
    }

  }

  public static <T> boolean validate(Node<T> tree, Grammar<T> grammar) {
    if (tree == null) {
      return false;
    }
    if (!tree.getContent().equals(grammar.getStartingSymbol())) {
      return false;
    }
    Set<T> terminals = new LinkedHashSet<>();
    for (List<List<T>> options : grammar.getRules().values()) {
      for (List<T> option : options) {
        terminals.addAll(option);
      }
    }
    terminals.removeAll(grammar.getRules().keySet());
    return innerValidate(tree, grammar, terminals);
  }

  private static <T> boolean innerValidate(Node<T> tree, Grammar<T> grammar, Set<T> terminals) {
    //validate node content
    if (!grammar.getRules().keySet().contains(tree.getContent()) && !terminals.contains(tree.getContent())) {
      return false;
    }
    if (terminals.contains(tree.getContent())) {
      return true;
    }
    //validate node children sequence (option)
    List<T> childContents = new ArrayList<>();
    for (Node<T> child : tree.getChildren()) {
      childContents.add(child.getContent());
    }
    if (!grammar.getRules().get(tree.getContent()).contains(childContents)) {
      return false;
    }
    for (Node<T> child : tree.getChildren()) {
      if (!innerValidate(child, grammar, terminals)) {
        return false;
      }
    }
    return true;
  }

  public static double pearsonCorrelation(List<Pair<Double, Double>> values) {
    if (values.isEmpty() || values.size() == 1) {
      return Double.NaN;
    }
    double[] x = new double[values.size()];
    double[] y = new double[values.size()];
    for (int i = 0; i < values.size(); i++) {
      x[i] = values.get(i).getFirst();
      y[i] = values.get(i).getSecond();
    }
    return new PearsonsCorrelation().correlation(x, y);
  }

  public static <T> Sequence<T> fromList(final List<T> list) {
    return new Sequence<T>() {
      @Override
      public T get(int index) {
        return list.get(index);
      }

      @Override
      public int size() {
        return list.size();
      }
    };
  }

  public static <T> Node<T> node(T t, Node<T>... children) {
    Node<T> n = new Node<>(t);
    for (Node<T> child : children) {
      n.getChildren().add(child);
    }
    return n;
  }

}
