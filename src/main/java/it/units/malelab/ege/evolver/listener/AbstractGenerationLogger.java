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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author eric
 */
public abstract class AbstractGenerationLogger<G extends Genotype, T> implements EvolutionListener<G, T> {

  private final FitnessComputer<T> generalizationFitnessComputer;
  private final Set<Class<? extends EvolutionEvent>> eventClasses;

  public AbstractGenerationLogger(FitnessComputer<T> generalizationFitnessComputer) {
    this.generalizationFitnessComputer = generalizationFitnessComputer;
    eventClasses = new LinkedHashSet<>();
    eventClasses.add(GenerationEvent.class);
  }

  protected Object[] getNumbers(int generation, List<Individual<G, T>> population) {
    boolean nf = population.get(0).getFitness() instanceof NumericFitness;
    Utils.sortByFitness(population);
    //diversities
    Set<G> genotypes = new HashSet<>();
    Set<Node<T>> phenotypes = new HashSet<>();
    Set<Fitness> fitnesses = new HashSet<>();
    for (Individual<G, T> individual : population) {
      genotypes.add(individual.getGenotype());
      phenotypes.add(individual.getPhenotype());
      fitnesses.add(individual.getFitness());
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
      bestGeneralizationFitness = generalizationFitnessComputer.compute(population.get(0).getPhenotype());
    }
    Object[] numbers = new Object[]{
      generation,
      population.size(),
      (double) genotypes.size() / (double) population.size(),
      (double) phenotypes.size() / (double) population.size(),
      (double) fitnesses.size() / (double) population.size(),
      (double) countInvalid / (double) population.size(),
      population.get((int) Math.ceil(population.size() / 2)).getGenotype().size(),
      population.get((int) Math.ceil(population.size() / 2)).getPhenotype()==null?null:population.get((int) Math.ceil(population.size() / 2)).getPhenotype().size(),
      population.get((int) Math.ceil(population.size() / 2)).getFitness().getValue(),
      population.get((int) Math.ceil(population.size() / 4)).getGenotype().size(),
      population.get((int) Math.ceil(population.size() / 2)).getPhenotype()==null?null:population.get((int) Math.ceil(population.size() / 4)).getPhenotype().size(),
      population.get((int) Math.ceil(population.size() / 4)).getFitness().getValue(),
      population.get(0).getGenotype().size(),
      population.get(0).getPhenotype()==null?null:population.get(0).getPhenotype().depth(),
      population.get(0).getPhenotype()==null?null:population.get(0).getPhenotype().size(),
      population.get(0).getPhenotype()==null?null:population.get(0).getPhenotype().leaves().size(),
      population.get(0).getFitness().getValue(),
      bestGeneralizationFitness != null ? bestGeneralizationFitness.getValue() : null,};
    return numbers;
  }

  @Override
  public Set<Class<? extends EvolutionEvent>> getEventClasses() {
    return eventClasses;
  }

}
