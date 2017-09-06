/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.mapper;

import com.google.common.collect.Range;
import it.units.malelab.ege.benchmark.mapper.element.Element;
import it.units.malelab.ege.benchmark.mapper.element.Function;
import it.units.malelab.ege.benchmark.mapper.element.NumericConstant;
import it.units.malelab.ege.benchmark.mapper.element.Variable;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.util.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author eric
 */
public class MapperUtils {

  public static Node<Element> transform(Node<String> stringNode) {
    if (stringNode.getChildren().isEmpty()) {
      Element element = fromString(stringNode.getContent());
      if (element == null) {
        return null;
      }
      return new Node<>(element);
    }
    if (stringNode.getChildren().size() == 1) {
      return transform(stringNode.getChildren().get(0));
    }
    Node<Element> node = transform(stringNode.getChildren().get(0));
    for (int i = 1; i < stringNode.getChildren().size(); i++) {
      Node<Element> child = transform(stringNode.getChildren().get(i));
      if (child != null) {
        node.getChildren().add(child);
      }
    }
    return node;
  }

  private static Element fromString(String string) {
    try {
      double value = Double.parseDouble(string);
      return new NumericConstant(value);
    } catch (NumberFormatException ex) {
      //just ignore
    }
    for (Variable variable : Variable.values()) {
      if (variable.getGrammarName().equals(string)) {
        return variable;
      }
    }
    for (Function function : Function.values()) {
      if (function.getGrammarName().equals(string)) {
        return function;
      }
    }
    return null;
  }

  public static Object compute(Node<Element> node, BitsGenotype g, List<Double> values, int depth, GlobalCounter globalCounter) {

    System.out.printf("%12.12s\t%s\t%s\t%d\t%d%n", node.getContent(), g, values, depth, globalCounter.r());

    if (node.getContent() instanceof Variable) {
      switch (((Variable) node.getContent())) {
        case GENOTYPE:
          return g;
        case LIST_N:
          return values;
        case DEPTH:
          return (double) depth;
        case GL_COUNT_R:
          return (double) globalCounter.r();
        case GL_COUNT_RW:
          return (double) globalCounter.rw();
      }
    }
    if (node.getContent() instanceof Function) {
      switch (((Function) node.getContent())) {
        case LENGTH:
          return ((List) compute(node.getChildren().get(0), g, values, depth, globalCounter)).size();
        case SIZE:
          return ((BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter)).size();
        case COUNT:
          return ((BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter)).count();
        case COUNT_R:
          BitsGenotype bitsGenotype = (BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter);
          return bitsGenotype.count() / bitsGenotype.size();
        case INT:
          return (double)((BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter)).toInt();
        case ROTATE:
          return rotate(
                  (BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue()
          );
        case SUBSTRING:
          return substring(
                  (BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue()
          );
        case SPLIT:
          return split(
                  (BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue()
          );
        case SPLIT_W:
          return splitWeighted(
                  (BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  (List<Double>) compute(node.getChildren().get(1), g, values, depth, globalCounter)
          );
        case LIST:
          return list(compute(node.getChildren().get(0), g, values, depth, globalCounter));
        case CONCAT:
          return concat(
                  (List) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  (List) compute(node.getChildren().get(1), g, values, depth, globalCounter)
          );
        case REPEAT:
          return repeat(
                  compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(0), g, values, depth, globalCounter)).intValue()
          );
        case APPLY:
          return apply(
                  (Function) node.getChildren().get(0).getContent(),
                  ((List<BitsGenotype>) compute(node.getChildren().get(1), g, values, depth, globalCounter))
          );
        case OP_ADD:
          return ((Double) compute(node.getChildren().get(0), g, values, depth, globalCounter)
                  + (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter));
        case OP_SUBTRACT:
          return ((Double) compute(node.getChildren().get(0), g, values, depth, globalCounter)
                  - (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter));
        case OP_MULT:
          return ((Double) compute(node.getChildren().get(0), g, values, depth, globalCounter)
                  * (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter));
        case OP_DIVIDE:
          return protectedDivision(
                  (Double) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)
          );
        case OP_REMAINDER:
          return protectedRemainder(
                  (Double) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)
          );
      }
    }
    if (node.getContent() instanceof NumericConstant) {
      return ((NumericConstant) node.getContent()).getValue();
    }
    return null;
  }

  private static double protectedDivision(double d1, double d2) {
    if (d2 == 0) {
      return 0d;
    }
    return d1 / d2;
  }

  private static double protectedRemainder(double d1, double d2) {
    if (d2 == 0) {
      return 0d;
    }
    return d1 % d2;
  }

  private static BitsGenotype rotate(BitsGenotype g, int n) {
    BitsGenotype copy = new BitsGenotype(g.size());
    n = n % g.size();
    copy.set(0, g.slice(n, g.size()));
    copy.set(g.size() - n, g.slice(0, n));
    return copy;
  }

  private static BitsGenotype substring(BitsGenotype g, int to) {
    return g.slice(0, Math.min(to, g.size()));
  }

  private static List<BitsGenotype> split(BitsGenotype g, int n) {
    n = Math.max(1, n);
    n = Math.min(n, g.size());
    List<Range<Integer>> ranges = Utils.slices(Range.closedOpen(0, g.size()), n);
    return g.slices(ranges);
  }

  private static List<BitsGenotype> splitWeighted(BitsGenotype g, List<Double> weights) {
    double minWeight = Double.POSITIVE_INFINITY;
    for (double w : weights) {
      if ((w < minWeight) && (w > 0)) {
        minWeight = w;
      }
    }
    if (Double.isInfinite(minWeight)) {
      if (weights.isEmpty()) {
        return Collections.singletonList(g);
      }
      return split(g, weights.size());
    }
    List<Integer> intWeights = new ArrayList<>(weights.size());
    for (double w : weights) {
      intWeights.add((int) Math.round(w / minWeight));
    }
    List<Range<Integer>> ranges = Utils.slices(Range.closedOpen(0, g.size()), intWeights);
    return g.slices(ranges);
  }

  private static List list(Object item) {
    List l = new ArrayList(1);
    l.add(item);
    return l;
  }

  private static List concat(List l1, List l2) {
    List l = new ArrayList(l1);
    l.addAll(l2);
    return l;
  }

  private static List<Double> apply(Function function, List<BitsGenotype> l1) {
    List<Double> l = new ArrayList<>(l1.size());
    for (BitsGenotype g : l1) {
      switch (function) {
        case SIZE:
          l.add((double) g.size());
          break;
        case COUNT:
          l.add((double) g.count());
          break;
        case COUNT_R:
          l.add((double) g.count() / (double) g.size());
          break;
        case INT:
          l.add((double) g.toInt());
          break;
        default:
          l.add(0d);
      }
    }
    return l;
  }

  private static <T> List<T> repeat(T element, int n) {
    List<T> list = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      list.add(element);
    }
    return list;
  }

}
