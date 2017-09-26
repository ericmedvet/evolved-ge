/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.listener;

import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.collector.Collector;
import it.units.malelab.ege.core.listener.event.EvolutionEvent;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import it.units.malelab.ege.util.Utils;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author eric
 */
public class CollectorGenerationLogger<G, T, F extends Fitness> extends AbstractListener<G, T, F> implements WithConstants {

  private final Map<String, Object> constants;
  private final PrintStream ps;
  private final boolean format;
  private final int headerInterval;
  private final String innerSeparator;
  private final String outerSeparator;
  private final List<Collector<G, T, F>> collectors;

  private final List<Map<String, String>> formattedNames;
  private int lines;

  public CollectorGenerationLogger(
          Map<String, Object> constants,
          PrintStream ps,
          boolean format,
          int headerInterval,
          String innerSeparator,
          String outerSeparator,
          Collector<G, T, F>... collectors) {
    super((Class) GenerationEvent.class);
    this.constants = new LinkedHashMap<>(constants);
    this.ps = ps;
    this.format = format;
    this.headerInterval = headerInterval;
    this.innerSeparator = innerSeparator;
    this.outerSeparator = outerSeparator;
    this.collectors = Arrays.asList(collectors);
    formattedNames = new ArrayList<>(this.collectors.size());
    for (Collector<G, T, F> collector : collectors) {
      Map<String, String> localFormattedNames = new LinkedHashMap<>();
      for (String name : collector.getFormattedNames().keySet()) {
        localFormattedNames.put(name, Utils.formatName(name, collector.getFormattedNames().get(name), format));
      }
      formattedNames.add(localFormattedNames);
    }
    lines = 0;
  }

  @Override
  public void listen(EvolutionEvent<G, T, F> event) {
    int generation = ((GenerationEvent) event).getGeneration();
    if ((headerInterval == 0 && lines == 0) || ((headerInterval > 0) && ((generation - 1) % headerInterval == 0))) {
      //print header: generation
      ps.print(format ? "gen" : "generation");
      ps.print(outerSeparator);
      //print header: constants
      int k = 0;
      for (String name : constants.keySet()) {
        ps.print(Utils.pad(name, constants.getOrDefault(name, "").toString().length(), format));
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
    //print values: generation
    ps.print(format ? String.format("%3d", generation) : generation);
    ps.print(outerSeparator);
    //print values: constants
    int k = 0;
    for (String name : constants.keySet()) {
      ps.print(Utils.pad(constants.get(name).toString(), name.length(), format));
      if (k != constants.size() - 1) {
        ps.print(innerSeparator);
      }
      k = k + 1;
    }
    if (k > 0) {
      ps.print(outerSeparator);
    }
    //print values: collectors
    for (int i = 0; i < formattedNames.size(); i++) {
      int j = 0;
      Map<String, Object> values = collectors.get(i).collect((GenerationEvent)event);
      for (String name : formattedNames.get(i).keySet()) {
        if (format) {
          String value = String.format(collectors.get(i).getFormattedNames().get(name), values.get(name));
          ps.print(Utils.pad(value, formattedNames.get(i).get(name).length(), format));
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
  public void updateConstants(Map<String, Object> newConstants) {
    for (String key : constants.keySet()) {
      if (newConstants.containsKey(key)) {
        constants.put(key, newConstants.get(key));
      }
    }
  }

}
