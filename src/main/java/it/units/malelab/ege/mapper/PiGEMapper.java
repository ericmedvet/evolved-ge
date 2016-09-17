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
import java.util.List;

/**
 *
 * @author eric
 */
public class PiGEMapper<T> extends AbstractMapper<T> {
  
  private final int codonLenght;
  private final int maxWraps;

  public PiGEMapper(int codonLenght, int maxWraps, Grammar<T> grammar) {
    super(grammar);
    this.codonLenght = codonLenght;
    this.maxWraps = maxWraps;
  }    

  @Override
  public Node<T> map(Genotype genotype) throws MappingException {
    if (genotype.size()<codonLenght) {
      throw new MappingException(String.format("Short genotype (%d<%d)", genotype.size(), codonLenght));
    }
    Node<T> tree = new Node<>(grammar.getStartingSymbol());
    int currentCodonIndex = 0;
    int wraps = 0;
    while (true) {
      List<Node<T>> nodesToBeReplaced = new ArrayList<>();
      for (Node<T> leaf : tree.leaves()) {
        if (grammar.getRules().keySet().contains(leaf.getContent())) {
          nodesToBeReplaced.add(leaf);
        }
      }
      if (nodesToBeReplaced.isEmpty()) {
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
      int nodeIndexCodon = genotype.slice(currentCodonIndex*codonLenght, currentCodonIndex*codonLenght+codonLenght/2).toInt()%nodesToBeReplaced.size();
      Node<T> nodeToBeReplaced = nodesToBeReplaced.get(nodeIndexCodon);
      List<List<T>> options = grammar.getRules().get(nodeToBeReplaced.getContent());
      int optionIndex = genotype.slice(currentCodonIndex*codonLenght+codonLenght/2, (currentCodonIndex+1)*codonLenght).toInt()%options.size();
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
