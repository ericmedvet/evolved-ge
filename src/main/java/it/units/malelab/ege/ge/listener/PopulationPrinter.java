/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.listener;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.AbstractListener;
import it.units.malelab.ege.core.listener.event.EvolutionEvent;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.GEIndividual;
import it.units.malelab.ege.ge.mapper.StandardGEMapper;
import it.units.malelab.ege.util.Utils;
import java.io.PrintStream;
import java.util.List;

/**
 *
 * @author eric
 */
public class PopulationPrinter<T, F extends Fitness> extends AbstractListener<T, F> {

  private final PrintStream ps;

  private static final char[][] CHARS = {{'O', 'I'}, {'.', ','}};

  public PopulationPrinter(PrintStream ps) {
    super(GenerationEvent.class);
    this.ps = ps;
  }

  @Override
  public void listen(EvolutionEvent<T, F> event) {
    int generation = ((GenerationEvent) event).getGeneration();
    ps.printf("Population at generation %d%n", generation);
    List<List<Individual<T, F>>> rankedPopulation = ((GenerationEvent) event).getRankedPopulation();
    for (List<Individual<T, F>> rank : rankedPopulation) {
      for (Individual<T, F> individual : rank) {
        if (individual instanceof GEIndividual) {
          if (((GEIndividual) individual).getGenotype() instanceof BitsGenotype) {
            BitsGenotype genotype = (BitsGenotype) ((GEIndividual) individual).getGenotype();
            //genotype
            int[] bitUsages = (int[]) individual.getOtherInfo().get(StandardGEMapper.BIT_USAGES_INDEX_NAME);
            if (bitUsages == null) {
              bitUsages = new int[genotype.size()];
            }
            for (int i = 0; i < genotype.size(); i++) {
              ps.print(CHARS[bitUsages[i] > 0 ? 0 : 1][genotype.get(i) ? 0 : 1]);
            }
            ps.printf(" -> %s%n", Utils.contents(individual.getPhenotype().leaves()));

          }
        }
      }
    }
  }

}
