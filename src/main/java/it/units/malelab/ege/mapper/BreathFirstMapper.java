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
public class BreathFirstMapper extends AbstractMapper {

  private final int codonLenght;
  private final int maxWraps;

  public BreathFirstMapper(int codonLenght, int maxWraps, Grammar grammar) {
    super(grammar);
    this.codonLenght = codonLenght;
    this.maxWraps = maxWraps;
  }

  private class EnhancedSymbol {

    private final String symbol;
    private final int depth;

    public EnhancedSymbol(String symbol, int depth) {
      this.symbol = symbol;
      this.depth = depth;
    }

    public String getSymbol() {
      return symbol;
    }

    public int getDepth() {
      return depth;
    }

  }

  @Override
  public List<String> map(Genotype genotype) throws MappingException {
    List<EnhancedSymbol> enhancedSymbols = new ArrayList<>();
    enhancedSymbols.add(new EnhancedSymbol(grammar.getStartingSymbol(), 0));
    int currentCodonIndex = 0;
    int wraps = 0;
    while (true) {
      int toReplaceSymbolIndex = -1;
      int minDepth = Integer.MAX_VALUE;
      for (int i = 0; i < enhancedSymbols.size(); i++) {
        if (grammar.getRules().keySet().contains(enhancedSymbols.get(i).getSymbol()) && (enhancedSymbols.get(i).getDepth() < minDepth)) {
          toReplaceSymbolIndex = i;
          minDepth = enhancedSymbols.get(i).getDepth();
        }
      }
      if (toReplaceSymbolIndex == -1) {
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
      List<List<String>> options = grammar.getRules().get(enhancedSymbols.get(toReplaceSymbolIndex).getSymbol());
      int optionIndex = genotype.slice(currentCodonIndex * codonLenght, (currentCodonIndex + 1) * codonLenght).toInt() % options.size();
      //replace
      List<EnhancedSymbol> tailEnhancedSymbols = new ArrayList<>();
      if (toReplaceSymbolIndex < enhancedSymbols.size() - 1) {
        tailEnhancedSymbols.addAll(enhancedSymbols.subList(toReplaceSymbolIndex + 1, enhancedSymbols.size()));
      }
      enhancedSymbols = enhancedSymbols.subList(0, toReplaceSymbolIndex);
      for (String string : options.get(optionIndex)) {
        enhancedSymbols.add(new EnhancedSymbol(string, minDepth+1));
      }
      enhancedSymbols.addAll(tailEnhancedSymbols);
      currentCodonIndex = currentCodonIndex + 1;
    }
    //convert
    List<String> program = new ArrayList<>(enhancedSymbols.size());
    for (EnhancedSymbol enhancedSymbol : enhancedSymbols) {
      program.add(enhancedSymbol.getSymbol());
    }
    return program;
  }

}
