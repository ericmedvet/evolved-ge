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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class ScreenGenerationLogger<G extends Genotype, T> extends AbstractGenerationLogger<G, T> {

  private final String fitnessFormat;
  private final int lengthOfFitnessFormat;
  private final PhenotypePrinter<T> phenotypePrinter;

  private final static int GEN_STEP = 10;

  public ScreenGenerationLogger(String fitnessFormat, int lengthOfFitnessFormat, PhenotypePrinter<T> phenotypePrinter, FitnessComputer<T> generalizationFitnessComputer, String prefix) {
    super(generalizationFitnessComputer, prefix);
    this.fitnessFormat = fitnessFormat;
    this.lengthOfFitnessFormat = lengthOfFitnessFormat;
    this.phenotypePrinter = phenotypePrinter;
  }

  @Override
  public void listen(EvolutionEvent<G, T> event) {
    List<Individual<G, T>> population = new ArrayList<>(((GenerationEvent) event).getPopulation());
    int generation = ((GenerationEvent) event).getGeneration();
    if (generation % GEN_STEP == 0) {
      System.out.printf("%4s %4s | %4s %4s %4s | %4s | %4s %4s %" + lengthOfFitnessFormat + "s | %4s %4s %" + lengthOfFitnessFormat + "s | %4s %4s %" + lengthOfFitnessFormat + "s | %4s %4s %4s %4s %" + lengthOfFitnessFormat + "s | %" + lengthOfFitnessFormat + "s %" + lengthOfFitnessFormat + "s | %s%n",
              "gen", "popS",
              "divG", "divP", "divF",
              "invP",
              "q3Gs", "q3Ps", "q3F",
              "q2Gs", "q2Ps", "q2F",
              "q1Gs", "q1Ps", "q1F",
              "bGs", "bPs", "bPd", "bPl", "bF",
              "genF",
              "avgF",
              "Best phenotype"
      );
    }
    Map<String, Object> indexes = computeIndexes(generation, population);
    if (phenotypePrinter!=null) {
      indexes.put("bestPhenotype", phenotypePrinter.toString(population.get(0).getPhenotype()));
    }
    System.out.printf("%4d %4d | %4.2f %4.2f %4.2f | %4.2f | %4d %4d " + fitnessFormat + " | %4d %4d " + fitnessFormat + " | %4d %4d " + fitnessFormat + " | %4d %4d %4d %4d " + fitnessFormat + " | " + fitnessFormat + " " + fitnessFormat + " | %s%n",
            indexes.get("generation"),
            indexes.get("populationSize"),
            indexes.get("genotypeDiversity"),
            indexes.get("phenotypeDiversity"),
            indexes.get("fitnessDiversity"),
            indexes.get("phenotypeInvalidity"),
            indexes.get("q3GenotypeSize"),
            indexes.get("q3PhenotypeSize"),
            indexes.get("q3Fitness"),
            indexes.get("q2GenotypeSize"),
            indexes.get("q2PhenotypeSize"),
            indexes.get("q2Fitness"),
            indexes.get("q1GenotypeSize"),
            indexes.get("q1PhenotypeSize"),
            indexes.get("q1Fitness"),
            indexes.get("bestGenotypeSize"),
            indexes.get("bestPhenotypeSize"),
            indexes.get("bestPhenotypeDepth"),
            indexes.get("bestPhenotypeLenght"),
            indexes.get("bestFitness"),
            indexes.get("generalizationFitness"),
            indexes.get("meanFitness"),
            indexes.get("bestPhenotype")
    );
  }

}
