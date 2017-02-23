/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util;

import it.units.malelab.ege.grammar.Node;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

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
    while ((line = br.readLine())!=null) {
      String[] components = line.split(Pattern.quote(Grammar.RULE_ASSIGNMENT_STRING));
      String toReplaceSymbol = components[0].trim();
      String[] optionStrings = components[1].split(Pattern.quote(Grammar.RULE_OPTION_SEPARATOR_STRING));
      if (grammar.getStartingSymbol()==null) {
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
    if (values.length==0) {
      return Double.NaN;
    }
    double mean = 0;
    for (double value : values) {
      mean = mean+value;
    }
    return mean/(double)values.length;
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
      if (toFillDecoratedNonTerminal==null) {
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
            if (!resolvedGrammar.getRules().keySet().contains(decoratedSymbol)&&grammar.getRules().keySet().contains(symbol)) {
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
    if (depth>maxDepth) {
      return null;
    }
    Node<T> node = new Node<>(symbol);
    List<List<T>> options = grammar.getRules().get(symbol);
    if (options==null) {
      return node;
    }
    Set<Node<T>> children = new LinkedHashSet<>();
    for (List<T> option : options) {
      Set<Node<T>> optionChildren = new LinkedHashSet<>();
      boolean nullNode = false;
      for (T optionSymbol : option) {
        Node<T> child = expand(optionSymbol, grammar, depth+1, maxDepth);
        if (child==null) {
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
      if (foundNode!=null) {
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
        count = count+1;
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
    ps.printf("%"+(1+node.getAncestors().size()*2)+"s-%s%n", "", node.getContent());
    for (Node<T> child : node.getChildren()) {
      prettyPrintTree(child, ps);
    }
  }
    
  public static <G extends Genotype, T> void sortByFitness(List<Individual<G, T>> individuals) {
    Collections.sort(individuals, new Comparator<Individual<G, T>>() {
      @Override
      public int compare(Individual<G, T> i1, Individual<G, T> i2) {
        return i1.getFitness().compareTo(i2.getFitness());
      }
    });
  }
  
  public static <G extends Genotype, T> void broadcast(EvolutionEvent<G, T> event, List<EvolutionListener<G, T>> listeners) {
    for (EvolutionListener<G, T> listener : listeners) {
      if (listener.getEventClasses().contains(event.getClass())) {
        listener.listen(event);
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
      sum = sum+rate;
    }
    double d = random.nextDouble()*sum;
    for (Map.Entry<T, Double> option : options.entrySet()) {
      if (d<option.getValue()) {
        return option.getKey();
      }
      d = d-option.getValue();
    }
    return (T)options.keySet().toArray()[0];
  }
  
  public static <K, V> Map<K, V> sameValueMap(V value, K... keys) {
    Map<K, V> map = new LinkedHashMap<>();
    for (K key : keys) {
      map.put(key, value);
    }
    return map;
  }
  
  public static <G extends Genotype, T> void printIndividualAncestry(Individual<G, T> individual, PrintStream ps) {    
    printIndividualAncestry(individual, ps, 0);
  }
  
  private static <G extends Genotype, T> void printIndividualAncestry(Individual<G, T> individual, PrintStream ps, int pad) {
    for (int i = 0; i<pad; i++) {
      ps.print(" ");
    }
    ps.printf("'%20.20s' (%3d w/ %10.10s) f=%5.5s%n",
            individual.getPhenotype().leaves(),
            individual.getBirthDate(),
            individual.getOperator()!=null?individual.getOperator().getClass().getSimpleName():"",
            individual.getFitness());
    for (Individual<G, T> parent : individual.getParents()) {
      printIndividualAncestry(parent, ps, pad+2);
    }
  }
  
}
