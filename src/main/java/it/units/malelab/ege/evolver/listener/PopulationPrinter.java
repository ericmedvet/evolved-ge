/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.Genotype;
import it.units.malelab.ege.core.grammar.Node;
import it.units.malelab.ege.ge.mapper.StandardGEMapper;
import it.units.malelab.ege.util.Utils;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eric
 */
public class PopulationPrinter<T> implements EvolutionListener<BitsGenotype, T> {
  
  private final Set<Class<? extends EvolutionEvent>> eventClasses;
  private final PrintStream ps;

  private static final char[][] CHARS = {{'O', 'I'}, {'.', ','}};

  public PopulationPrinter(PrintStream ps) {
    eventClasses = new LinkedHashSet<>();
    eventClasses.add(GenerationEvent.class);
    this.ps = ps;
  }

  @Override
  public void listen(EvolutionEvent<BitsGenotype, T> event) {
    int generation = ((GenerationEvent) event).getGeneration();
    List<Individual<BitsGenotype, T>> population = new ArrayList<>(((GenerationEvent) event).getPopulation());
    ps.printf("Population at generation %d%n", generation);
    for (Individual<BitsGenotype, T> individual : population) {
      //genotype
      int[] bitUsages = (int[]) individual.getOtherInfo().get(StandardGEMapper.BIT_USAGES_INDEX_NAME);
      if (bitUsages==null) {
        bitUsages = new int[individual.getGenotype().size()];
      }
      for (int i = 0; i<individual.getGenotype().size(); i++) {
        ps.print(CHARS[bitUsages[i]>0?0:1][individual.getGenotype().get(i)?0:1]);
      }
      ps.printf(" -> %s%n", Utils.contents(individual.getPhenotype().leaves()));
    }
  }
  
  @Override
  public Set<Class<? extends EvolutionEvent>> getEventClasses() {
    return eventClasses;
  }
  
}
