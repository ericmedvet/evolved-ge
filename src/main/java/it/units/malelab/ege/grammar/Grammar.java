/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.grammar;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class Grammar {
  
  public static final String RULE_ASSIGNMENT_STRING = "::=";
  public static final String RULE_OPTION_SEPARATOR_STRING = "|";
  
  private String startingSymbol;
  private Map<String, List<List<String>>> rules;

  public Grammar() {
    rules = new LinkedHashMap<>();
  }    

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, List<List<String>>> rule : rules.entrySet()) {
      sb.append(rule.getKey())
              .append(" ")
              .append(rule.getKey().equals(startingSymbol)?"*":"")
              .append(RULE_ASSIGNMENT_STRING+" ");
      for (List<String> option : rule.getValue()) {
        for (String symbol : option) {
          sb.append(symbol)
                  .append(" ");
        }
        sb.append(RULE_OPTION_SEPARATOR_STRING+" ");
      }
      sb.delete(sb.length()-2-RULE_OPTION_SEPARATOR_STRING.length(), sb.length());
      sb.append("\n");
    }
    return sb.toString();
  }

  public String getStartingSymbol() {
    return startingSymbol;
  }

  public void setStartingSymbol(String startingSymbol) {
    this.startingSymbol = startingSymbol;
  }

  public Map<String, List<List<String>>> getRules() {
    return rules;
  }
  
}
