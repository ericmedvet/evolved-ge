/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.mapper.BreathFirstMapper;
import it.units.malelab.ege.mapper.HierarchicalMapper;
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
import it.units.malelab.ege.mapper.StructuralGEMapper;
import it.units.malelab.ege.mapper.WeightedHierarchicalMapper;
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
    //main.localityAnalysis();
    Grammar<String> g1 = Utils.parseFromFile(new File("grammars/max-grammar-easy.bnf"));
    Grammar<String> g2 = Utils.parseFromFile(new File("grammars/text.bnf"));
    Grammar<String> g3 = Utils.parseFromFile(new File("grammars/simple-recursive.bnf"));
    Mapper<String> wf1 = new WeightedHierarchicalMapper<>(5, g1);
    Mapper<String> wf2 = new WeightedHierarchicalMapper<>(5, g2);
    Mapper<String> wf3 = new WeightedHierarchicalMapper<>(5, g3);
    Mapper<String> f1 = new HierarchicalMapper<>(g1);
    Mapper<String> f2 = new HierarchicalMapper<>(g2);
    Mapper<String> f3 = new HierarchicalMapper<>(g3);
    Mapper<String> ge1 = new StandardGEMapper<>(8, 10, g1);
    Mapper<String> ge2 = new StandardGEMapper<>(8, 10, g2);
    Mapper<String> ge3 = new StandardGEMapper<>(8, 10, g3);
    Mapper<String> sge1 = new StructuralGEMapper<>(5, g1);
    Mapper<String> sge2 = new StructuralGEMapper<>(5, g2);
    Mapper<String> sge3 = new StructuralGEMapper<>(5, g3);
    Random r = new Random(1);
    for (int i = 0; i<5; i++) {
      Genotype g = Utils.randomGenotype(2048, new Random(i));
      System.out.printf("%3d %3d %3d | %3d %3d %3d | %3d %3d %3d | %3d %3d %3d%n",
              Utils.safelyMapAndFlat(ge1, g).size(),
              Utils.safelyMapAndFlat(ge2, g).size(),
              Utils.safelyMapAndFlat(ge3, g).size(),
              Utils.safelyMapAndFlat(f1, g).size(),
              Utils.safelyMapAndFlat(f2, g).size(),
              Utils.safelyMapAndFlat(f3, g).size(),
              Utils.safelyMapAndFlat(wf1, g).size(),
              Utils.safelyMapAndFlat(wf2, g).size(),
              Utils.safelyMapAndFlat(wf3, g).size(),
              Utils.safelyMapAndFlat(sge1, g).size(),
              Utils.safelyMapAndFlat(sge2, g).size(),
              Utils.safelyMapAndFlat(sge3, g).size()
              );
    }
  }

  public void localityAnalysis() throws IOException {
    PrintStream filePs = new PrintStream("/home/eric/Scrivania/ge-locality/analysis." + dateForFile() + ".csv", "UTF-8");
    filePs.println("gSize;operator;grammar;mapper;i;dg1;dg2;dp1;dp2;p1PSize;p2PSize;c1PSize;c2PSize");
    Map<PrintStream, String> outputs = new HashMap<>();
    outputs.put(System.out, "%4d %10.10s %10.10s %10.10s | %3d | %4.0f %4.0f | %4.0f %4.0f | %4.0f %4.0f | %4.0f %4.0f%n");
    outputs.put(filePs, "%d;%s;%s;%s;%d;%.0f;%.0f;%.0f;%.0f;%.0f;%.0f;%.0f;%.0f%n");
    Random random = new Random(1);
    int numberOfRepetitions = 1000;
    int[] genotypeSizes = new int[]{32, 64, 128, 256, 512, 1024};
    String[] grammarNames = new String[]{"max-grammar", "text", "santa-fe", "symbolic-regression"};
    Distance<Genotype> genotypeDistance = new GenotypeHammingDistance();
    Distance<List<String>> phenotypeDistance = new EditDistance<>();
    List<GeneticOperator> operators = new ArrayList<>();
    operators.add(new SparseFlipMutation(random));
    operators.add(new CompactFlipMutation(random));
    operators.add(new ProbabilisticMutation(random, 0.01d));
    operators.add(new OnePointCrossover(random));
    operators.add(new TwoPointsCrossover(random));
    for (int genotypeSize : genotypeSizes) {
      List<List<Genotype>> parentGenotypes = new ArrayList<>();
      for (int i = 0; i < numberOfRepetitions; i++) {
        List<Genotype> genotypes = new ArrayList<>();
        genotypes.add(Utils.randomGenotype(genotypeSize, random));
        genotypes.add(Utils.randomGenotype(genotypeSize, random));
        parentGenotypes.add(genotypes);
      }
      for (GeneticOperator operator : operators) {
        List<List<Genotype>> childGenotypes = new ArrayList<>();
        List<List<Double>> genotypeDistances = new ArrayList<>();
        for (List<Genotype> parents : parentGenotypes) {
          List<Genotype> children = operator.apply(parents);
          childGenotypes.add(children);
          List<Double> distances = new ArrayList<>();
          for (int i = 0; i < children.size(); i++) {
            distances.add(genotypeDistance.d(parents.get(i), children.get(i)));
          }
          genotypeDistances.add(distances);
        }
        for (String grammarName : grammarNames) {
          Grammar<String> grammar = Utils.parseFromFile(new File("grammars/" + grammarName + ".bnf"));
          List<Mapper<String>> mappers = new ArrayList<>();
          mappers.add(new StandardGEMapper(8, 10, grammar));
          mappers.add(new BreathFirstMapper(8, 10, grammar));
          mappers.add(new PiGEMapper<>(16, 10, grammar));
          mappers.add(new HierarchicalMapper(grammar));
          for (Mapper<String> mapper : mappers) {
            for (int i = 0; i < parentGenotypes.size(); i++) {
              List<List<String>> parents = new ArrayList<>();
              List<List<String>> children = new ArrayList<>();
              List<Double> distances = new ArrayList<>();
              boolean oneArity = childGenotypes.get(i).size()==1;
              for (int j = 0; j < childGenotypes.get(i).size(); j++) {
                try {
                  parents.add(Utils.contents(mapper.map(parentGenotypes.get(i).get(j)).leaves()));
                } catch (MappingException ex) {
                  parents.add(Collections.EMPTY_LIST);
                }
                try {
                  children.add(Utils.contents(mapper.map(childGenotypes.get(i).get(j)).leaves()));
                } catch (MappingException ex) {
                  children.add(Collections.EMPTY_LIST);
                }
                distances.add(phenotypeDistance.d(parents.get(j), children.get(j)));
              }
              //print
              for (Map.Entry<PrintStream, String> outputEntry : outputs.entrySet()) {
                outputEntry.getKey().printf(outputEntry.getValue(),
                        genotypeSize, operator.getClass().getSimpleName(), grammarName, mapper.getClass().getSimpleName(),
                        i,
                        genotypeDistances.get(i).get(0), oneArity?null:genotypeDistances.get(i).get(1),
                        distances.get(0), oneArity?null:distances.get(1),
                        (double) parents.get(0).size(), oneArity?null:((double)parents.get(1).size()),
                        (double) children.get(0).size(), oneArity?null:((double)children.get(1).size())
                );
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
