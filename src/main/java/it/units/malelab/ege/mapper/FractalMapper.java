/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.Node;
import it.units.malelab.ege.grammar.Grammar;
import java.util.List;

/**
 *
 * @author eric
 */
public class FractalMapper<T> extends AbstractMapper<T> {

  private final int maxZooms;
  private int longestExpansion;

  public FractalMapper(int maxZooms, Grammar<T> grammar) {
    super(grammar);
    this.maxZooms = maxZooms;
    longestExpansion = 0;
    for (List<List<T>> rule : grammar.getRules().values()) {
      for (List<T> expansion : rule) {
        longestExpansion = Math.max(longestExpansion, expansion.size());
      }
    }
  }

  private class EnhancedSymbol<T> {

    private final T symbol;
    private final Genotype genotype;
    private final int zooms;

    public EnhancedSymbol(T symbol, Genotype genotype, int zooms) {
      this.symbol = symbol;
      this.genotype = genotype;
      this.zooms = zooms;
    }

    public T getSymbol() {
      return symbol;
    }

    public Genotype getGenotype() {
      return genotype;
    }

    public int getZooms() {
      return zooms;
    }
  }

  @Override
  public Node<T> map(Genotype genotype) throws MappingException {
    if (genotype.size() < longestExpansion) {
      throw new MappingException(String.format("Short genotype (%d<%d)", genotype.size(), longestExpansion));
    }
    Node<EnhancedSymbol<T>> enhancedTree = new Node<>(new EnhancedSymbol<>(grammar.getStartingSymbol(), genotype, 0));
    while (true) {
      Node<EnhancedSymbol<T>> nodeToBeReplaced = null;
      for (Node<EnhancedSymbol<T>> node : enhancedTree.leaves()) {
        if (grammar.getRules().keySet().contains(node.getContent().getSymbol())) {
          nodeToBeReplaced = node;
          break;
        }
      }
      if (nodeToBeReplaced == null) {
        break;
      }
      //get genotype
      T symbol = nodeToBeReplaced.getContent().getSymbol();
      int zooms = nodeToBeReplaced.getContent().getZooms();
      Genotype symbolGenotype = nodeToBeReplaced.getContent().getGenotype();
      if (zooms > maxZooms) {
        throw new MappingException(String.format("Too many zooms (%d>%d)", zooms, maxZooms));
      }
      List<List<T>> options = grammar.getRules().get(symbol);
      //get option
      List<T> symbols = chooseOption(symbolGenotype, options);
      //add children
      if (symbolGenotype.size() < symbols.size()) {
        symbolGenotype = zoomGenotype(symbolGenotype, genotype);
        zooms = zooms + 1;
      }
      for (int i = 0; i < symbols.size(); i++) {
        Node<EnhancedSymbol<T>> newChild = new Node<>(new EnhancedSymbol<>(
                symbols.get(i),
                getSlice(symbolGenotype, symbols, i),
                zooms
        ));
        nodeToBeReplaced.getChildren().add(newChild);
      }
    }
    //convert
    return extractFromEnhanced(enhancedTree);
  }

  protected Genotype getSlice(Genotype genotype, List<T> symbols, int index) {
    return getEqualSlice(genotype, symbols.size(), index);
  }

  private Genotype getEqualSlice(Genotype genotype, int pieces, int index) {
    int pieceSize = (int) Math.floor((double) genotype.size() / (double) pieces);
    int fromIndex = pieceSize * index;
    int toIndex = pieceSize * (index + 1);
    if (index == pieces - 1) {
      toIndex = genotype.size();
    }
    return genotype.slice(fromIndex, toIndex);
  }

  private Node<T> extractFromEnhanced(Node<EnhancedSymbol<T>> enhancedNode) {
    Node<T> node = new Node<>(enhancedNode.getContent().getSymbol());
    for (Node<EnhancedSymbol<T>> enhancedChild : enhancedNode.getChildren()) {
      node.getChildren().add(extractFromEnhanced(enhancedChild));
    }
    return node;
  }

  private <K> K chooseOption(Genotype genotype, List<K> options) {
    if (options.size() == 1) {
      return options.get(0);
    }
    int numberOfSlices = Math.max((int) Math.ceil(Math.log10(options.size()) / Math.log10(2d)), 2);
    if (numberOfSlices > genotype.size()) {
      return options.get(genotype.toInt() % options.size());
    }
    int value = 0;
    float threshold = (float)genotype.count()/(float)genotype.size();
    for (int i = 0; i < numberOfSlices; i++) {
      Genotype sliceGenotype = getEqualSlice(genotype, numberOfSlices, i);
      int bit = (((float) sliceGenotype.count() / (float) (sliceGenotype.size()))>=threshold)?1:0;
      value = value + bit * (int) Math.pow(2, i);
    }
    return options.get(value % options.size());
  }

  private Genotype zoomGenotype(Genotype genotype, Genotype referenceGenotype) {
    int oldAvg = Math.round(genotype.count() / genotype.size());
    genotype = referenceGenotype.slice(0, referenceGenotype.size());
    int avg = Math.round(genotype.count() / genotype.size());
    if (oldAvg != avg) {
      genotype.flip();
    }
    return genotype;
  }

}
