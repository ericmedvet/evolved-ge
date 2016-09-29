/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.PhenotypePrinter;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.evolver.fitness.FitnessComputer;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author eric
 */
public class ScreenGenerationLogger<G extends Genotype, T> extends AbstractGenerationLogger<G, T> {

  private final String fitnessFormat;
  private final int lengthOfFitnessFormat;
  private final PhenotypePrinter<T> phenotypePrinter;

  private final static int GEN_STEP = 10;

  public ScreenGenerationLogger(String fitnessFormat, int lengthOfFitnessFormat, PhenotypePrinter<T> phenotypePrinter, FitnessComputer<T> generalizationFitnessComputer) {
    super(generalizationFitnessComputer);
    this.fitnessFormat = fitnessFormat;
    this.lengthOfFitnessFormat = lengthOfFitnessFormat;
    this.phenotypePrinter = phenotypePrinter;
  }

  @Override
  public void listen(EvolutionEvent<G, T> event) {
    List<Individual<G, T>> population = new ArrayList<>(((GenerationEvent) event).getPopulation());
    int generation = ((GenerationEvent) event).getGeneration();
    if (generation % GEN_STEP == 0) {
      System.out.printf("%4s %4s | %4s %4s %4s | %4s | %4s %4s %" + lengthOfFitnessFormat + "s | %4s %4s %" + lengthOfFitnessFormat + "s | %4s %4s %4s %4s %" + lengthOfFitnessFormat + "s | %" + lengthOfFitnessFormat + "s | %s%n",
              "Gen.", "Pop.",
              "D-G", "D-P", "D-F",
              "N-P",
              "50Gs", "50Ps", "50F",
              "25Gs", "25Ps", "25F",
              "B-Gs", "B-Pd", "B-Ps", "B-Pl", "B-F",
              "B-GF",
              "Best phenotype"
      );
    }
    Object[] numbers = getNumbers(generation, population);
    Object[] data = new Object[numbers.length+1];
    if (phenotypePrinter!=null) {
      data[data.length-1] = phenotypePrinter.toString(population.get(0).getPhenotype());
    }
    System.arraycopy(numbers, 0, data, 0, numbers.length);
    System.out.printf("%4d %4d | %4.2f %4.2f %4.2f | %4.2f | %4d %4d " + fitnessFormat + " | %4d %4d " + fitnessFormat + " | %4d %4d %4d %4d " + fitnessFormat + " | " + fitnessFormat + " | %s%n",
            data
    );
  }

}
