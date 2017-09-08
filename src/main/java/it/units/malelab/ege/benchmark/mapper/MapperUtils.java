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
      if (child != null) { //discard decorations
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
    Object result = null;
    if (node.getContent() instanceof Variable) {
      switch (((Variable) node.getContent())) {
        case GENOTYPE:
          result = g;
          break;
        case LIST_N:
          result = values;
          break;
        case DEPTH:
          result = (double) depth;
          break;
        case GL_COUNT_R:
          result = (double) globalCounter.r();
          break;
        case GL_COUNT_RW:
          result = (double) globalCounter.rw();
          break;
      }
    } else if (node.getContent() instanceof Function) {
      switch (((Function) node.getContent())) {
        case SIZE:
          result = (double) ((BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter)).size();
          break;
        case WEIGHT:
          result = (double) ((BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter)).count();
          break;
        case WEIGHT_R:
          BitsGenotype bitsGenotype = (BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter);
          result = (double) bitsGenotype.count() / (double) bitsGenotype.size();
          break;
        case INT:
          result = (double) ((BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter)).toInt();
          break;
        case ADD:
          result = ((Double) compute(node.getChildren().get(0), g, values, depth, globalCounter)
                  + (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter));
          break;
        case SUBTRACT:
          result = ((Double) compute(node.getChildren().get(0), g, values, depth, globalCounter)
                  - (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter));
          break;
        case MULT:
          result = ((Double) compute(node.getChildren().get(0), g, values, depth, globalCounter)
                  * (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter));
          break;
        case DIVIDE:
          result = protectedDivision(
                  (Double) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)
          );
          break;
        case REMAINDER:
          result = protectedRemainder(
                  (Double) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  (Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)
          );
          break;
        case LENGTH:
          result = (double) ((List) compute(node.getChildren().get(0), g, values, depth, globalCounter)).size();
          break;
        case MAX_INDEX:
          result = (double) maxIndex((List<Double>) compute(node.getChildren().get(0), g, values, depth, globalCounter), 1d);
          break;
        case MIN_INDEX:
          result = (double) maxIndex((List<Double>) compute(node.getChildren().get(0), g, values, depth, globalCounter), -1d);
          break;
        case GET:
          result = getFromList(
                  (List) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue()
          );
          break;
        case SEQ:
          result = seq(
                  ((Double) compute(node.getChildren().get(0), g, values, depth, globalCounter)).intValue(),
                  values.size()
          );
          break;
        case REPEAT:
          result = repeat(
                  compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue(),
                  values.size()
          );
          break;
        case ROTATE_SX:
          result = rotateSx(
                  (BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue()
          );
          break;
        case ROTATE_DX:
          result = rotateDx(
                  (BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue()
          );
          break;
        case SUBSTRING:
          result = substring(
                  (BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue()
          );
          break;
        case SPLIT:
          result = split(
                  (BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  ((Double) compute(node.getChildren().get(1), g, values, depth, globalCounter)).intValue(),
                  values.size()
          );
          break;
        case SPLIT_W:
          result = splitWeighted(
                  (BitsGenotype) compute(node.getChildren().get(0), g, values, depth, globalCounter),
                  (List<Double>) compute(node.getChildren().get(1), g, values, depth, globalCounter),
                  values.size()
          );
          break;
        case APPLY:
          result = apply(
                  (Function) node.getChildren().get(0).getContent(),
                  ((List) compute(node.getChildren().get(1), g, values, depth, globalCounter)),
                  (node.getChildren().size() >= 3) ? compute(node.getChildren().get(2), g, values, depth, globalCounter) : null
          );
          break;
      }
    } else if (node.getContent() instanceof NumericConstant) {
      result = ((NumericConstant) node.getContent()).getValue();
    }
    return result;
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

  private static BitsGenotype rotateDx(BitsGenotype g, int n) {
    if (g.size() == 0) {
      return g;
    }
    n = n % g.size();
    if (n <= 0) {
      return g;
    }
    BitsGenotype copy = new BitsGenotype(g.size());
    copy.set(0, g.slice(g.size() - n, g.size()));
    copy.set(n, g.slice(0, g.size() - n));
    return copy;
  }

  private static BitsGenotype rotateSx(BitsGenotype g, int n) {
    if (g.size() == 0) {
      return g;
    }
    n = n % g.size();
    if (n <= 0) {
      return g;
    }
    BitsGenotype copy = new BitsGenotype(g.size());
    copy.set(0, g.slice(n, g.size()));
    copy.set(g.size() - n, g.slice(0, n));
    return copy;
  }

  private static BitsGenotype substring(BitsGenotype g, int to) {
    if (to <= 0) {
      return new BitsGenotype(0);
    }
    if (g.size() == 0) {
      return g;
    }
    return g.slice(0, Math.min(to, g.size()));
  }

  private static List<BitsGenotype> split(BitsGenotype g, int n, int maxN) {
    if (n <= 0) {
      return Collections.singletonList(g);
    }
    if (n > maxN) {
      n = maxN;
    }
    if (g.size() == 0) {
      return Collections.nCopies(n, new BitsGenotype(0));
    }
    n = Math.max(1, n);
    n = Math.min(n, g.size());
    List<Range<Integer>> ranges = Utils.slices(Range.closedOpen(0, g.size()), n);
    return g.slices(ranges);
  }

  private static List<BitsGenotype> splitWeighted(BitsGenotype g, List<Double> weights, int maxN) {
    if (weights.isEmpty()) {
      return Collections.singletonList(g);
    }
    if (g.size() == 0) {
      return Collections.nCopies(weights.size(), new BitsGenotype(0));
    }
    double minWeight = Double.POSITIVE_INFINITY;
    for (double w : weights) {
      if ((w < minWeight) && (w > 0)) {
        minWeight = w;
      }
    }
    if (Double.isInfinite(minWeight)) {
      //all zero
      return split(g, weights.size(), maxN);
    }
    List<Integer> intWeights = new ArrayList<>(weights.size());
    for (double w : weights) {
      intWeights.add((int) Math.max(Math.round(w / minWeight), 0d));
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

  private static List apply(Function function, List inputList, Object arg) {
    List outputList = new ArrayList(inputList.size());
    for (Object repeatedArg : inputList) {
      switch (function) {
        case SIZE:
          outputList.add((double) ((BitsGenotype) repeatedArg).size());
          break;
        case WEIGHT:
          outputList.add((double) ((BitsGenotype) repeatedArg).count());
          break;
        case WEIGHT_R:
          outputList.add((double) ((BitsGenotype) repeatedArg).count() / (double) ((BitsGenotype) repeatedArg).size());
          break;
        case INT:
          outputList.add((double) ((BitsGenotype) repeatedArg).toInt());
          break;
        case ROTATE_SX:
          outputList.add(rotateSx((BitsGenotype) arg, ((Double) repeatedArg).intValue()));
          break;
        case ROTATE_DX:
          outputList.add(rotateDx((BitsGenotype) arg, ((Double) repeatedArg).intValue()));
          break;
        case SUBSTRING:
          outputList.add(substring((BitsGenotype) arg, ((Double) repeatedArg).intValue()));
          break;
      }
    }
    return outputList;
  }

  private static <T> List<T> repeat(T element, int n, int maxN) {
    if (n <= 0) {
      return Collections.singletonList(element);
    }
    if (n > maxN) {
      n = maxN;
    }
    List<T> list = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      list.add(element);
    }
    return list;
  }

  private static <T> T getFromList(List<T> list, int n) {
    n = Math.min(n, list.size() - 1);
    n = Math.max(0, n);
    return list.get(n);
  }

  private static int maxIndex(List<Double> list, double mult) {
    if (list.isEmpty()) {
      return 0;
    }
    int index = 0;
    for (int i = 1; i < list.size(); i++) {
      if (mult * list.get(i) > mult * list.get(index)) {
        index = i;
      }
    }
    return index;
  }

  private static List<Double> seq(int n, int maxN) {
    if (n > maxN) {
      n = maxN;
    }
    if (n < 1) {
      n = 1;
    }
    List<Double> list = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      list.add((double) i);
    }
    return list;
  }

}
