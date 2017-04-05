/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.core.grammar.Node;
import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.core.fitness.FitnessComputer;
import it.units.malelab.ege.ge.genotype.Genotype;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class EvolvabilityFitnessLogger<G extends Genotype, T> extends AbstractGenerationLogger<G, T> {

  private final PrintStream ps;
  private final boolean writeHeader;

  private boolean headerWritten;
  private final List<String> columnNames;

  public EvolvabilityFitnessLogger(PrintStream ps, FitnessComputer<T> generalizationFitnessComputer, Map<String, Object> constants, boolean writeHeader) {
    super(generalizationFitnessComputer, constants);
    this.ps = ps;
    this.writeHeader = writeHeader;
    columnNames = new ArrayList<>();
  }

  @Override
  public synchronized void listen(EvolutionEvent<G, T> event) {
    GenerationEvent ev = ((GenerationEvent) event);
    int generation = ev.getGeneration();
    List<Individual<G, T>> population = new ArrayList<>(ev.getPopulation());
    Map<String, Object> indexes = new LinkedHashMap<>();
    indexes.putAll(computeIndexes(generation, population));
    for (Individual i : population) {
      ps.printf("%s;%s;%s;%s;%d;%s;%s;%s%n", indexes.get("Problem"), indexes.get("Mapper"), indexes.get("initGenoSize"), indexes.get("Run"), generation, i.getFitness(), calcParentsFitness(i, 0), calcParentsFitness(i, 1));
    }
    //System.out.printf("%s\t%s\t%s\t%s\t%d%n", constants.get("Problem"), constants.get("Mapper"), constants.get("initGenoSize"), constants.get("Run"), generation);
  }

  private Double calcParentsFitness(Individual<G, T> child, int i) {
    try {
      return (double) ((Individual<G, T>) child.getParents().get(i)).getFitness().getValue();
    } catch (IndexOutOfBoundsException ex) {
      return Double.NaN;
    }
  }
}
