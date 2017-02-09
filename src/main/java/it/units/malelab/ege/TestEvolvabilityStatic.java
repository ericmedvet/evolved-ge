/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.BenchmarkProblems.Problem;
import it.units.malelab.ege.distance.BitsGenotypeEditDistance;
import it.units.malelab.ege.distance.CachedDistance;
import it.units.malelab.ege.distance.Distance;
import it.units.malelab.ege.distance.EditDistance;
import it.units.malelab.ege.distance.SGEGenotypeHammingDistance;
import it.units.malelab.ege.distance.TreeEditDistance;
import it.units.malelab.ege.evolver.fitness.FitnessComputer;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.genotype.SGEGenotypeFactory;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.operator.AbstractCrossover;
import it.units.malelab.ege.evolver.operator.AbstractMutation;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Danny
 */
public class TestEvolvabilityStatic {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
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
    PrintStream distancesFilePS = new PrintStream("evolvability" + dateForFile() + ".csv");
    distancesFilePS.println("Problem;Mapper;Operator;GenoSize;ChildSize;p1-c_G;p1-c_PL;p1-c_F;p2-c_G;p2-c_PL;p2-c_F;p1-p2_G;p1-p2_PL;p1-p2_F;c_PSize;c_PDepth;c_PLength;p1_PSize;p1_PDepth;p1_PLength;p2_PSize;p2_PDepth;p2_PLength;childFitness;p1Fitness;p2Fitness");
    //prepare problems
    Map<String, BenchmarkProblems.Problem> problems = new LinkedHashMap<>();
    problems.put("harmonic", BenchmarkProblems.harmonicCurveProblem());
    problems.put("poly4", BenchmarkProblems.classic4PolynomialProblem());
    //problems.put("max", BenchmarkProblems.max());
    problems.put("text", BenchmarkProblems.text("Hello world!"));
    problems.put("santafe", BenchmarkProblems.santaFe());
    Mapper mapper;
    AbstractOperator operator;
    int n_valid_exp = 100;
    List[] bitGenosSet1 = new List[1];
    List[] bitGenosSet2 = new List[1];
    List genosSet1 = null, genosSet2 = null;
    for (int i = 0; i < bitGenosSet1.length; i++) {
      bitGenosSet1[i] = (new RandomInitializer<>(new Random(i), new BitsGenotypeFactory((int) (512 * Math.pow(2, i))))).getGenotypes(50 * n_valid_exp, new AnyValidator());
      bitGenosSet2[i] = (new RandomInitializer<>(new Random(i + 5), new BitsGenotypeFactory((int) (512 * Math.pow(2, i))))).getGenotypes(50 * n_valid_exp, new AnyValidator());
    }
    for (String problemName : problems.keySet()) {
      descriptions.put("problemName", problemName);
      BenchmarkProblems.Problem problem = problems.get(problemName);
      for (int m = 0; m < 4; m++) {
        mapper = null;
        Random r = new Random(m);
        Grammar<String> grammar = problem.getGrammar();
        switch (m) {
          case 0:
            descriptions.put("mapperName", "SGE");
            mapper = new SGEMapper<>(6, grammar);
            genosSet1 = (new RandomInitializer<>(new Random(1), new SGEGenotypeFactory((SGEMapper) mapper))).getGenotypes(50 * n_valid_exp, new AnyValidator());
            genosSet2 = (new RandomInitializer<>(new Random(2), new SGEGenotypeFactory((SGEMapper) mapper))).getGenotypes(50 * n_valid_exp, new AnyValidator());
            break;
          case 1:
            descriptions.put("mapperName", "BreathFirst");
            mapper = new BreathFirstMapper<>(8, 5, grammar);
            break;
          case 2:
            descriptions.put("mapperName", "PiGE");
            mapper = new PiGEMapper<>(16, 5, grammar);
            break;
          case 3:
            descriptions.put("mapperName", "StdGE");
            mapper = new StandardGEMapper<>(8, 5, grammar);
            break;
        }
        if (mapper.getClass().equals(SGEMapper.class)) {
          for (int o = 0; o < 2; o++) {
            operator = null;
            switch (o) {
              case 0:
                operator = new SGECrossover(r);
                descriptions.put("operatorName", "Cross");
                break;
              case 1:
                operator = new SGEMutation(0.01, (SGEMapper) mapper, r);
                descriptions.put("operatorName", "Mut");
                break;
            }
            calcDistances(n_valid_exp, genosSet1, genosSet2, mapper, operator, distancesFilePS, genotypeDistances.get("SGEHamming"), phenotypeDistances, problem, descriptions);
            System.out.printf("%s %s %s\n", descriptions.get("problemName"), descriptions.get("mapperName"), descriptions.get("operatorName"));
          }
        } else {
          for (int o = 0; o < 2; o++) {
            operator = null;
            switch (o) {
              case 0:
                operator = new OnePointCrossover(r);
                descriptions.put("operatorName", "Cross");
                break;
              case 1:
                operator = new ProbabilisticMutation(r, 0.01);
                descriptions.put("operatorName", "Mut");
                break;
            }
            if (operator != null && mapper != null) {
              for (int i = 0; i < bitGenosSet1.length; i++) {
                calcDistances(n_valid_exp, bitGenosSet1[i], bitGenosSet2[i], mapper, operator, distancesFilePS, genotypeDistances.get("BitsEdit"), phenotypeDistances, problem, descriptions);
              }
              System.out.printf("%s %s %s\n", descriptions.get("problemName"), descriptions.get("mapperName"), descriptions.get("operatorName"));
            }
          }
        }
      }
    }
    phenotypeDistances.clear();
    distancesFilePS.close();
  }

  private static String dateForFile() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
    return simpleDateFormat.format(new Date());
  }

  private static Node mapApply(Mapper mapper, Genotype geno) {
    try {
      return mapper.map(geno);
    } catch (MappingException ex) {
      return Node.EMPTY_TREE;
    }
  }

  private static Double distFitCalc(FitnessComputer pc, Node a, Node b) {
    try {
      return Math.abs((double) pc.compute(a).getValue() - (double) pc.compute(b).getValue());
    } catch (NullPointerException ex) {
      return null;
    }
  }

  private static Double distPhenoCalc(Distance<Node<String>> d, Node a, Node b) {
    if (!a.equals(Node.EMPTY_TREE) && !b.equals(Node.EMPTY_TREE)) {
      return d.d(a, b);
    } else {
      return null;
    }
  }

  private static void calcDistances(int n_valid, List p1Set, List p2Set, Mapper mapper, AbstractOperator operator, PrintStream out, Distance genoDist, Map<String, Distance<Node<String>>> phenoDistSet, Problem problem, Map<String, String> descr) {
    Genotype cG, p1G, p2G;
    Node cP, p1P, p2P;
    Double[] distArray;
    Integer[] sizes;
    int current_valid = 0;
    int index = 0;
    int repeats = 30;
    //System.out.println(p1Set.size());
    if (operator instanceof AbstractMutation) {
      while (current_valid < n_valid*repeats & index < p1Set.size()) {
        for (int h = 0; h < repeats; h++) {
          try {
            distArray = new Double[12];
            sizes = new Integer[9];

            /* array with the distances:
                                                |      p1-c      |      p2-c      |      p1-p2     |      c sizes      |      p1sizes     |      p2sizes     |  fitness values  |
                                                |  G   P(L)   F  |  G   P(L)   F  |  G   P(L)   F  | Size Depth Leaves |Size Depth Leaves |Size Depth Leaves |  c    p1    p2   |F  |
             */
            p1G = (Genotype) p1Set.get(index);
            cG = (Genotype) operator.apply(Arrays.asList(p1G)).get(0);
            distArray[0] = genoDist.d(p1G, cG);
            p1P = mapApply(mapper, p1G);
            cP = mapApply(mapper, cG);
            if (!cP.equals(Node.EMPTY_TREE)) {
              sizes[0] = cP.size();
              sizes[1] = cP.depth();
              sizes[2] = cP.leaves().size();
            }
            if (!p1P.equals(Node.EMPTY_TREE)) {
              sizes[3] = p1P.size();
              sizes[4] = p1P.depth();
              sizes[5] = p1P.leaves().size();
            }
            distArray[1] = distPhenoCalc(phenoDistSet.get("LeavesEdit"), p1P, cP);
            distArray[3] = distFitCalc(problem.getFitnessComputer(), p1P, cP);
            out.printf("%s;%s;%s;%d;%d;%f;%f;%f;%s;%s;%s;%s;%s;%s;%d;%d;%d;%d;%d;%d;%s;%s;%s;%f;%f;%s\n",
                    descr.get("problemName"),
                    descr.get("mapperName"),
                    descr.get("operatorName"),
                    p1G.size(),
                    cG.size(),
                    distArray[0], distArray[1], distArray[3], "", "", "", "", "", "",
                    sizes[0], sizes[1], sizes[2], sizes[3], sizes[4], sizes[5], "", "", "",
                    problem.getFitnessComputer().compute(cP).getValue(),
                    problem.getFitnessComputer().compute(p1P).getValue(),
                    ""
            );
            if (distArray[1] != null & distArray[3] != null) {
              current_valid++;
            }
          } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println(ex.toString());
          } catch (NullPointerException e) {

          }
        }
        index++;
      }
    } else if (operator instanceof AbstractCrossover) {
      while (current_valid < n_valid*repeats & index < p1Set.size()) {
        for (int h = 0; h < repeats; h++) {
          try {
            distArray = new Double[12];
            sizes = new Integer[9];

            /* array with the distances:
                                                |      p1-c      |      p2-c      |      p1-p2     |      c sizes      |      p1sizes     |      p2sizes     |  fitness values  |
                                                |  G   P(L)   F  |  G   P(L)   F  |  G   P(L)   F  | Size Depth Leaves |Size Depth Leaves |Size Depth Leaves |  c    p1    p2   |
             */
            p1G = (Genotype) p1Set.get(index);
            p2G = (Genotype) p2Set.get(index);
            cG = (Genotype) operator.apply(Arrays.asList(p1G, p2G)).get(0);
            distArray[0] = genoDist.d(p1G, cG);
            distArray[4] = genoDist.d(p2G, cG);
            distArray[8] = genoDist.d(p1G, p2G);
            cP = mapApply(mapper, cG);
            p1P = mapApply(mapper, p1G);
            p2P = mapApply(mapper, p2G);
            if (!cP.equals(Node.EMPTY_TREE)) {
              sizes[0] = cP.size();
              sizes[1] = cP.depth();
              sizes[2] = cP.leaves().size();
            }
            if (!p1P.equals(Node.EMPTY_TREE)) {
              sizes[3] = p1P.size();
              sizes[4] = p1P.depth();
              sizes[5] = p1P.leaves().size();
            }
            if (!p2P.equals(Node.EMPTY_TREE)) {
              sizes[6] = p2P.size();
              sizes[7] = p2P.depth();
              sizes[8] = p2P.leaves().size();
            }
            distArray[1] = distPhenoCalc(phenoDistSet.get("LeavesEdit"), p1P, cP);
            distArray[5] = distPhenoCalc(phenoDistSet.get("LeavesEdit"), p2P, cP);
            distArray[9] = distPhenoCalc(phenoDistSet.get("LeavesEdit"), p1P, p2P);
            distArray[3] = distFitCalc(problem.getFitnessComputer(), p1P, cP);
            distArray[7] = distFitCalc(problem.getFitnessComputer(), p2P, cP);
            distArray[11] = distFitCalc(problem.getFitnessComputer(), p1P, p2P);
            out.printf("%s;%s;%s;%d;%d;%f;%f;%f;%f;%f;%f;%f;%f;%f;%d;%d;%d;%d;%d;%d;%d;%d;%d;%f;%f;%f\n",
                    descr.get("problemName"),
                    descr.get("mapperName"),
                    descr.get("operatorName"),
                    p1G.size(),
                    cG.size(),
                    distArray[0], distArray[1], distArray[3], distArray[4], distArray[5], distArray[7], distArray[8], distArray[9], distArray[11],
                    sizes[0], sizes[1], sizes[2], sizes[3], sizes[4], sizes[5], sizes[6], sizes[7], sizes[8],
                    problem.getFitnessComputer().compute(cP).getValue(),
                    problem.getFitnessComputer().compute(p1P).getValue(),
                    problem.getFitnessComputer().compute(p2P).getValue()
            );
            if (distArray[1] != null & distArray[3] != null & distArray[5] != null & distArray[7] != null) {
              current_valid++;
            }
          } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println(ex.toString());
          } catch (NullPointerException e) {

          }
        }
        index++;
      }
    }
    System.out.println(index + "tries to get " + n_valid + "valid experiments");
  }
}
