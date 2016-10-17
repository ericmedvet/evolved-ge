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
import it.units.malelab.ege.distance.TreeEditDistance;
import it.units.malelab.ege.evolver.fitness.FitnessComputer;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
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
import it.units.malelab.ege.evolver.operator.TwoPointsCrossover;
import it.units.malelab.ege.evolver.validator.AnyValidator;
import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.mapper.BitsSGEMapper;
import it.units.malelab.ege.mapper.BreathFirstMapper;
import it.units.malelab.ege.mapper.HierarchicalMapper;
import it.units.malelab.ege.mapper.Mapper;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.PiGEMapper;
import it.units.malelab.ege.mapper.StandardGEMapper;
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
public class TestDistances {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        Map<String, Distance<BitsGenotype>> genotypeDistances = new LinkedHashMap<>();
        genotypeDistances.put("BitsEdit", new CachedDistance<>(new BitsGenotypeEditDistance()));
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
        //phenotypeDistances.put("TreeEdit", new CachedDistance<>(new TreeEditDistance<String>()));
        //prepare file
        PrintStream distancesFilePS = new PrintStream("dist" + dateForFile() + ".csv");
        distancesFilePS.println("Distance;Problem;Mapper;Operator;GenoSize;ChildSize;p1-c_G;p1-c_P;p1-c_F;p2-c_G;p2-c_P;p2-c_F;p1-p2_G;p1-p2_P;p1-p2_F;c_PSize;c_PDepth;c_PLength;p1_PSize;p1_PDepth;p1_PLength;p2_PSize;p2_PDepth;p2_PLength");
        //prepare problems
        Map<String, BenchmarkProblems.Problem> problems = new LinkedHashMap<>();
        //problems.put("harmonic", BenchmarkProblems.harmonicCurveProblem());
        //problems.put("poly4", BenchmarkProblems.classic4PolynomialProblem());
        //problems.put("max", BenchmarkProblems.max());
        //problems.put("text", BenchmarkProblems.text("Hello world!"));
        problems.put("santafe", BenchmarkProblems.santaFe());
        Mapper mapper;
        AbstractOperator operator;
        int n_individuals = 300;
        List[] genosSet1 = new List[5];
        List[] genosSet2 = new List[5];
        for (int i = 0; i < genosSet1.length; i++) {
            genosSet1[i] = (new RandomInitializer<>(new Random(i), new BitsGenotypeFactory((int) (128 * Math.pow(2, i))))).getGenotypes(n_individuals, new AnyValidator());
            genosSet2[i] = (new RandomInitializer<>(new Random(i + 5), new BitsGenotypeFactory((int) (128 * Math.pow(2, i))))).getGenotypes(n_individuals, new AnyValidator());
        }
        for (String distancePName : phenotypeDistances.keySet()) {
            descriptions.put("distanceName", distancePName);
            for (String problemName : problems.keySet()) {
                descriptions.put("problemName", problemName);
                BenchmarkProblems.Problem problem = problems.get(problemName);
                for (int m = 0; m < 6; m++) {
                    mapper = null;
                    Random r = new Random(m);
                    Grammar<String> grammar = problem.getGrammar();
                    switch (m) {
                        case 0:
                            descriptions.put("mapperName", "StdGE");
                            mapper = new StandardGEMapper<>(8, 5, grammar);
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
                            descriptions.put("mapperName", "BitsSGE");
                            mapper = new BitsSGEMapper<>(6, grammar);
                            break;
                        case 4:
                            descriptions.put("mapperName", "Hier");
                            mapper = new HierarchicalMapper<>(grammar);
                            break;
                        case 5:
                            descriptions.put("mapperName", "wHier");
                            mapper = new WeightedHierarchicalMapper<>(6, grammar);
                            break;
                    }
                    for (int o = 0; o < 7; o++) {
                        operator = null;
                        switch (o) {
                            case 0:
                                if (mapper.getClass() == BitsSGEMapper.class) {
                                    operator = new BitsSGECrossover((BitsSGEMapper) mapper, r);
                                    descriptions.put("operatorName", "BitsSGE");
                                }
                                break;
                            case 1:
                                operator = new LengthPreservingOnePointCrossover(r);
                                descriptions.put("operatorName", "LengthOneP");
                                break;
                            case 2:
                                operator = new LengthPreservingTwoPointsCrossover(r);
                                descriptions.put("operatorName", "LengthTwoP");
                                break;
                            case 3:
                                operator = new OnePointCrossover(r);
                                descriptions.put("operatorName", "OneP");
                                break;
                            case 4:
                                operator = new TwoPointsCrossover(r);
                                descriptions.put("operatorName", "TwoP");
                                break;
                            case 5:
                                operator = new ProbabilisticMutation(r, 0.01);
                                descriptions.put("operatorName", "ProbMut");
                                break;
                            case 6:
                                operator = new CompactFlipMutation(r);
                                descriptions.put("operatorName", "CFlipMut");
                                break;
                        }
                        if (operator != null && mapper != null) {
                            for (int i = 0; i < genosSet1.length; i++) {
                                //System.out.println(((BitsGenotype)genosSet1[i].get(0)).size());
                                calcDistances(genosSet1[i], genosSet2[i], mapper, operator, distancesFilePS, genotypeDistances.get("BitsEdit"), phenotypeDistances.get(distancePName), problem, descriptions);
                            }
                            System.out.printf("%s %s %s %s\n", descriptions.get("distanceName"), descriptions.get("problemName"), descriptions.get("mapperName"), descriptions.get("operatorName"));
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

    private static Node mapApply(Mapper mapper, BitsGenotype geno) {
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

    private static void calcDistances(List p1Set, List p2Set, Mapper mapper, AbstractOperator operator, PrintStream out, Distance<BitsGenotype> genoDist, Distance<Node<String>> phenoDist, Problem problem, Map<String, String> descr) {
        BitsGenotype cG, p1G, p2G;
        Node cP, p1P, p2P;
        Double[] distArray;
        Integer[] sizes;
        //System.out.println(p1Set.size());
        if (operator instanceof AbstractMutation) {
            for (int i = 0; i < p1Set.size(); i++) {
                try {
                    distArray = new Double[9];
                    sizes = new Integer[9];
                    /* array with the distances:
                                                |     p1-c    |    p2-c     |    p1-p2    |
                                                |  G   P   F  |  G   P   F  |  G   P   F  |
                     */

                    p1G = (BitsGenotype) p1Set.get(i);
                    cG = (BitsGenotype) operator.apply(Arrays.asList(p1G)).get(0);
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
                    distArray[1] = distPhenoCalc(phenoDist, p1P, cP);
                    distArray[2] = distFitCalc(problem.getFitnessComputer(), p1P, cP);
                    //out.printf("%s\n%s\n%s\n%s\n",p1G.toInt(),cG.toInt(),p1P.leaves().toString(),cP.leaves().toString());
                    out.printf("%s;%s;%s;%s;%d;%d;%f;%f;%f;%s;%s;%s;%s;%s;%s;%d;%d;%d;%d;%d;%d;%s;%s;%s\n",
                            descr.get("distanceName"),
                            descr.get("problemName"),
                            descr.get("mapperName"),
                            descr.get("operatorName"),
                            p1G.size(),
                            cG.size(),
                            distArray[0], distArray[1], distArray[2], "", "", "", "", "", "",
                            sizes[0], sizes[1], sizes[2], sizes[3], sizes[4], sizes[5], "", "", ""
                    );
                } catch (ArrayIndexOutOfBoundsException ex) {
                    System.out.println(ex.toString());
                }
            }
        } else if (operator instanceof AbstractCrossover) {
            for (int i = 0; i < p1Set.size(); i++) {
                try {
                    distArray = new Double[9];
                    sizes = new Integer[9];
                    /* array with the distances:
                                                |     p1-c    |    p2-c     |    p1-p2    |
                                                |  G   P   F  |  G   P   F  |  G   P   F  |
                     */

                    p1G = (BitsGenotype) p1Set.get(i);
                    p2G = (BitsGenotype) p2Set.get(i);
                    cG = (BitsGenotype) operator.apply(Arrays.asList(p1G, p2G)).get(0);
                    distArray[0] = genoDist.d(p1G, cG);
                    distArray[3] = genoDist.d(p2G, cG);
                    distArray[6] = genoDist.d(p1G, p2G);
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
                    distArray[1] = distPhenoCalc(phenoDist, p1P, cP);
                    distArray[4] = distPhenoCalc(phenoDist, p2P, cP);
                    distArray[7] = distPhenoCalc(phenoDist, p1P, p2P);
                    distArray[2] = distFitCalc(problem.getFitnessComputer(), p1P, cP);
                    distArray[5] = distFitCalc(problem.getFitnessComputer(), p2P, cP);
                    distArray[8] = distFitCalc(problem.getFitnessComputer(), p1P, p2P);
                    //out.printf("%s\n%s\n%s\n%s\n%s\n%s\n",p1G.toInt(),p2G.toInt(),cG.toInt(),p1P.leaves().toString(),p2P.leaves().toString(),cP.leaves().toString());
                    out.printf("%s;%s;%s;%s;%d;%d;%f;%f;%f;%f;%f;%f;%f;%f;%f;%d;%d;%d;%d;%d;%d;%d;%d;%d\n",
                            descr.get("distanceName"),
                            descr.get("problemName"),
                            descr.get("mapperName"),
                            descr.get("operatorName"),
                            p1G.size(),
                            cG.size(),
                            distArray[0], distArray[1], distArray[2], distArray[3], distArray[4], distArray[5], distArray[6], distArray[7], distArray[8],
                            sizes[0], sizes[1], sizes[2], sizes[3], sizes[4], sizes[5], sizes[6], sizes[7], sizes[8]
                    );
                } catch (ArrayIndexOutOfBoundsException ex) {
                    System.out.println(ex.toString());
                }
            }
        }
    }
}
