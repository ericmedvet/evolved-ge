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
import it.units.malelab.ege.mapper.PiGEMapper;
import it.units.malelab.ege.operators.OnePointCrossover;
import it.units.malelab.ege.operators.ProbabilisticMutation;
import it.units.malelab.ege.operators.TwoPointsCrossover;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    PrintStream filePs = new PrintStream("/home/eric/Scrivania/ge-locality/analysis."+dateForFile()+".csv", "UTF-8");
    filePs.println("i;j;grammar;gSize;operator;mapper;dg1;dg2;dp1;dp2;p1PSize;p2PSize;c1PSize;c2PSize");
    Map<PrintStream, String> outputs = new HashMap<>();
    outputs.put(System.out, "%2d %2d | %10.10s %4d | %15.15s %15.15s | %4.0f %4.0f | %4.0f %4.0f | %4.0f %4.0f | %4.0f %4.0f%n");
    outputs.put(filePs, "%d;%d;%s;%d;%s;%s;%f;%.0f;%f;%.0f;%.0f;%.0f;%.0f;%.0f%n");
    Random random = new Random(1);
    int numberOfIndividuals = 100;
    int numberOfRepetitions = 100;
    int[] genotypeSizes = new int[]{32,64,128,256,512};
    String[] grammarNames = new String[]{"max-grammar", "text", "santa-fe", "symbolic-regression"};
    Distance<Genotype> genotypeDistance = new GenotypeHammingDistance();
    Distance<List<String>> phenotypeDistance = new EditDistance<>();
    List<GeneticOperator> operators = new ArrayList<>();
    operators.add(new SparseFlipMutation(random));
    operators.add(new CompactFlipMutation(random));
    operators.add(new ProbabilisticMutation(random, 0.01d));
    operators.add(new OnePointCrossover(random));
    operators.add(new TwoPointsCrossover(random));
    List<Genotype> population = new ArrayList<>();
    for (String grammarName : grammarNames) {
      Grammar<String> grammar = Utils.parseFromFile(new File("grammars/" + grammarName + ".bnf"));
      List<Mapper<String>> mappers = new ArrayList<>();
      mappers.add(new StandardGEMapper(8, 10, grammar));
      mappers.add(new BreathFirstMapper(8, 10, grammar));
      mappers.add(new PiGEMapper<>(16, 10, grammar));
      mappers.add(new FractalMapper(10, grammar));
      for (int genotypeSize : genotypeSizes) {
        for (int i = 0; i < numberOfIndividuals; i++) {
          population.add(Utils.randomGenotype(genotypeSize, random));
        }
        for (int i = 0; i < population.size(); i++) {
          for (int j = 0; j < numberOfRepetitions; j++) {
            List<Genotype> parentGenotypes = new ArrayList<>(2);
            parentGenotypes.add(population.get(i));
            parentGenotypes.add(population.get(j));
            for (GeneticOperator operator : operators) {
              List<Genotype> childGenotypes = operator.apply(parentGenotypes);
              double[] genotypeDistances = new double[childGenotypes.size()];
              for (int h = 0; h < genotypeDistances.length; h++) {
                genotypeDistances[h] = genotypeDistance.d(parentGenotypes.get(h), childGenotypes.get(h));
              }
              for (Mapper<String> mapper : mappers) {
                List<List<String>> parentPhenotypes = new ArrayList<>();
                List<List<String>> childPhenotypes = new ArrayList<>();
                for (int h = 0; h < childGenotypes.size(); h++) {
                  try {
                    parentPhenotypes.add(mapper.map(parentGenotypes.get(h)).flatContents());
                  } catch (MappingException ex) {
                    parentPhenotypes.add(Collections.EMPTY_LIST);
                  }
                  try {
                    childPhenotypes.add(mapper.map(childGenotypes.get(h)).flatContents());
                  } catch (MappingException ex) {
                    childPhenotypes.add(Collections.EMPTY_LIST);
                  }
                }
                double[] phenotypeDistances = new double[genotypeDistances.length];
                for (int h = 0; h < phenotypeDistances.length; h++) {
                  phenotypeDistances[h] = phenotypeDistance.d(parentPhenotypes.get(h), childPhenotypes.get(h));
                }
                for (Map.Entry<PrintStream, String> outputEntry : outputs.entrySet()) {
                  outputEntry.getKey().printf(outputEntry.getValue(), i, j,
                          grammarName, genotypeSize,
                          operator.getClass().getSimpleName(),
                          mapper.getClass().getSimpleName(),
                          genotypeDistances[0], (genotypeDistances.length > 1) ? genotypeDistances[1] : null,
                          phenotypeDistances[0], (genotypeDistances.length > 1) ? phenotypeDistances[1] : null,
                          (double)parentPhenotypes.get(0).size(), (genotypeDistances.length > 1) ? (double)parentPhenotypes.get(1).size() : null,
                          (double)childPhenotypes.get(0).size(), (genotypeDistances.length > 1) ? (double)childPhenotypes.get(1).size() : null
                  );
                }
              }
            }
          }
        }
      }
    }
    filePs.close();
  }
  
  private String dateForFile() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
    return simpleDateFormat.format(new Date());
  }

}
