/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.Node;
import it.units.malelab.ege.grammar.Grammar;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 *
 * @author eric
 */
public class StandardGEMapper<T> extends AbstractMapper<T> {
  
  private final int codonLenght;
  private final int maxWraps;

  public StandardGEMapper(int codonLenght, int maxWraps, Grammar grammar) {
    super(grammar);
    this.codonLenght = codonLenght;
    this.maxWraps = maxWraps;
  }    

  @Override
  public Node<T> map(Genotype genotype) throws MappingException {
    Node<T> tree = new Node<>(grammar.getStartingSymbol());
    int currentCodonIndex = 0;
    int wraps = 0;
    while (true) {
      Node<T> nodeToBeReplaced = null;
      for (Node<T> node : tree.flatNodes()) {
        if (grammar.getRules().keySet().contains(node.getContent())) {
          nodeToBeReplaced = node;
          break;
        }
      }
      if (nodeToBeReplaced==null) {
        break;
      }
      //get codon index and option
      if ((currentCodonIndex+1)*codonLenght>genotype.size()) {
        wraps = wraps+1;
        currentCodonIndex = 0;
        if (wraps>maxWraps) {
          throw new MappingException(String.format("Too many wraps (%d>%d)", wraps, maxWraps));
        }
      }
      List<List<T>> options = grammar.getRules().get(nodeToBeReplaced.getContent());
      int optionIndex = genotype.slice(currentCodonIndex*codonLenght, (currentCodonIndex+1)*codonLenght).toInt()%options.size();
      //add children
      for (T t : options.get(optionIndex)) {
        Node<T> newChild = new Node<>(t);
        nodeToBeReplaced.getChildren().add(newChild);
      }
      currentCodonIndex = currentCodonIndex+1;
    }
    return tree;
  }
     
}
