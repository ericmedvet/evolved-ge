/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.listener.collector.PopulationInfoCollector;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author eric
 */
public class CollectorGenerationLogger<G extends Genotype, T> implements EvolutionListener<G, T> {

  private final Set<Class<? extends EvolutionEvent>> eventClasses;
  private final Map<String, Object> constants;
  private final PrintStream ps;
  private final boolean format;
  private final int headerInterval;
  private final String innerSeparator;
  private final String outerSeparator;
  private final List<PopulationInfoCollector<G, T>> collectors;

  private final List<Map<String, String>> formattedNames;
  private int lines;

  public CollectorGenerationLogger(
          Map<String, Object> constants,
          PrintStream ps,
          boolean format,
          int headerInterval,
          String innerSeparator,
          String outerSeparator,
          PopulationInfoCollector<G, T>... collectors) {
    eventClasses = new LinkedHashSet<>();
    eventClasses.add(GenerationEvent.class);
    this.constants = new LinkedHashMap<>(constants);
    this.ps = ps;
    this.format = format;
    this.headerInterval = headerInterval;
    this.innerSeparator = innerSeparator;
    this.outerSeparator = outerSeparator;
    this.collectors = Arrays.asList(collectors);
    formattedNames = new ArrayList<>(this.collectors.size());
    for (PopulationInfoCollector<G, T> collector : collectors) {
      Map<String, String> localFormattedNames = new LinkedHashMap<>();
      for (String name : collector.getFormattedNames().keySet()) {
        localFormattedNames.put(name, formatName(name, collector.getFormattedNames().get(name)));
      }
      formattedNames.add(localFormattedNames);
    }
    lines = 0;
  }

  @Override
  public void listen(EvolutionEvent<G, T> event) {
    int generation = ((GenerationEvent) event).getGeneration();
    List<Individual<G, T>> population = new ArrayList<>(((GenerationEvent) event).getPopulation());
    if ((headerInterval == 0 && lines == 0) || ((headerInterval > 0)&&((generation - 1) % headerInterval == 0))) {
      //print header: generation
      ps.print(format ? "gen" : "generation");
      ps.print(outerSeparator);
      //print header: constants
      int k = 0;
      for (String name : constants.keySet()) {
        ps.print(pad(name, constants.get(name).toString().length()));
        if (k != constants.size() - 1) {
          ps.print(innerSeparator);
        }
        k = k + 1;
      }
      if (k > 0) {
        ps.print(outerSeparator);
      }
      //print header: collectors
      for (int i = 0; i < formattedNames.size(); i++) {
        int j = 0;
        for (String name : formattedNames.get(i).keySet()) {
          ps.print(formattedNames.get(i).get(name));
          if (j != formattedNames.get(i).size() - 1) {
            ps.print(innerSeparator);
          }
          j = j + 1;
        }
        if (i != formattedNames.size() - 1) {
          ps.print(outerSeparator);
        }
      }
      ps.println();
    }
    //print header: generation
    ps.print(format ? String.format("%3d", generation) : generation);
    ps.print(outerSeparator);
    //print header: constants
    int k = 0;
    for (String name : constants.keySet()) {
      ps.print(pad(constants.get(name).toString(), name.length()));
      if (k != constants.size() - 1) {
        ps.print(innerSeparator);
      }
      k = k + 1;
    }
    if (k > 0) {
      ps.print(outerSeparator);
    }
    //print header: collectors
    for (int i = 0; i < formattedNames.size(); i++) {
      int j = 0;
      Map<String, Object> values = collectors.get(i).collect(population);
      for (String name : formattedNames.get(i).keySet()) {
        if (format) {
          String value = String.format(collectors.get(i).getFormattedNames().get(name), values.get(name));
          ps.print(pad(value, formattedNames.get(i).get(name).length()));
        } else {
          ps.print(values.get(name));
        }
        if (j != formattedNames.get(i).size() - 1) {
          ps.print(innerSeparator);
        }
        j = j + 1;
      }
      if (i != formattedNames.size() - 1) {
        ps.print(outerSeparator);
      }
    }
    ps.println();
    lines = lines + 1;
  }

  @Override
  public Set<Class<? extends EvolutionEvent>> getEventClasses() {
    return eventClasses;
  }

  private String formatName(String name, String format) {
    if (!this.format) {
      return name;
    }
    String acronym = "";
    String[] pieces = name.split("\\.");
    for (String piece : pieces) {
      acronym = acronym + piece.substring(0, 1);
    }
    Matcher matcher = Pattern.compile("\\d++").matcher(format);
    if (matcher.find()) {
      int size = Integer.parseInt(matcher.group());
      acronym = pad(acronym, size);
    }
    return acronym;
  }

  private String pad(String s, int length) {
    while (format && (s.length() < length)) {
      s = " " + s;
    }
    return s;
  }

  public void updateConstants(Map<String, Object> newConstants) {
    for (String key : constants.keySet()) {
      if (newConstants.containsKey(key)) {
        constants.put(key, newConstants.get(key));
      }
    }
  }

}
