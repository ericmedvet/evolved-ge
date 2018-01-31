/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark;

import com.google.common.collect.Range;
import it.units.malelab.ege.core.Grammar;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.PhenotypePrinter;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.FitnessComputer;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author eric
 */
public class KLandscapes extends Problem<String, NumericFitness> {

  private final static int ARITY = 2;
  private final static Range<Double> V_RANGE = Range.closed(-1d, 1d);
  private final static Range<Double> W_RANGE = Range.closed(0d, 1d);
  private final static int N_TERMINALS = 4;
  private final static int N_NON_TERMINALS = 2;

  public KLandscapes(int k) {
    this(k, ARITY, V_RANGE, W_RANGE, N_TERMINALS, N_NON_TERMINALS);
  }

  public KLandscapes(int k, int arity, Range<Double> vRange, Range<Double> wRange, int nTerminals, int nNonTerminals) {
    super(
            buildGrammar(nTerminals, nNonTerminals, arity),
            getFitnessComputer(k, nTerminals, nNonTerminals, arity, vRange, wRange),
            null,
            new PhenotypePrinter<String>() {
      @Override
      public String toString(Node<String> node) {
        return transform(node).toString();
      }
    });
  }

  private static FitnessComputer<String, NumericFitness> getFitnessComputer(final int k, final int nTerminals, final int nNonTerminals, final int arity, Range<Double> vRange, Range<Double> wRange) {
    Random random = new Random(1l);
    final Map<String, Double> v = new LinkedHashMap<>();
    final Map<Pair<String, String>, Double> w = new LinkedHashMap<>();
    //fill v map
    for (int i = 0; i < nTerminals; i++) {
      v.put("t" + i, random.nextDouble() * (vRange.upperEndpoint() - vRange.lowerEndpoint()) + vRange.lowerEndpoint());
    }
    for (int i = 0; i < nNonTerminals; i++) {
      v.put("n" + i, random.nextDouble() * (vRange.upperEndpoint() - vRange.lowerEndpoint()) + vRange.lowerEndpoint());
    }
    //fill w map
    for (int j = 0; j < nNonTerminals; j++) {
      for (int i = 0; i < nTerminals; i++) {
        w.put(new Pair<>("n" + j, "t" + i), random.nextDouble() * (wRange.upperEndpoint() - wRange.lowerEndpoint()) + wRange.lowerEndpoint());
      }
      for (int i = 0; i < nNonTerminals; i++) {
        w.put(new Pair<>("n" + j, "n" + i), random.nextDouble() * (wRange.upperEndpoint() - wRange.lowerEndpoint()) + wRange.lowerEndpoint());
      }
    }
    //prepare fitness
    final double optimumFitness = f(optimum(k, nTerminals, nNonTerminals, arity, v, w), k, v, w);
    return new FitnessComputer<String, NumericFitness>() {
      @Override
      public NumericFitness compute(Node<String> phenotype) {
        Node<String> tree = transform(phenotype);
        return new NumericFitness(1 - f(tree, k, v, w) / optimumFitness);
      }

      @Override
      public NumericFitness worstValue() {
        return new NumericFitness(Double.POSITIVE_INFINITY);
      }

      @Override
      public NumericFitness bestValue() {
        return new NumericFitness(0);
      }
    };
  }

  protected static double f(Node<String> tree, int k, Map<String, Double> v, Map<Pair<String, String>, Double> w) {
    return 1d / (1d + (double) Math.abs(k - tree.depth())) * maxFK(tree, k, v, w);
  }

  protected static double fK(Node<String> tree, int k, Map<String, Double> v, Map<Pair<String, String>, Double> w) {
    if (k == 0) {
      return v.get(tree.getContent());
    }
    double sum = v.get(tree.getContent());
    for (Node<String> child : tree.getChildren()) {
      final double weight = w.get(new Pair<>(tree.getContent(), child.getContent()));
      final double innerFK = fK(child, k - 1, v, w);
      sum = sum + (1 + weight) * innerFK;
    }
    return sum;
  }

  protected static Node<String> optimum(int k, int nTerminals, int nNonTerminals, int arity, Map<String, Double> v, Map<Pair<String, String>, Double> w) {
    Node<String> optimum = null;
    double maxFitness = Double.NEGATIVE_INFINITY;
    for (int d = 1; d <= k + 1; d++) {
      int[] indexes = new int[d]; //indexes of the (non)Terminals to be used. terminal is the last index.
      while (true) {
        Node<String> tree = levelEqualTree(indexes, arity);
        double fitness = f(tree, k, v, w);
        if ((optimum == null) || (fitness > maxFitness)) {
          optimum = tree;
          maxFitness = fitness;
        }
        indexes[indexes.length - 1] = indexes[indexes.length - 1] + 1;
        for (int j = indexes.length - 1; j > 0; j--) {
          int threshold = (j == (indexes.length - 1)) ? nTerminals : nNonTerminals;
          if (indexes[j] == threshold) {
            indexes[j] = 0;
            indexes[j - 1] = indexes[j - 1] + 1;
          }
        }
        if (indexes[0] == nNonTerminals) {
          break;
        }
      }
    }
    return optimum;
  }

  protected static Node<String> levelEqualTree(int[] indexes, int arity) {
    if (indexes.length == 1) {
      return new Node<>("t" + indexes[0]);
    }
    Node<String> node = new Node<>("n" + indexes[0]);
    for (int i = 0; i < arity; i++) {
      node.getChildren().add(levelEqualTree(Arrays.copyOfRange(indexes, 1, indexes.length), arity));
    }
    return node;
  }

  protected static double maxFK(Node<String> tree, int k, Map<String, Double> v, Map<Pair<String, String>, Double> w) {
    double max = fK(tree, k, v, w);
    for (Node<String> child : tree.getChildren()) {
      max = Math.max(max, maxFK(child, k, v, w));
    }
    return max;
  }

  private static Grammar<String> buildGrammar(int nTerminals, int nNonTerminals, int arity) {
    Grammar<String> grammar = new Grammar<>();
    grammar.setStartingSymbol("N");
    grammar.getRules().put("N", l(c(l("n"), r(arity, "N")), l("t")));
    List<List<String>> nonTerminalConstOptions = new ArrayList<>();
    for (int i = 0; i < nNonTerminals; i++) {
      nonTerminalConstOptions.add(l("n" + i));
    }
    grammar.getRules().put("n", nonTerminalConstOptions);
    List<List<String>> terminalConstOptions = new ArrayList<>();
    for (int i = 0; i < nTerminals; i++) {
      terminalConstOptions.add(l("t" + i));
    }
    grammar.getRules().put("t", terminalConstOptions);
    return grammar;
  }

  private static <T> List<T> l(T... ts) {
    return Arrays.asList(ts);
  }

  private static <T> List<T> c(List<T>... tss) {
    List<T> list = new ArrayList<>();
    for (List<T> ts : tss) {
      list.addAll(ts);
    }
    return list;
  }

  private static <T> List<T> r(int n, T... ts) {
    List<T> list = new ArrayList<>(n * ts.length);
    for (int i = 0; i < n; i++) {
      list.addAll(l(ts));
    }
    return list;
  }

  protected static Node<String> transform(Node<String> original) {
    if (Node.EMPTY_TREE.equals(original)) {
      return Node.EMPTY_TREE;
    }
    Node<String> node = new Node<>(original.getChildren().get(0).getChildren().get(0).getContent());
    if (original.getChildren().size() > 1) {
      //is a non terminal node
      for (Node<String> orginalChild : original.getChildren()) {
        if (orginalChild.getContent().equals("N")) {
          node.getChildren().add(transform(orginalChild));
        }
      }
    }
    return node;
  }

}
