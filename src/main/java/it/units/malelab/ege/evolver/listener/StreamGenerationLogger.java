/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.evolver.fitness.FitnessComputer;
import it.units.malelab.ege.evolver.genotype.Genotype;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class StreamGenerationLogger<G extends Genotype, T> extends AbstractGenerationLogger<G, T> {

  private final PrintStream ps;
  private final boolean writeHeader;

  private boolean headerWritten;
  private final List<String> columnNames;

  public StreamGenerationLogger(PrintStream ps, FitnessComputer<T> generalizationFitnessComputer, Map<String, Object> constants, boolean writeHeader) {
    super(generalizationFitnessComputer, constants);
    this.ps = ps;
    this.writeHeader = writeHeader;
    columnNames = new ArrayList<>();
  }

  @Override
  public synchronized void listen(EvolutionEvent<G, T> event) {
    int generation = ((GenerationEvent) event).getGeneration();
    List<Individual<G, T>> population = new ArrayList<>(((GenerationEvent) event).getPopulation());
    Map<String, Object> indexes = new LinkedHashMap<>();
    if (event.getData()!=null) {
      indexes.putAll(event.getData());
    }
    indexes.putAll(computeIndexes(generation, population));
    if (columnNames.isEmpty()) {
      columnNames.addAll(indexes.keySet());
    }
    if (writeHeader&&!headerWritten) {
      headerWritten = true;
      for (String columnName : columnNames) {
        ps.print(columnName);
        if (!columnNames.get(columnNames.size() - 1).equals(columnName)) {
          ps.print(";");
        }
      }
      ps.println();
    }
    for (String columnName : columnNames) {
      ps.print(indexes.get(columnName));
      if (!columnNames.get(columnNames.size() - 1).equals(columnName)) {
        ps.print(";");
      }
    }
    ps.println();
  }

}
