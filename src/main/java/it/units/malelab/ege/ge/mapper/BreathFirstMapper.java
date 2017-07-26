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
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class BreathFirstMapper<T> extends AbstractMapper<BitsGenotype, T> {

  private final int codonLenght;
  private final int maxWraps;

  public BreathFirstMapper(int codonLenght, int maxWraps, Grammar<T> grammar) {
    super(grammar);
    this.codonLenght = codonLenght;
    this.maxWraps = maxWraps;
  }

  private class EnhancedSymbol<T> {

    private final T symbol;
    private final int depth;

    public EnhancedSymbol(T symbol, int depth) {
      this.symbol = symbol;
      this.depth = depth;
    }

    public T getSymbol() {
      return symbol;
    }

    public int getDepth() {
      return depth;
    }

    @Override
    public String toString() {
      return symbol+":"+depth;
    }

  }

  @Override
  public Node<T> map(BitsGenotype genotype, Map<String, Object> report) throws MappingException {
    int[] bitUsages = new int[genotype.size()];
    if (genotype.size()<codonLenght) {
      throw new MappingException(String.format("Short genotype (%d<%d)", genotype.size(), codonLenght));
    }
    Node<EnhancedSymbol<T>> enhancedTree = new Node<>(new EnhancedSymbol<>(grammar.getStartingSymbol(), 0));
    int currentCodonIndex = 0;
    int wraps = 0;
    while (true) {
      int minDepth = Integer.MAX_VALUE;
      Node<EnhancedSymbol<T>> nodeToBeReplaced = null;
      for (Node<EnhancedSymbol<T>> node : enhancedTree.leaves()) {
        if (grammar.getRules().keySet().contains(node.getContent().getSymbol())&&(node.getContent().getDepth()<minDepth)) {
          nodeToBeReplaced = node;
          minDepth = node.getContent().getDepth();
        }
      }
      if (nodeToBeReplaced==null) {
        break;
      }
      //get codon index and option
      if ((currentCodonIndex + 1) * codonLenght > genotype.size()) {
        wraps = wraps + 1;
        currentCodonIndex = 0;
        if (wraps > maxWraps) {
          throw new MappingException(String.format("Too many wraps (%d>%d)", wraps, maxWraps));
        }
      }
      List<List<T>> options = grammar.getRules().get(nodeToBeReplaced.getContent().getSymbol());
      int optionIndex = genotype.slice(currentCodonIndex * codonLenght, (currentCodonIndex + 1) * codonLenght).toInt() % options.size();
      //update usages
      for (int i = currentCodonIndex*codonLenght; i<(currentCodonIndex+1)*codonLenght; i++) {
        bitUsages[i] = bitUsages[i]+1;
      }
      //add children
      for (T t : options.get(optionIndex)) {
        Node<EnhancedSymbol<T>> newChild = new Node<>(new EnhancedSymbol<>(t, nodeToBeReplaced.getContent().getDepth()+1));
        nodeToBeReplaced.getChildren().add(newChild);
      }
      currentCodonIndex = currentCodonIndex+1;
    }
    report.put(BIT_USAGES_INDEX_NAME, bitUsages);
    //convert
    return extractFromEnhanced(enhancedTree);
  }
  
  private Node<T> extractFromEnhanced(Node<EnhancedSymbol<T>> enhancedNode) {
    Node<T> node = new Node<>(enhancedNode.getContent().getSymbol());
    for (Node<EnhancedSymbol<T>> enhancedChild : enhancedNode.getChildren()) {
      node.getChildren().add(extractFromEnhanced(enhancedChild));
    }
    return node;
  }

  @Override
  public String toString() {
    return "BreathFirstMapper{" + "codonLenght=" + codonLenght + ", maxWraps=" + maxWraps + '}';
  }

}
