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

  public FractalMapper(int maxZooms, Grammar<T> grammar) {
    super(grammar);
    this.maxZooms = maxZooms;
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
    Node<EnhancedSymbol<T>> enhancedTree = new Node<>(new EnhancedSymbol<>(grammar.getStartingSymbol(), genotype, 0));
    while (true) {
      Node<EnhancedSymbol<T>> nodeToBeReplaced = null;
      for (Node<EnhancedSymbol<T>> node : enhancedTree.flatLeaves()) {
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
      if (symbolGenotype.size()<symbols.size()) {
        symbolGenotype = zoomGenotype(symbolGenotype, genotype);
        zooms = zooms+1;
      }
      for (int i = 0; i < symbols.size(); i++) {
        Node<EnhancedSymbol<T>> newChild = new Node<>(new EnhancedSymbol<>(
                symbols.get(i),
                getSlice(symbolGenotype, symbols.size(), i),
                zooms
        ));
        nodeToBeReplaced.getChildren().add(newChild);
      }
    }
    //convert
    return extractFromEnhanced(enhancedTree);
  }

  private Genotype getSlice(Genotype genotype, int pieces, int index) {
    int pieceSize = (int) Math.floor(genotype.size() / pieces);
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
    int numberOfSlices = (int) Math.ceil(Math.log10(options.size()) / Math.log10(2d));
    if (numberOfSlices > genotype.size()) {
      return options.get(genotype.toInt() % options.size());
    }
    int value = 0;
    for (int i = 0; i < numberOfSlices; i++) {
      Genotype sliceGenotype = getSlice(genotype, numberOfSlices, i);
      int bit = (int) Math.round((float) sliceGenotype.count() / (float) (sliceGenotype.size()));
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
