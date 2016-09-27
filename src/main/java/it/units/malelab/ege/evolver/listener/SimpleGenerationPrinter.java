/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.Utils;
import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author eric
 */
public class SimpleGenerationPrinter<G extends Genotype, T> implements EvolutionListener<G, T> {

  private final PrintStream ps;
  private final String format;
  private final Set<Class<? extends EvolutionEvent>> eventClasses;

  public SimpleGenerationPrinter(PrintStream ps, String format) {
    this.ps = ps;
    this.format = format;
    eventClasses = new LinkedHashSet<>();
    eventClasses.add(GenerationEvent.class);
  }

  @Override
  public void listen(EvolutionEvent<G, T> event) {
    List<Individual<G, T>> population = new ArrayList<>(((GenerationEvent)event).getPopulation());
    Utils.sortByFitness(population);
    ps.printf(format,
            event.getGeneration(),
            population.size(),
            population.get(0).getFitness().getValue(),
            population.get(0).getGenotype().size(),
            Utils.contents(population.get(0).getPhenotype().leaves())
            );
  }

  @Override
  public Set<Class<? extends EvolutionEvent>> getEventClasses() {
    return eventClasses;
  }

}
