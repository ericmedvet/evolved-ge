/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.evolver.sepandconq;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.Validator;
import it.units.malelab.ege.core.evolver.PartitionConfiguration;
import it.units.malelab.ege.core.fitness.BinaryClassification;
import it.units.malelab.ege.core.fitness.FitnessComputer;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.core.initializer.PopulationInitializer;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.ranker.Ranker;
import it.units.malelab.ege.core.selector.Selector;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class SACConfiguration<I, G, T, F extends MultiObjectiveFitness> extends PartitionConfiguration<G, T, F> {

  private final Joiner<T> joiner;
  private final int maxFlatGenerations;
  
  private List<I> removedPositives = new ArrayList<>();

  public SACConfiguration(
          Joiner<T> joiner,
          int maxFlatGenerations,
          Comparator<Individual<G, T, F>> partitionerComparator,
          int partitionSize,
          Ranker<Individual<G, T, F>> parentInPartitionRanker,
          Selector<Individual<G, T, F>> parentInPartitionSelector,
          Ranker<Individual<G, T, F>> unsurvivalInPartitionRanker,
          Selector<Individual<G, T, F>> unsurvivalInPartitionSelector,
          int populationSize,
          int numberOfGenerations,
          PopulationInitializer<G> populationInitializer,
          Validator<G> initGenotypeValidator,
          Mapper<G, T> mapper,
          Map<GeneticOperator<G>, Double> operators,
          Ranker<Individual<G, T, F>> ranker,
          Selector<Individual<G, T, F>> parentSelector,
          Selector<Individual<G, T, F>> unsurvivalSelector,
          int offspringSize,
          boolean overlapping,
          Problem<T, F> problem) {
    super(partitionerComparator, partitionSize, parentInPartitionRanker, parentInPartitionSelector, unsurvivalInPartitionRanker, unsurvivalInPartitionSelector, populationSize, numberOfGenerations, populationInitializer, initGenotypeValidator, mapper, operators, ranker, parentSelector, unsurvivalSelector, offspringSize, overlapping, problem);
    this.joiner = joiner;
    this.maxFlatGenerations = maxFlatGenerations;
    if (!(problem.getLearningFitnessComputer() instanceof BinaryClassification)) {
      throw new IllegalArgumentException("Separate and conquer is suitable only for binary classification problems");
    }
  }

  @Override
  public Problem<T, F> getProblem() {
    BinaryClassification<I, T> allFitnessComputer = (BinaryClassification<I, T>)super.getProblem().getLearningFitnessComputer();
    List<I> localPositives = new ArrayList<>(allFitnessComputer.getPositives());
    localPositives.removeAll(removedPositives);
    BinaryClassification<I, T> localFitnessComputer = new BinaryClassification<>(
            localPositives,
            allFitnessComputer.getNegatives(), 
            allFitnessComputer.getClassifier()
    );
    return new Problem<>(
            super.getProblem().getGrammar(),
            (FitnessComputer<T, F>)localFitnessComputer,
            super.getProblem().getTestingFitnessComputer(),
            super.getProblem().getPhenotypePrinter()
    );
  }
  
  public boolean remove(Node<T> partialSolution) {
    boolean removed = false;
    BinaryClassification<I, T> allFitnessComputer = (BinaryClassification<I, T>)super.getProblem().getLearningFitnessComputer();
    for (I i : allFitnessComputer.getPositives()) {
      if (allFitnessComputer.getClassifier().classify(i, partialSolution)) {
        if (!removedPositives.contains(i)) {
          removedPositives.add(i);
          removed = true;
        }
      }
    }
    return removed;
  }

  public Joiner<T> getJoiner() {
    return joiner;
  }

  public int getMaxFlatGenerations() {
    return maxFlatGenerations;
  }

  public List<I> getRemovedPositives() {
    return removedPositives;
  }

  @Override
  public String toString() {
    return "SCEConfiguration{" + "joiner=" + joiner + ", maxFlatGenerations=" + maxFlatGenerations + ", removedPositives=" + removedPositives + '}';
  }
  
}
