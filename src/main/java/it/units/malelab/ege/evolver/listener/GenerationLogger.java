/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.Node;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.Utils;
import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.evolver.fitness.Fitness;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author eric
 */
public class GenerationLogger<G extends Genotype, T> implements EvolutionListener<G, T> {

  private final PrintStream ps;
  private final String fitnessFormat;
  private final int lengthOfFitnessFormat;
  private final Set<Class<? extends EvolutionEvent>> eventClasses;

  private final static int GEN_STEP = 10;

  public GenerationLogger(PrintStream ps, String fitnessFormat, int lengthOfFitnessFormat) {
    this.ps = ps;
    this.fitnessFormat = fitnessFormat;
    this.lengthOfFitnessFormat = lengthOfFitnessFormat;
    eventClasses = new LinkedHashSet<>();
    eventClasses.add(GenerationEvent.class);
  }

  @Override
  public void listen(EvolutionEvent<G, T> event) {
    List<Individual<G, T>> population = new ArrayList<>(((GenerationEvent) event).getPopulation());
    Utils.sortByFitness(population);
    if (event.getGeneration() % GEN_STEP == 0) {
      ps.printf("%4s %4s | %4s %4s %4s | %4s %4s %"+lengthOfFitnessFormat+"s | %4s %4s %"+lengthOfFitnessFormat+"s | %4s %4s %4s %4s %"+lengthOfFitnessFormat+"s%n",
              "Gen.", "Pop.",
              "D-G", "D-P", "D-F",
              "50Gs", "50Ps", "50F.",
              "25Gs", "25Ps", "25F.",
              "BGs", "BPd", "BPs", "BPl", "Fitness"
      );
    }
    Set<G> genotypes = new HashSet<>();
    Set<Node<T>> phenotypes = new HashSet<>();
    Set<Fitness> fitnesses = new HashSet<>();
    for (Individual<G, T> individual : population) {
      genotypes.add(individual.getGenotype());
      phenotypes.add(individual.getPhenotype());
      fitnesses.add(individual.getFitness());
    }
    ps.printf("%4d %4d | %4.2f %4.2f %4.2f | %4d %4d " + fitnessFormat + " | %4d %4d " + fitnessFormat + " | %4d %4d %4d %4d " + fitnessFormat + " | %s%n",
            event.getGeneration(),
            population.size(),
            (double)genotypes.size()/(double)population.size(),
            (double)phenotypes.size()/(double)population.size(),
            (double)fitnesses.size()/(double)population.size(),
            population.get((int)Math.ceil(population.size()/2)).getGenotype().size(),
            population.get((int)Math.ceil(population.size()/2)).getPhenotype().size(),
            population.get((int)Math.ceil(population.size()/2)).getFitness().getValue(),
            population.get((int)Math.ceil(population.size()/4)).getGenotype().size(),
            population.get((int)Math.ceil(population.size()/4)).getPhenotype().size(),
            population.get((int)Math.ceil(population.size()/4)).getFitness().getValue(),
            population.get(0).getGenotype().size(),
            population.get(0).getPhenotype().depth(),
            population.get(0).getPhenotype().size(),
            population.get(0).getPhenotype().leaves().size(),
            population.get(0).getFitness().getValue(),
            Utils.contents(population.get(0).getPhenotype().leaves())
    );
  }

  @Override
  public Set<Class<? extends EvolutionEvent>> getEventClasses() {
    return eventClasses;
  }

}
