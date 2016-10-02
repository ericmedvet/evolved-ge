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
import it.units.malelab.ege.evolver.fitness.FitnessComputer;
import it.units.malelab.ege.evolver.fitness.NumericFitness;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author eric
 */
public abstract class AbstractGenerationLogger<G extends Genotype, T> implements EvolutionListener<G, T> {

  private final FitnessComputer<T> generalizationFitnessComputer;
  protected final Set<Class<? extends EvolutionEvent>> eventClasses;
  private final Map<String, Object> constants;

  public AbstractGenerationLogger(FitnessComputer<T> generalizationFitnessComputer, Map<String, Object> constants) {
    this.generalizationFitnessComputer = generalizationFitnessComputer;
    eventClasses = new LinkedHashSet<>();
    this.constants = constants;
    eventClasses.add(GenerationEvent.class);
  }

  protected Map<String, Object> computeIndexes(int generation, List<Individual<G, T>> population) {
    Utils.sortByFitness(population);
    //diversities
    Set<G> genotypes = new HashSet<>();
    Set<Node<T>> phenotypes = new HashSet<>();
    Set<Fitness> fitnesses = new HashSet<>();
    double avgFitness = 0;
    double validFitnessCount = 0;
    for (Individual<G, T> individual : population) {
      genotypes.add(individual.getGenotype());
      phenotypes.add(individual.getPhenotype());
      fitnesses.add(individual.getFitness());
      if (individual.getFitness() instanceof NumericFitness) {
        if (!Double.isInfinite(((NumericFitness)individual.getFitness()).getValue())) {
          avgFitness = avgFitness+((NumericFitness)individual.getFitness()).getValue();
          validFitnessCount = validFitnessCount+1;
        }
      }
    }
    //invalid phenotypes
    int countInvalid = 0;
    for (Individual<G, T> individual : population) {
      if (Node.EMPTY_TREE.equals(individual.getPhenotype())) {
        countInvalid = countInvalid + 1;
      }
    }
    //generalization fitness
    Fitness<T> bestGeneralizationFitness = null;
    if (generalizationFitnessComputer != null) {
      if (Node.EMPTY_TREE.equals(population.get(0).getPhenotype())) {
        bestGeneralizationFitness = generalizationFitnessComputer.worstValue();
      } else {
        bestGeneralizationFitness = generalizationFitnessComputer.compute(population.get(0).getPhenotype());
      }
    }
    Individual<G, T> q3Individual = population.get((int) Math.ceil((double)population.size() / (double)4*(double)3));
    Individual<G, T> q2Individual = population.get((int) Math.ceil((double)population.size() / (double)4*(double)2));
    Individual<G, T> q1Individual = population.get((int) Math.ceil((double)population.size() / (double)4*(double)1));
    Individual<G, T> bestIndividual = population.get(0);
    Map<String, Object> indexes = new LinkedHashMap<>();
    indexes.putAll(constants);
    indexes.put("generation", generation);
    indexes.put("populationSize", population.size());
    indexes.put("genotypeDiversity", (double) genotypes.size() / (double) population.size());
    indexes.put("phenotypeDiversity", (double) phenotypes.size() / (double) population.size());
    indexes.put("fitnessDiversity", (double) fitnesses.size() / (double) population.size());
    indexes.put("phenotypeInvalidity", (double) countInvalid / (double) population.size());
    indexes.put("q3GenotypeSize", q3Individual.getGenotype().size());
    indexes.put("q3PhenotypeSize", q3Individual.getPhenotype().size());
    indexes.put("q3Fitness", q3Individual.getFitness().getValue());
    indexes.put("q2GenotypeSize", q2Individual.getGenotype().size());
    indexes.put("q2PhenotypeSize", q2Individual.getPhenotype().size());
    indexes.put("q2Fitness", q2Individual.getFitness().getValue());
    indexes.put("q1GenotypeSize", q1Individual.getGenotype().size());
    indexes.put("q1PhenotypeSize", q1Individual.getPhenotype().size());
    indexes.put("q1Fitness", q1Individual.getFitness().getValue());
    indexes.put("bestGenotypeSize", bestIndividual.getGenotype().size());
    indexes.put("bestPhenotypeSize", bestIndividual.getPhenotype().size());
    indexes.put("bestPhenotypeDepth", bestIndividual.getPhenotype().depth());
    indexes.put("bestPhenotypeLenght", bestIndividual.getPhenotype().leaves().size());
    indexes.put("bestFitness", bestIndividual.getFitness().getValue());
    indexes.put("meanFitness", avgFitness/validFitnessCount);
    if (generalizationFitnessComputer!=null) {
      indexes.put("generalizationFitness", bestGeneralizationFitness.getValue());
    }
    return indexes;
  }

  @Override
  public Set<Class<? extends EvolutionEvent>> getEventClasses() {
    return eventClasses;
  }

}
