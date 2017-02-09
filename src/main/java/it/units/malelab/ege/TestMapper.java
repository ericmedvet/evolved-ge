/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.distance.BitsGenotypeEditDistance;
import it.units.malelab.ege.distance.CachedDistance;
import it.units.malelab.ege.distance.Distance;
import it.units.malelab.ege.distance.EditDistance;
import it.units.malelab.ege.distance.SGEGenotypeHammingDistance;
import it.units.malelab.ege.distance.TreeEditDistance;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.genotype.SGEGenotypeFactory;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.operator.AbstractOperator;
import it.units.malelab.ege.evolver.operator.BitsSGECrossover;
import it.units.malelab.ege.evolver.operator.CompactFlipMutation;
import it.units.malelab.ege.evolver.operator.LengthPreservingOnePointCrossover;
import it.units.malelab.ege.evolver.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.evolver.operator.OnePointCrossover;
import it.units.malelab.ege.evolver.operator.ProbabilisticMutation;
import it.units.malelab.ege.evolver.operator.SGECrossover;
import it.units.malelab.ege.evolver.operator.SGEMutation;
import it.units.malelab.ege.evolver.operator.TwoPointsCrossover;
import it.units.malelab.ege.evolver.validator.AnyValidator;
import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.mapper.BitsSGEMapper;
import it.units.malelab.ege.mapper.BreathFirstMapper;
import it.units.malelab.ege.mapper.HierarchicalMapper;
import it.units.malelab.ege.mapper.Mapper;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.PiGEMapper;
import it.units.malelab.ege.mapper.SGEMapper;
import it.units.malelab.ege.mapper.StandardGEMapper;
import it.units.malelab.ege.mapper.DHierarchicalMapper;
import it.units.malelab.ege.mapper.WeightedHierarchicalMapper;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author danny
 */
public class TestMapper {

  public static void main(String[] args) throws IOException {
    Map<String, Distance> genotypeDistances = new LinkedHashMap<>();
    genotypeDistances.put("BitsEdit", new CachedDistance<>(new BitsGenotypeEditDistance()));
    genotypeDistances.put("SGEHamming", new CachedDistance<>(new SGEGenotypeHammingDistance()));
    Map<String, Distance<Node<String>>> phenotypeDistances = new LinkedHashMap<>();
    Map<String, String> descriptions = new LinkedHashMap<>();
    final EditDistance<String> editDistance = new EditDistance<>();
    phenotypeDistances.put("LeavesEdit", new CachedDistance<>(new Distance<Node<String>>() {
      @Override
      public double d(Node<String> t1, Node<String> t2) {
        List<String> s1 = Node.EMPTY_TREE.equals(t1) ? Collections.EMPTY_LIST : Utils.contents(t1.leaves());
        List<String> s2 = Node.EMPTY_TREE.equals(t2) ? Collections.EMPTY_LIST : Utils.contents(t2.leaves());
        return editDistance.d(s1, s2);
      }
    }));
    phenotypeDistances.put("TreeEdit", new CachedDistance<>(new TreeEditDistance<String>()));
    //prepare file
    //PrintStream distancesFilePS = new PrintStream("dist.csv");
    //distancesFilePS.println("Problem;Mapper;Operator;GenoSize;ChildSize;p1-c_G;p1-c_PL;p1-c_PT;p1-c_F;p2-c_G;p2-c_PL;p2-c_PT;p2-c_F;p1-p2_G;p1-p2_PL;p1-p2_PT;p1-p2_F;c_PSize;c_PDepth;c_PLength;p1_PSize;p1_PDepth;p1_PLength;p2_PSize;p2_PDepth;p2_PLength");
    //prepare problems
    Map<String, BenchmarkProblems.Problem> problems = new LinkedHashMap<>();
    problems.put("harmonic", BenchmarkProblems.harmonicCurveProblem());
    problems.put("poly4", BenchmarkProblems.classic4PolynomialProblem());
    problems.put("max", BenchmarkProblems.max());
    problems.put("text", BenchmarkProblems.text("Hello world!"));
    problems.put("santafe", BenchmarkProblems.santaFe());
    Mapper mapper;
    AbstractOperator operator;
    int n_individuals = 100;
    List[] bitGenosSet1 = new List[1];
    List[] bitGenosSet2 = new List[1];
    List genosSet1 = null, genosSet2 = null;
    for (int i = 0; i < bitGenosSet1.length; i++) {
      bitGenosSet1[i] = (new RandomInitializer<>(new Random(i), new BitsGenotypeFactory((int) (128* Math.pow(2, i))))).getGenotypes(n_individuals, new AnyValidator());
      bitGenosSet2[i] = (new RandomInitializer<>(new Random(i + 5), new BitsGenotypeFactory((int) (128 * Math.pow(2, i))))).getGenotypes(n_individuals, new AnyValidator());
    }
    for (String problemName : problems.keySet()) {
      descriptions.put("problemName", problemName);
      BenchmarkProblems.Problem problem = problems.get(problemName);
      for (int m = 1; m < 2; m++) {
        mapper = null;
        Random r = new Random(m);
        Grammar<String> grammar = problem.getGrammar();
        switch (m) {
          case 0:
            descriptions.put("mapperName", "StdGE");
            mapper = new StandardGEMapper<>(8, 5, grammar);
            break;
          case 1:
            descriptions.put("mapperName", "DHier");
            mapper = new DHierarchicalMapper<>(grammar);
            break;
          case 2:
            descriptions.put("mapperName", "Hier");
            mapper = new HierarchicalMapper<>(grammar);
            break;
          case 3:
            descriptions.put("mapperName", "wHier");
            mapper = new WeightedHierarchicalMapper<>(6,grammar);
            break;
        }
        for (int o = 0; o < 1; o++) {
          operator = null;
          switch (o) {
            case 0:
              operator = new LengthPreservingTwoPointsCrossover(r);
              descriptions.put("operatorName", "LengthTwoP");
              break;
          }
          if (operator != null && mapper != null) {
            for (int i = 0; i < bitGenosSet1.length; i++) {
              for (Object geno : bitGenosSet1[i]) {
                doMap((BitsGenotype) geno, mapper);
              }
            }
            System.out.printf("%s %s %s\n", descriptions.get("problemName"), descriptions.get("mapperName"), descriptions.get("operatorName"));
          }
        }
      }
    }
    phenotypeDistances.clear();
    //distancesFilePS.close();
  }

  private static void doMap(BitsGenotype geno, Mapper mapper) {
    try {
      System.out.println("\n" + geno.toInt() + " -> " + mapper.map(geno).leaves().toString() + "\n");
    } catch (MappingException ex) {
      //Logger.getLogger(TestMapper.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
