/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.mapper.BreathFirstMapper;
import it.units.malelab.ege.mapper.FractalMapper;
import it.units.malelab.ege.mapper.Mapper;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.StandardGEMapper;
import it.units.malelab.ege.operators.CompactFlipMutation;
import it.units.malelab.ege.operators.GeneticOperator;
import it.units.malelab.ege.operators.SparseFlipMutation;
import it.units.malelab.ege.distance.Distance;
import it.units.malelab.ege.distance.EditDistance;
import it.units.malelab.ege.distance.GenotypeHammingDistance;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class Main {

  public static void main(String[] args) throws IOException {
    Main main = new Main();
    main.localityAnalysis();
  }

  public void localityAnalysis() throws IOException {
    Random random = new Random(1);
    int numberOfIndividuals = 10;
    int numberOfRepetitions = 10;
    int[] genotypeSizes = new int[]{128};
    String[] grammarNames = new String[]{"max-grammar", "text"};
    Distance<Genotype> genotypeDistance = new GenotypeHammingDistance();
    Distance<List<String>> phenotypeDistance = new EditDistance<String>();
    List<GeneticOperator> operators = new ArrayList<>();
    operators.add(new SparseFlipMutation(random));
    operators.add(new CompactFlipMutation(random));
    List<Genotype> population = new ArrayList<>();
    for (String grammarName : grammarNames) {
      Grammar<String> grammar = Utils.parseFromFile(new File("grammars/"+grammarName+".bnf"));
      List<Mapper<String>> mappers = new ArrayList<>();
      mappers.add(new StandardGEMapper(8, 10, grammar));
      mappers.add(new BreathFirstMapper(8, 10, grammar));
      mappers.add(new FractalMapper(10, grammar));
      for (int genotypeSize : genotypeSizes) {
        for (int i = 0; i < numberOfIndividuals; i++) {
          population.add(Utils.randomGenotype(genotypeSize, random));
        }
        for (Genotype parent1 : population) {
          for (int i = 0; i < numberOfRepetitions; i++) {
            List<Genotype> parentGenotypes = new ArrayList<>(2);
            parentGenotypes.add(parent1);
            parentGenotypes.add(population.get(i));
            for (GeneticOperator operator : operators) {
              List<Genotype> childGenotypes = operator.apply(parentGenotypes);
              double[] genotypeDistances = new double[childGenotypes.size()];
              for (int j = 0; j < genotypeDistances.length; j++) {
                genotypeDistances[j] = genotypeDistance.d(parentGenotypes.get(j), childGenotypes.get(j));
              }
              for (Mapper<String> mapper : mappers) {
                List<List<String>> parentPhenotypes = new ArrayList<>();
                List<List<String>> childPhenotypes = new ArrayList<>();
                for (int j = 0; j < childGenotypes.size(); j++) {
                  try {
                    parentPhenotypes.add(mapper.map(parentGenotypes.get(j)).flatContents());
                  } catch (MappingException ex) {
                    parentPhenotypes.add(Collections.EMPTY_LIST);
                  }
                  try {
                    childPhenotypes.add(mapper.map(childGenotypes.get(j)).flatContents());
                  } catch (MappingException ex) {
                    childPhenotypes.add(Collections.EMPTY_LIST);
                  }
                }
                double[] phenotypeDistances = new double[genotypeDistances.length];
                for (int j = 0; j < phenotypeDistances.length; j++) {
                  phenotypeDistances[j] = phenotypeDistance.d(parentPhenotypes.get(j), childPhenotypes.get(j));
                }
                System.out.printf("%2d | %10.10s %4d | %20.20s %20.20s | %5.0f %5.0f | %5.0f %5.0f\n", i,
                        grammarName, genotypeSize,
                        operator.getClass().getSimpleName(),
                        mapper.getClass().getSimpleName(),
                        genotypeDistances[0], phenotypeDistances[0],
                        Utils.mean(genotypeDistances), Utils.mean(phenotypeDistances)
                );
              }
            }
          }
        }
      }
    }
  }
  
  

}
