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
import it.units.malelab.ege.util.Utils;
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
    int[] bitUsages = new int[genotype.size()];
    if (genotype.size()<codonLenght) {
      throw new MappingException(String.format("Short genotype (%d<%d)", genotype.size(), codonLenght));
    }
    Node<T> tree = new Node<>(grammar.getStartingSymbol());
    int currentCodonIndex = 0;
    int wraps = 0;
    while (true) {
      List<Node<T>> nodesToBeReplaced = new ArrayList<>();
      for (Node<T> leaf : tree.leafNodes()) {
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
      /*
      //System.out.printf("i=%3d g_i^nont=%3d n_s=%3d j^nont=%3d g_i^rule=%3d |r_s|=%2d j^rule=%2d w=%2d %s:%s %s%n",
      System.out.printf("%3d & %1d & %1d & %3d & %1d & %1d & %s \\\\%n",
              //currentCodonIndex,
              genotype.slice(currentCodonIndex*codonLenght, currentCodonIndex*codonLenght+codonLenght/2).toInt(),
              nodesToBeReplaced.size(),
              nodeIndexCodon,
              genotype.slice(currentCodonIndex*codonLenght+codonLenght/2, (currentCodonIndex+1)*codonLenght).toInt(),
              options.size(),
              optionIndex,
              //wraps,
              //genotype.slice(currentCodonIndex*codonLenght, currentCodonIndex*codonLenght+codonLenght/2),
              //genotype.slice(currentCodonIndex*codonLenght+codonLenght/2, (currentCodonIndex+1)*codonLenght),
              l(Utils.contents(tree.leaves()), nodeIndexCodon)
              );
      */
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
  /*
  private String l(List<T> ts, int i) {
    StringBuilder sb = new StringBuilder();
    sb.append("\\gft{ ");
    int j = 0;
    for (T t : ts) {
      if (i==j&&grammar.getRules().keySet().contains(t)) {
        sb.append("\\textbf{");
      }
      if (grammar.getRules().keySet().contains(t)) {
        sb.append("\\bnfPiece{"+t.toString().replaceAll("[<>]", "")+"}");
      } else {
        sb.append(t.toString().replaceAll("[<>]", ""));
      }
      if (i==j&&grammar.getRules().keySet().contains(t)) {
        sb.append("}");
      }
      sb.append(" ");
      if (grammar.getRules().keySet().contains(t)) {
        j = j+1;
      }
    }
    sb.append("}");
    return sb.toString();
  }
*/

  @Override
  public String toString() {
    return "PiGEMapper{" + "codonLenght=" + codonLenght + ", maxWraps=" + maxWraps + '}';
  }  
     
}
