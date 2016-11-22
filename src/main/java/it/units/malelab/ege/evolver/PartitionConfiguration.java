/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver;

import it.units.malelab.ege.BenchmarkProblems;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.evolver.operator.ProbabilisticMutation;
import it.units.malelab.ege.evolver.selector.Best;
import it.units.malelab.ege.evolver.selector.First;
import it.units.malelab.ege.evolver.selector.IndividualComparator;
import it.units.malelab.ege.evolver.selector.RepresenterBasedListSelector;
import it.units.malelab.ege.evolver.selector.Selector;
import it.units.malelab.ege.evolver.selector.Tournament;
import it.units.malelab.ege.evolver.validator.AnyValidator;
import it.units.malelab.ege.mapper.StandardGEMapper;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class PartitionConfiguration<G extends Genotype, T> extends StandardConfiguration<G, T> {
  
  private Comparator<Individual<G, T>> partitionerComparator;
  private Selector<List<Individual<G, T>>> parentPartitionSelector;
  private Selector<List<Individual<G, T>>> unsurvivalPartitionSelector;
  private int partitionSize;
  
  public static PartitionConfiguration<BitsGenotype, String> createDefault(BenchmarkProblems.Problem problem, Random random) {
    PartitionConfiguration<BitsGenotype, String> configuration = new PartitionConfiguration<>();
    StandardConfiguration<BitsGenotype, String> standardConfiguration = StandardConfiguration.createDefault(problem, random);
    configuration
            .partitionSize(100)
            .partitionerComparator((Comparator)(new IndividualComparator(IndividualComparator.Attribute.PHENO)))
            .parentPartitionSelector(new RepresenterBasedListSelector<>(
                    new First<>(),
                    new Tournament(5, random, new IndividualComparator(IndividualComparator.Attribute.FITNESS))
            ))
            .unsurvivalPartitionSelector(new RepresenterBasedListSelector<>(
                    new First<>(),
                    new Best(Collections.reverseOrder(new IndividualComparator(IndividualComparator.Attribute.FITNESS)))
            ))
            .unsurvivalSelector(new Best(Collections.reverseOrder(new IndividualComparator(IndividualComparator.Attribute.AGE))))
            .populationSize(standardConfiguration.getPopulationSize())
            .offspringSize(standardConfiguration.getOffspringSize())
            .overlapping(standardConfiguration.isOverlapping())
            .numberOfGenerations(standardConfiguration.getNumberOfGenerations())
            .populationInitializer(standardConfiguration.getPopulationInitializer())
            .initGenotypeValidator(standardConfiguration.getInitGenotypeValidator())
            .mapper(standardConfiguration.getMapper())
            .parentSelector(standardConfiguration.getParentSelector())
            .operators(standardConfiguration.getOperators())
            .fitnessComputer(standardConfiguration.getFitnessComputer());
    return configuration;
  }

  public Comparator<Individual<G, T>> getPartitionerComparator() {
    return partitionerComparator;
  }

  public PartitionConfiguration<G, T> partitionerComparator(Comparator<Individual<G, T>> partitionerComparator) {
    this.partitionerComparator = partitionerComparator;
    return this;
  }

  public Selector<List<Individual<G, T>>> getParentPartitionSelector() {
    return parentPartitionSelector;
  }

  public PartitionConfiguration<G, T> parentPartitionSelector(Selector<List<Individual<G, T>>> parentPartitionSelector) {
    this.parentPartitionSelector = parentPartitionSelector;
    return this;
  }

  public Selector<List<Individual<G, T>>> getUnsurvivalPartitionSelector() {
    return unsurvivalPartitionSelector;
  }

  public PartitionConfiguration<G, T> unsurvivalPartitionSelector(Selector<List<Individual<G, T>>> unsurvivalPartitionSelector) {
    this.unsurvivalPartitionSelector = unsurvivalPartitionSelector;
    return this;
  }

  public int getPartitionSize() {
    return partitionSize;
  }

  public PartitionConfiguration<G, T> partitionSize(int partitionSize) {
    this.partitionSize = partitionSize;
    return this;
  }
  
  
  
}
