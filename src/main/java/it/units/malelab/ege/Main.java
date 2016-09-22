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
import it.units.malelab.ege.distance.GenotypeEditDistance;
import it.units.malelab.ege.distance.GenotypeHammingDistance;
import it.units.malelab.ege.mapper.PiGEMapper;
import it.units.malelab.ege.mapper.StructuralGEMapper;
import it.units.malelab.ege.mapper.WeightedHierarchicalMapper;
import it.units.malelab.ege.operators.LengthPreservingOnePointCrossover;
import it.units.malelab.ege.operators.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.operators.OnePointCrossover;
import it.units.malelab.ege.operators.ProbabilisticMutation;
import it.units.malelab.ege.operators.SGECrossover;
import it.units.malelab.ege.operators.TwoPointsCrossover;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

/**
 *
 * @author eric
 */
public class Main {

  public static void main(String[] args) throws IOException {
    Main main = new Main();
    main.localityDegeneracyAnalysis();
  }

  public void testBinaryOperators() {
    Random r = new Random(1);
    List<GeneticOperator> operators = new ArrayList<>();
    operators.add(new OnePointCrossover(r));
    operators.add(new TwoPointsCrossover(r));
    operators.add(new LengthPreservingOnePointCrossover(r));
    operators.add(new LengthPreservingTwoPointsCrossover(r));
    for (int i = 0; i < 5; i++) {
      List<Genotype> parents = new ArrayList<>();
      parents.add(Utils.randomGenotype(256, r));
      parents.add(Utils.randomGenotype(256, r));
      for (GeneticOperator operator : operators) {
        List<Genotype> children = operator.apply(parents);
        System.out.printf("%20.20s | %4d %4d | %4d %4d%n",
                operator.getClass().getSimpleName(),
                parents.get(0).size(), parents.get(1).size(),
                children.get(0).size(), children.get(1).size());
      }
    }
  }

  public void testMappers() throws IOException {
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
    for (int i = 0; i < 5; i++) {
      Genotype g = Utils.randomGenotype(128, r);
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

  public void localityDegeneracyAnalysis() throws IOException {
    PrintStream filePs = new PrintStream("/home/eric/Scrivania/ge-locality/analysis." + dateForFile() + ".csv", "UTF-8");
    filePs.println("gSize;operator;grammar;mapper;i;dg1;dg2;dp1;dp2;p1PSize;p2PSize;c1PSize;c2PSize");
    Map<PrintStream, String> outputs = new HashMap<>();
    outputs.put(System.out, "%4d %10.10s %10.10s %15.15s || %4.0f %4.0f | %4.0f %4.0f || %4d %4d | %4d %4d || %4d %4d | %4d %4d || %4d %4d | %4d %4d ||%n");
    outputs.put(filePs, "%d;%s;%s;%s;%.0f;%.0f;%.0f;%.0f;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d%n");
    filePs.println("gSize;grammar;mapper;operator;gd0;gd1;pd0;pd1;p0l;p1l;c0l;c1l;p0d;p1d;c0d;c1d;p0s;p1s;c0s;c1s");
    Random random = new Random(1);
    int numberOfRepetitions = 2;
    int[] genotypeSizes = new int[]{128, 256, 512, 1024, 2048, 4096};
    Distance<Genotype> gd = new GenotypeEditDistance();
    Distance<List<String>> pd = new EditDistance<>();
    Map<String[], Pair<Mapper<String>, GeneticOperator>> map = new LinkedHashMap<>();    
    System.out.println("Preparing mappers:");    
    for (String grammarName : new String[]{"max-grammar", "text", "santa-fe", "symbolic-regression"}) {
      System.out.printf("\t%s%n", grammarName);
      Grammar<String> grammar = Utils.parseFromFile(new File("grammars/" + grammarName + ".bnf"));
      Map<String, Mapper<String>> mappers = new LinkedHashMap<>();
      mappers.put("ge-8-1", new StandardGEMapper<>(8, 1, grammar));
      mappers.put("ge-8-10", new StandardGEMapper<>(8, 10, grammar));
      mappers.put("bf-8-11", new BreathFirstMapper<>(8, 11, grammar));
      mappers.put("bf-8-10", new BreathFirstMapper<>(8, 10, grammar));
      mappers.put("pige-16-1", new PiGEMapper<>(16, 1, grammar));
      mappers.put("pige-16-10", new PiGEMapper<>(16, 10, grammar));
      mappers.put("sge-5", new StructuralGEMapper<>(5, grammar));
      mappers.put("sge-10", new StructuralGEMapper<>(10, grammar));
      mappers.put("hge", new HierarchicalMapper<>(grammar));
      mappers.put("whge-5", new WeightedHierarchicalMapper<>(5, grammar));
      mappers.put("whge-10", new WeightedHierarchicalMapper<>(10, grammar));
      for (Map.Entry<String, Mapper<String>> mapperEntry : mappers.entrySet()) {
        List<GeneticOperator> operators = new ArrayList<>();
        operators.add(new SparseFlipMutation(random));
        operators.add(new CompactFlipMutation(random));
        operators.add(new ProbabilisticMutation(random, 0.01d));
        operators.add(new OnePointCrossover(random));
        operators.add(new TwoPointsCrossover(random));
        operators.add(new LengthPreservingOnePointCrossover(random));
        operators.add(new LengthPreservingTwoPointsCrossover(random));
        if (mapperEntry.getValue() instanceof StructuralGEMapper) {
          operators.add(new SGECrossover((StructuralGEMapper) mapperEntry.getValue(), random));
        }
        for (GeneticOperator operator : operators) {
          map.put(
                  new String[]{grammarName, mapperEntry.getKey(), operator.getClass().getSimpleName()},
                  new Pair<>(mapperEntry.getValue(), operator));
        }
      }
    }
    for (int genotypeSize : genotypeSizes) {
      List<List<Genotype>> parentsList = new ArrayList<>();
      for (int i = 0; i < numberOfRepetitions; i++) {
        parentsList.add(Arrays.asList(
                Utils.randomGenotype(genotypeSize, random),
                Utils.randomGenotype(genotypeSize, random)
        ));
      }
      Map<Pair<String, Genotype>, Node<String>> phenotypesMap = new WeakHashMap<>();
      Map<List<Genotype>, Double> genotypeDistancesCache = new WeakHashMap<>();
      for (Map.Entry<String[], Pair<Mapper<String>, GeneticOperator>> entry : map.entrySet()) {
        for (List<Genotype> parents : parentsList) {
          List<Genotype> children = entry.getValue().getSecond().apply(parents);
          boolean binary = children.size() > 1;
          //compute/retrieve distances in genotype
          Double gd0 = genotypeDistancesCache.get(Arrays.asList(parents.get(0), children.get(0)));
          if (gd0 == null) {
            gd0 = gd.d(parents.get(0), children.get(0));
            genotypeDistancesCache.put(Arrays.asList(parents.get(0), children.get(0)), gd0);
          }
          Double gd1 = null;
          if (binary) {
            gd1 = genotypeDistancesCache.get(Arrays.asList(parents.get(1), children.get(1)));
            if (gd1 == null) {
              gd1 = gd.d(parents.get(1), children.get(1));
              genotypeDistancesCache.put(Arrays.asList(parents.get(1), children.get(1)), gd1);
            }
          }
          //compute/retrieve phenotypes
          Node<String> phenotypeParent0 = phenotypesMap.get(new Pair<>(entry.getKey()[2], parents.get(0)));
          if (phenotypeParent0 == null) {
            try {
              phenotypeParent0 = entry.getValue().getFirst().map(parents.get(0));
            } catch (MappingException ex) {
              //leave at null
            }
          }
          Node<String> phenotypeChild0 = phenotypesMap.get(new Pair<>(entry.getKey()[2], parents.get(0)));
          if (phenotypeChild0 == null) {
            try {
              phenotypeChild0 = entry.getValue().getFirst().map(children.get(0));
            } catch (MappingException ex) {
              //leave at null
            }
          }
          Node<String> phenotypeParent1 = null;
          Node<String> phenotypeChild1 = null;
          if (binary) {
            phenotypeParent1 = phenotypesMap.get(new Pair<>(entry.getKey()[2], parents.get(1)));
            if (phenotypeParent1 == null) {
              try {
                phenotypeParent1 = entry.getValue().getFirst().map(parents.get(1));
              } catch (MappingException ex) {
                //leave at null
              }
            }
            phenotypeChild1 = phenotypesMap.get(new Pair<>(entry.getKey()[2], parents.get(1)));
            if (phenotypeChild1 == null) {
              try {
                phenotypeChild1 = entry.getValue().getFirst().map(children.get(1));
              } catch (MappingException ex) {
                //leave at null
              }
            }
          }
          //compute phenotype distances
          Double pd0 = ((phenotypeParent0 == null) || (phenotypeChild0 == null)) ? Double.NaN : pd.d(
                  Utils.contents(phenotypeParent0.leaves()),
                  Utils.contents(phenotypeChild0.leaves()));
          Double pd1 = null;
          if (binary) {
            pd1 = ((phenotypeParent1 == null) || (phenotypeChild1 == null)) ? Double.NaN : pd.d(
                    Utils.contents(phenotypeParent1.leaves()),
                    Utils.contents(phenotypeChild1.leaves()));
          }
          //get tree measures
          Integer parent0Lenght = (phenotypeParent0==null)?null:phenotypeParent0.leaves().size();
          Integer parent1Lenght = (phenotypeParent1==null)?null:phenotypeParent1.leaves().size();
          Integer child0Lenght = (phenotypeChild0==null)?null:phenotypeChild0.leaves().size();
          Integer child1Lenght = (phenotypeChild1==null)?null:phenotypeChild1.leaves().size();
          Integer parent0Depth = (phenotypeParent0==null)?null:phenotypeParent0.depth();
          Integer parent1Depth = (phenotypeParent1==null)?null:phenotypeParent1.depth();
          Integer child0Depth = (phenotypeChild0==null)?null:phenotypeChild0.depth();
          Integer child1Depth = (phenotypeChild1==null)?null:phenotypeChild1.depth();
          Integer parent0Size = (phenotypeParent0==null)?null:phenotypeParent0.size();
          Integer parent1Size = (phenotypeParent1==null)?null:phenotypeParent1.size();
          Integer child0Size = (phenotypeChild0==null)?null:phenotypeChild0.size();
          Integer child1Size = (phenotypeChild1==null)?null:phenotypeChild1.size();
          //print
          for (Map.Entry<PrintStream, String> outputEntry : outputs.entrySet()) {
            outputEntry.getKey().printf(outputEntry.getValue(),
                    genotypeSize,
                    entry.getKey()[0], entry.getKey()[1], entry.getKey()[2],
                    gd0, gd1, pd0, pd1,
                    parent0Lenght, parent1Lenght, child0Lenght, child1Lenght,
                    parent0Depth, parent1Depth, child0Depth, child1Depth,
                    parent0Size, parent1Size, child0Size, child1Size
            );
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
