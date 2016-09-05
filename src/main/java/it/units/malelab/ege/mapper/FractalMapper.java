/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.grammar.Grammar;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 *
 * @author eric
 */
public class FractalMapper extends AbstractMapper {

  private final int maxZooms;

  public FractalMapper(int maxZooms, Grammar grammar) {
    super(grammar);
    this.maxZooms = maxZooms;
  }

  private class EnhancedSymbol {

    private final String symbol;
    private final Genotype genotype;
    private int zooms;

    public EnhancedSymbol(String symbol, Genotype genotype, int zooms) {
      this.symbol = symbol;
      this.genotype = genotype;
      this.zooms = zooms;
    }

    public String getSymbol() {
      return symbol;
    }

    public Genotype getGenotype() {
      return genotype;
    }

    public int getZooms() {
      return zooms;
    }

    @Override
    public String toString() {
      return "EnhancedSymbol{" + "symbol=" + symbol + ", genotype=" + genotype + ", zooms=" + zooms + '}';
    }

  }

  @Override
  public List<String> map(Genotype genotype) throws MappingException {
    List<EnhancedSymbol> enhancedSymbols = new ArrayList<>();
    enhancedSymbols.add(new EnhancedSymbol(grammar.getStartingSymbol(), genotype, 0));
    while (true) {
      int toReplaceSymbolIndex = -1;
      for (int i = 0; i < enhancedSymbols.size(); i++) {
        if (grammar.getRules().keySet().contains(enhancedSymbols.get(i).getSymbol())) {
          toReplaceSymbolIndex = i;
          break;
        }
      }
      if (toReplaceSymbolIndex == -1) {
        break;
      }
      //get genotype
      String symbol = enhancedSymbols.get(toReplaceSymbolIndex).getSymbol();
      int zooms = enhancedSymbols.get(toReplaceSymbolIndex).getZooms();
      Genotype symbolGenotype = enhancedSymbols.get(toReplaceSymbolIndex).getGenotype();
      if (zooms > maxZooms) {
        throw new MappingException(String.format("Too many zooms (%d>%d)", zooms, maxZooms));
      }
      List<List<String>> options = grammar.getRules().get(symbol);
      if (options.size() > symbolGenotype.size()) {
        int oldAvg = Math.round(symbolGenotype.count() / symbolGenotype.size());
        symbolGenotype = genotype.slice(0, genotype.size());
        int avg = Math.round(symbolGenotype.count() / symbolGenotype.size());
        if (oldAvg != avg) {
          symbolGenotype.flip();
        }
        zooms = zooms + 1;
      }
      //get option index
      //TODO replace with coarse binaryToIndex instead of max of portion (which allows for tie, and hence favors one options)
      float maxValue = 0;
      int optionIndex = 0;
      for (int i = 0; i < options.size(); i++) {
        Genotype sliceGenotype = getSlice(symbolGenotype, options.size(), i);
        float value = (float) sliceGenotype.count() / (float) (sliceGenotype.size());
        if (value >= maxValue) {
          optionIndex = i;
          maxValue = value;
        }
      }
      //replace
      List<EnhancedSymbol> tailEnhancedSymbols = new ArrayList<>();
      if (toReplaceSymbolIndex < enhancedSymbols.size() - 1) {
        tailEnhancedSymbols.addAll(enhancedSymbols.subList(toReplaceSymbolIndex + 1, enhancedSymbols.size()));
      }
      enhancedSymbols = enhancedSymbols.subList(0, toReplaceSymbolIndex);
      for (int i = 0; i < options.get(optionIndex).size(); i++) {
        enhancedSymbols.add(new EnhancedSymbol(options.get(optionIndex).get(i), getSlice(symbolGenotype, options.get(optionIndex).size(), i), zooms));
      }
      enhancedSymbols.addAll(tailEnhancedSymbols);
    }
    //convert
    List<String> program = new ArrayList<>(enhancedSymbols.size());
    for (EnhancedSymbol enhancedSymbol : enhancedSymbols) {
      program.add(enhancedSymbol.getSymbol());
    }
    return program;
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

}
