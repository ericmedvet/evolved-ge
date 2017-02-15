/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.mapper;

import it.units.malelab.ege.grammar.Node;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.grammar.Grammar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author danny
 */
public class DHierarchicalMapper<T> extends AbstractMapper<BitsGenotype, T> {

  Map<T, List<List<T>>> rules;
  T start;
  Map<T, List<List<T>>> terminalRules;

  public DHierarchicalMapper(Grammar<T> grammar) {
    super(grammar);
    this.start = grammar.getStartingSymbol();
    this.rules = grammar.getRules();;
    this.terminalRules = new HashMap<>(this.rules);
    calcTermPaths(this.terminalRules, new LinkedList(), this.start);
  }

  @Override
  public Node<T> map(BitsGenotype genotype, Map<String, Object> report) throws MappingException {
    Node<T> n = new Node(this.start);
    construct(this.rules, n, genotype);
    return n;
  }

  private void construct(Map<T, List<List<T>>> rules, Node<T> parent, BitsGenotype geno) throws MappingException {
    List<List<T>> choices = rules.get(parent.getContent());
    if (choices == null) {
      return;
    }
    int partsize = (int) Math.ceil((float) Math.log(choices.size()) / Math.log(2)) + 2;
    int maxvalue = (int) Math.pow(2, partsize);
    int choiceIndex = 0, j = 0;
    List<BitsGenotype> slices = geno.slices(Math.max(1, Math.floorDiv(geno.size(), partsize)));
    for (BitsGenotype g : slices) {
      choiceIndex += g.toInt();
    }
    //System.out.print(choiceIndex + " " + slices.toString());
    choiceIndex = rules.equals(terminalRules) ? Math.floorMod(choiceIndex, maxvalue) : Math.floorDiv(choiceIndex, slices.size());
    //choiceIndex = Math.floorMod(choiceIndex, maxvalue);
    ArrayList<Integer> optCount = new ArrayList<>();
    for (int i = 0; i < choices.size(); i++) {
      optCount.add(i);
      for (T x : choices.get(i)) {
        optCount.add(i);
      }
    }
    int size = optCount.size();
    List<T> chosen = choices.get(optCount.get(Math.min(size * choiceIndex / maxvalue, size - 1)));

    //int size = choices.size();
    //List<T> chosen = choices.get(Math.min(size * choiceIndex / maxvalue, size - 1));
    //System.out.println(" " + choiceIndex + " " + chosen);
    int i = 0;
    for (BitsGenotype g : geno.slices(chosen.size())) {
      Node<T> child = new Node(chosen.get(i));
      parent.getChildren().add(child);
      if (rules.get(chosen.get(i)) == null || g.size() < 3 * rules.get(chosen.get(i)).size()) {
        construct(terminalRules, child, geno);
      } else {
        construct(rules, child, g);
      }
      i++;
    }
  }

  private boolean calcTermPaths(Map<T, List<List<T>>> grammarRules, List<T> list, T currentNode) {
    if (grammarRules.get(currentNode) == null) {
      return true;
    } else if (list.contains(currentNode)) {
      return false;
    }
    list.add(currentNode);
    boolean isterminal = true;
    List<List<T>> rule = new LinkedList<>();
    for (List<T> opt : grammarRules.get(currentNode)) {
      isterminal = true;
      for (T elem : opt) {
        isterminal = isterminal & calcTermPaths(grammarRules, new LinkedList<>(list), elem);
      }
      if (isterminal) {
        rule.add(opt);
      }
    }
    if (!rule.isEmpty()) {
      grammarRules.replace(currentNode, rule);
    }
    return isterminal;
  }
}
