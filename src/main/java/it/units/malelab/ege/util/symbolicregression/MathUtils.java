/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util.symbolicregression;

import it.units.malelab.ege.core.grammar.Node;
import it.units.malelab.ege.evolver.PhenotypePrinter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author eric
 */
public class MathUtils {
  
  public static double[] compute(Node<Element> node, Map<String, double[]> values, int length) {
    if (node.getContent() instanceof Decoration) {
      return null;
    }
    if (node.getContent() instanceof Variable) {
      double[] result = values.get(node.getContent().toString());
      if (result==null) {
        throw new RuntimeException(String.format("Undefined variable: %s", node.getContent().toString()));
      }
      return result;
    }
    double[] result = new double[length];
    if (node.getContent() instanceof Constant) {
      Arrays.fill(result, ((Constant)node.getContent()).getValue());
      return result;
    }
    double[][] childrenValues = new double[node.getChildren().size()][];
    int i = 0;
    for (Node<Element> child : node.getChildren()) {
      double[] childValues = compute(child, values, length);
      if (childValues!=null) {
        childrenValues[i] = childValues;
        i = i+1;
      }
    }
    for (int j = 0; j<result.length; j++) {
      double[] operands = new double[childrenValues.length];
      for (int k = 0; k<operands.length; k++) {
        operands[k] = childrenValues[k][j];
      }
      result[j] = compute((Operator)node.getContent(), operands);
    }
    return result;
  }
  
  private static double compute(Operator operator, double... operands) {
    switch (operator) {
      case ADDITION: return operands[0]+operands[1];
      case COS: return Math.cos(operands[0]);
      case DIVISION: return operands[0]/operands[1];
      case EXP: return Math.exp(operands[0]);
      case INVERSE: return 1/operands[0];
      case LOG: return Math.log(operands[0]);
      case MULTIPLICATION: return operands[0]*operands[1];
      case OPPOSITE: return -operands[0];
      case SIN: return Math.sin(operands[0]);
      case SQRT: return Math.sqrt(operands[0]);
      case SUBTRACTION: return operands[0]-operands[1];
    }
    return Double.NaN;
  }

  public static Node<Element> transform(Node<String> stringNode) {
    if (stringNode.getChildren().isEmpty()) {
      return new Node<>(fromString(stringNode.getContent()));
    }
    if (stringNode.getChildren().size()==1) {
      return transform(stringNode.getChildren().get(0));
    }
    Node<Element> node = transform(stringNode.getChildren().get(0));
    for (int i = 1; i<stringNode.getChildren().size(); i++) {
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
    try {
      double value = Double.parseDouble(string);
      return new Constant(value);
    } catch (NumberFormatException ex) {
      //just ignore
    }
    if (string.matches("[a-zA-Z]\\w*")) {
      return new Variable(string);
    }
    return new Decoration(string);
  }
  
  public static double[] uniformSample(double min, double max, double step) {
    double[] values = new double[(int)Math.round((max-min)/step)];
    for (int i = 0; i<values.length; i++) {
      values[i] = min+i*step;
    }
    return values;
  }
  
  public static Map<String, double[]> varValuesMap(String string, double... values) {
    return Collections.singletonMap(string, values);
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
  
}
