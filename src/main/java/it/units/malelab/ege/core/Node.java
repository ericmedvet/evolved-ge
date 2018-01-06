/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core;

import it.units.malelab.ege.util.Utils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author eric
 */
public class Node<T> implements Sequence<T>, Serializable {
  
  public static Node EMPTY_TREE = new Node(null);
  
  private final T content;
  private final List<Node<T>> children = new ArrayList<>();
  private Node<T> parent;
  
  public Node(T content) {
    this.content = content;
  }
  
  public Node(Node<T> original) {
    if (original==null) {
      this.content = null;
      return;
    }
    this.content = original.getContent();
    for (Node<T> child : original.getChildren()) {
      children.add(new Node<>(child));
    }
  }
  
  public T getContent() {
    return content;
  }

  public List<Node<T>> getChildren() {
    return children;
  }
  
  public List<Node<T>> leafNodes() {
    if (children.isEmpty()) {
      return Collections.singletonList(this);
    }
    List<Node<T>> childContents = new ArrayList<>();
    for (Node<T> child : children) {
      childContents.addAll(child.leafNodes());
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
  
  public List<Node<T>> getAncestors() {
    if (parent==null) {
      return Collections.EMPTY_LIST;
    }
    List<Node<T>> ancestors = new ArrayList<>();
    ancestors.add(parent);
    ancestors.addAll(parent.getAncestors());
    return Collections.unmodifiableList(ancestors);
  }

  public Node<T> getParent() {
    return parent;
  }
  
  public void propagateParentship() {
    for (Node<T> child : children) {
      child.parent = this;
      child.propagateParentship();
    }
  }
  
  public int depth() {
    int max = 0;
    for (Node<T> child : children) {
      max = Math.max(max, child.depth());
    }
    return max+1;
  }
  
  public int nodeSize() {
    int size = 0;
    for (Node<T> child : children) {
      size = size+child.nodeSize();
    }
    return size+1;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 53 * hash + Objects.hashCode(this.content);
    hash = 53 * hash + Objects.hashCode(this.children);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Node<?> other = (Node<?>) obj;
    if (!Objects.equals(this.content, other.content)) {
      return false;
    }
    if (!Objects.equals(this.children, other.children)) {
      return false;
    }
    return true;
  }
  
  public Sequence<T> leafContents() {
    final List<Node<T>> leafNodes = leafNodes();
    return Utils.fromList(Utils.contents(leafNodes));
  }

  @Override
  public T get(int index) {
    return leafContents().get(index);
  }

  @Override
  public int size() {
    return leafContents().size();
  }  

  @Override
  public Sequence<T> clone() {
    return new Node<>(this);
  }

  @Override
  public void set(int index, T t) {
    throw new UnsupportedOperationException("Set not supported on trees.");
  }

}
