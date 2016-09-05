/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.grammar.Grammar;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 *
 * @author eric
 */
public class Utils {
  
  public static Grammar parseFromFile(File file) throws FileNotFoundException, IOException {
    Grammar grammar = new Grammar();
    BufferedReader br = new BufferedReader(new FileReader(file));
    String line;
    while ((line = br.readLine())!=null) {
      String[] components = line.split(Pattern.quote(Grammar.RULE_ASSIGNMENT_STRING));
      String toReplaceSymbol = components[0].trim();
      String[] optionStrings = components[1].split(Pattern.quote(Grammar.RULE_OPTION_SEPARATOR_STRING));
      if (grammar.getStartingSymbol()==null) {
        grammar.setStartingSymbol(toReplaceSymbol);
      }
      List<List<String>> options = new ArrayList<>();
      for (String optionString : optionStrings) {
        List<String> symbols = new ArrayList<>();
        for (String symbol : optionString.split("\\s+")) {
          if (!symbol.trim().isEmpty()) {
            symbols.add(symbol.trim());
          }
        }
        if (!symbols.isEmpty()) {
          options.add(symbols);
        }
      }
      grammar.getRules().put(toReplaceSymbol, options);
    }
    br.close();
    return grammar;
  }
  
  public static Genotype randomGenotype(int size, Random random) {
    Genotype g = new Genotype(size);
    for (int i = 0; i<g.size(); i++) {
      g.set(i, random.nextBoolean());
    }
    return g;
  }
  
  public static String bitSetToString(BitSet bs, int size) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < size; i++) {
      sb.append(bs.get(i) ? '1' : '0');
    }
    return sb.toString();
  }
  
}
