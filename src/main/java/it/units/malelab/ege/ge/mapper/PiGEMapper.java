/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.mapper;

import it.units.malelab.ege.core.mapper.AbstractMapper;
import it.units.malelab.ege.core.mapper.MappingException;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Grammar;
import static it.units.malelab.ege.ge.mapper.StandardGEMapper.BIT_USAGES_INDEX_NAME;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class PiGEMapper<T> extends AbstractMapper<BitsGenotype, T> {
  
  private final int codonLenght;
  private final int maxWraps;

  public PiGEMapper(int codonLenght, int maxWraps, Grammar<T> grammar) {
    super(grammar);
    this.codonLenght = codonLenght;
    this.maxWraps = maxWraps;
  }    

  @Override
  public Node<T> map(BitsGenotype genotype, Map<String, Object> report) throws MappingException {
    int[] bitUsages = new int[genotype.length()];
    if (genotype.length()<codonLenght) {
      throw new MappingException(String.format("Short genotype (%d<%d)", genotype.length(), codonLenght));
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
      if ((currentCodonIndex+1)*codonLenght>genotype.length()) {
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
      //update usages
      for (int i = currentCodonIndex*codonLenght; i<(currentCodonIndex+1)*codonLenght; i++) {
        bitUsages[i] = bitUsages[i]+1;
      }
      //add children
      for (T t : options.get(optionIndex)) {
        Node<T> newChild = new Node<>(t);
        nodeToBeReplaced.getChildren().add(newChild);
      }
      currentCodonIndex = currentCodonIndex+1;
    }
    report.put(BIT_USAGES_INDEX_NAME, bitUsages);
    return tree;
  }

  @Override
  public String toString() {
    return "PiGEMapper{" + "codonLenght=" + codonLenght + ", maxWraps=" + maxWraps + '}';
  }  
     
}
