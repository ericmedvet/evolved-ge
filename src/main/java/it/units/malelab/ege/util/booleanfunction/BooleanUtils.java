/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util.booleanfunction;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.PhenotypePrinter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author eric
 */
public class BooleanUtils {

  public static boolean[] compute(Node<Element> node, Map<String, boolean[]> values, int length) {
    if (node.getContent() instanceof Decoration) {
      return null;
    }
    if (node.getContent() instanceof Variable) {
      boolean[] result = values.get(node.getContent().toString());
      if (result == null) {
        throw new RuntimeException(String.format("Undefined variable: %s", node.getContent().toString()));
      }
      return result;
    }
    boolean[] result = new boolean[length];
    if (node.getContent() instanceof Constant) {
      Arrays.fill(result, ((Constant) node.getContent()).getValue());
      return result;
    }
    boolean[][] childrenValues = new boolean[node.getChildren().size()][];
    int i = 0;
    for (Node<Element> child : node.getChildren()) {
      boolean[] childValues = compute(child, values, length);
      if (childValues != null) {
        childrenValues[i] = childValues;
        i = i + 1;
      }
    }
    for (int j = 0; j < result.length; j++) {
      boolean[] operands = new boolean[childrenValues.length];
      for (int k = 0; k < operands.length; k++) {
        operands[k] = childrenValues[k][j];
      }
      result[j] = compute((Operator) node.getContent(), operands);
    }
    return result;
  }

  private static boolean compute(Operator operator, boolean... operands) {
    switch (operator) {
      case AND:
        return operands[0] && operands[1];
      case AND1NOT:
        return (!operands[0]) && operands[1];
      case OR:
        return operands[0] || operands[1];
      case XOR:
        return operands[0] ^ operands[1];
      case NOT:
        return !operands[0];
      case IF:
        return operands[0] ? operands[1] : operands[2];
    }
    return false;
  }

  public static Node<Element> transform(Node<String> stringNode) {
    if (stringNode.getChildren().isEmpty()) {
      return new Node<>(fromString(stringNode.getContent()));
    }
    if (stringNode.getChildren().size() == 1) {
      return transform(stringNode.getChildren().get(0));
    }
    Node<Element> node = transform(stringNode.getChildren().get(0));
    for (int i = 1; i < stringNode.getChildren().size(); i++) {
      node.getChildren().add(transform(stringNode.getChildren().get(i)));
    }
    return node;
  }

  private static Element fromString(String string) {
    for (Operator operator : Operator.values()) {
      if (operator.toString().equals(string)) {
        return operator;
      }
    }
    if (string.equals("0")) {
      return new Constant(false);
    }
    if (string.equals("1")) {
      return new Constant(true);
    }
    if (string.matches("[a-zA-Z]+[0-9.]+")) {
      return new Variable(string);
    }
    return new Decoration(string);
  }

  public static PhenotypePrinter<String> phenotypePrinter() {
    return new PhenotypePrinter<String>() {
      @Override
      public String toString(Node<String> node) {
        if (Node.EMPTY_TREE.equals(node)) {
          return null;
        }
        return transform(node).toString();
      }
    };
  }

  public static Map<String, boolean[]> buildCompleteCases(String... names) {
    Map<String, boolean[]> map = new LinkedHashMap<>();
    for (String name : names) {
      map.put(name, new boolean[(int) Math.pow(2, names.length)]);
    }
    for (int i = 0; i < Math.pow(2, names.length); i++) {
      for (int j = 0; j < names.length; j++) {
        map.get(names[j])[i] = (i & (int) Math.pow(2, j)) > 0;
      }
    }
    return map;
  }

  public static boolean[] toBinary(int input, int size) {
    boolean[] bits = new boolean[size];
    for (int i = size-1; i >= 0; i--) {
      bits[i] = (input & (1 << i)) != 0;
    }
    return bits;
  }

}
