/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author eric
 */
public class Node<T> {
  
  private final T content;
  private final List<Node<T>> children = new ArrayList<>();

  public Node(T content) {
    this.content = content;
  }

  public T getContent() {
    return content;
  }

  public List<Node<T>> getChildren() {
    return children;
  }
  
  public List<Node<T>> flat() {
    if (children.isEmpty()) {
      return Collections.singletonList(this);
    }
    List<Node<T>> childContents = new ArrayList<>();
    for (Node<T> child : children) {
      childContents.addAll(child.flat());
    }
    return childContents;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(content);
    if (!children.isEmpty()) {
      sb.append("{");
      for (Node<T> child : children) {
        sb.append(child.toString()).append(",");
      }
      sb.deleteCharAt(sb.length()-1);
      sb.append("}");
    }
    return sb.toString();
  }
  
  
  
}
