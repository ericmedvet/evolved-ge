/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import it.units.malelab.ege.Genotype;
import it.units.malelab.ege.Utils;
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

  @Override
  public List<String> map(Genotype genotype) throws MappingException {
    List<String> program = new ArrayList<>();
    List<Integer> dephts = new ArrayList<>();
    program.add(grammar.getStartingSymbol());
    dephts.add(0);
    int currentCodonIndex = 0;
    int wraps = 0;
    while (true) {
      int toReplaceSymbolIndex = -1;
      int minDepth = Integer.MAX_VALUE;
      for (int i = 0; i<program.size(); i++) {
        if (grammar.getRules().keySet().contains(program.get(i))&&(dephts.get(i)<minDepth)) {
          toReplaceSymbolIndex = i;
          minDepth = dephts.get(i);
        }
      }
      if (toReplaceSymbolIndex==-1) {
        break;
      }
      //get codon
      if ((currentCodonIndex+1)*codonLenght>genotype.size()) {
        wraps = wraps+1;
        currentCodonIndex = 0;
        if (wraps>maxWraps) {
          throw new MappingException(String.format("Too many wraps (%d>%d)", wraps, maxWraps));
        }
      }
      BitSet codon = genotype.get(currentCodonIndex*codonLenght, (currentCodonIndex+1)*codonLenght);
      //get option
      List<List<String>> options = grammar.getRules().get(program.get(toReplaceSymbolIndex));
      int optionIndex = codonToInt(codon)%options.size();
      //replace
      List<String> tailProgram = new ArrayList<>();
      List<Integer> tailDepths = new ArrayList<>();
      if (toReplaceSymbolIndex<program.size()-1) {
        tailProgram.addAll(program.subList(toReplaceSymbolIndex+1, program.size()));
        tailDepths.addAll(dephts.subList(toReplaceSymbolIndex+1, program.size()));
      }
      program = program.subList(0, toReplaceSymbolIndex);
      program.addAll(options.get(optionIndex));
      program.addAll(tailProgram);
      dephts = dephts.subList(0, toReplaceSymbolIndex);
      for (String symbol : options.get(optionIndex)) {
        dephts.add(minDepth+1);
      }
      dephts.addAll(tailDepths);
      currentCodonIndex = currentCodonIndex+1;
    }
    return program;
  }
  
  private int codonToInt(BitSet bs) {
    if (bs.length()==0) {
      return 0;
    } else {
      return (int)bs.toLongArray()[0];
    }
  }
    
}
