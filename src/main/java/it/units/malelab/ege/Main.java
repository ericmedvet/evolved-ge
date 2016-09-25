/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Range;
import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.mapper.BreathFirstMapper;
import it.units.malelab.ege.mapper.HierarchicalMapper;
import it.units.malelab.ege.mapper.Mapper;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.StandardGEMapper;
import it.units.malelab.ege.operator.CompactFlipMutation;
import it.units.malelab.ege.operator.GeneticOperator;
import it.units.malelab.ege.operator.SparseFlipMutation;
import it.units.malelab.ege.distance.Distance;
import it.units.malelab.ege.distance.EditDistance;
import it.units.malelab.ege.distance.GenotypeEditDistance;
import it.units.malelab.ege.evolver.validator.AnyValidator;
import it.units.malelab.ege.evolver.Configuration;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.StandardEvolver;
import it.units.malelab.ege.evolver.fitness.DistanceFitness;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.listener.SimpleGenerationPrinter;
import it.units.malelab.ege.evolver.selector.TournamentSelector;
import it.units.malelab.ege.mapper.PiGEMapper;
import it.units.malelab.ege.mapper.StructuralGEMapper;
import it.units.malelab.ege.mapper.WeightedHierarchicalMapper;
import it.units.malelab.ege.operator.Copy;
import it.units.malelab.ege.operator.LengthPreservingOnePointCrossover;
import it.units.malelab.ege.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.operator.OnePointCrossover;
import it.units.malelab.ege.operator.ProbabilisticMutation;
import it.units.malelab.ege.operator.SGECrossover;
import it.units.malelab.ege.operator.TwoPointsCrossover;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class Main {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
    Main main = new Main();
    main.evolve();
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
    PrintStream filePs = new PrintStream("/home/eric/Scrivania/analysis." + dateForFile() + ".csv", "UTF-8");
    filePs.println("gSize;operator;grammar;mapper;i;dg1;dg2;dp1;dp2;p1PSize;p2PSize;c1PSize;c2PSize");
    Map<PrintStream, String> outputs = new HashMap<>();
    outputs.put(System.out, "%4d %10.10s %10.10s %15.15s || %4.0f %4.0f | %4.0f %4.0f || %4d %4d | %4d %4d || %4d %4d | %4d %4d || %4d %4d | %4d %4d ||%n");
    outputs.put(filePs, "%d;%s;%s;%s;%.0f;%.0f;%.0f;%.0f;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d%n");
    filePs.println("gSize;grammar;mapper;operator;gd0;gd1;pd0;pd1;p0l;p1l;c0l;c1l;p0d;p1d;c0d;c1d;p0s;p1s;c0s;c1s");
    Random random = new Random(1);
    int numberOfRepetitions = 1;
    int[] genotypeSizes = new int[]{128, 256, 512, 1024, 2048, 4096};
    //String[] grammarNames = new String[]{"max-grammar", "text", "santa-fe", "symbolic-regression"};
    String[] grammarNames = new String[]{"max-grammar"};
    final Distance<Genotype> gd = new GenotypeEditDistance();
    final Distance<List<String>> pd = new EditDistance<>();
    Map<String[], Pair<Mapper<String>, GeneticOperator>> map = new LinkedHashMap<>();
    System.out.println("Preparing mappers:");
    final Map<Pair<String, String>, Mapper<String>> mappers = new LinkedHashMap<>();
    for (String grammarName : grammarNames) {
      System.out.printf("\t%s%n", grammarName);
      Grammar<String> grammar = Utils.parseFromFile(new File("grammars/" + grammarName + ".bnf"));
      mappers.put(new Pair<>(grammarName, "ge-8-1"), new StandardGEMapper<>(8, 1, grammar));
      mappers.put(new Pair<>(grammarName, "ge-8-10"), new StandardGEMapper<>(8, 10, grammar));
      mappers.put(new Pair<>(grammarName, "bf-8-11"), new BreathFirstMapper<>(8, 11, grammar));
      mappers.put(new Pair<>(grammarName, "bf-8-10"), new BreathFirstMapper<>(8, 10, grammar));
      mappers.put(new Pair<>(grammarName, "pige-16-1"), new PiGEMapper<>(16, 1, grammar));
      mappers.put(new Pair<>(grammarName, "pige-16-10"), new PiGEMapper<>(16, 10, grammar));
      mappers.put(new Pair<>(grammarName, "sge-5"), new StructuralGEMapper<>(5, grammar));
      mappers.put(new Pair<>(grammarName, "sge-10"), new StructuralGEMapper<>(10, grammar));
      mappers.put(new Pair<>(grammarName, "hge"), new HierarchicalMapper<>(grammar));
      mappers.put(new Pair<>(grammarName, "whge-5"), new WeightedHierarchicalMapper<>(5, grammar));
      mappers.put(new Pair<>(grammarName, "whge-10"), new WeightedHierarchicalMapper<>(10, grammar));
      for (Map.Entry<Pair<String, String>, Mapper<String>> mapperEntry : mappers.entrySet()) {
        System.out.printf("\t\t%s%n", mapperEntry.getKey().getSecond());
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
                  new String[]{grammarName, mapperEntry.getKey().getSecond(), operator.getClass().getSimpleName()},
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
      LoadingCache<Triplet<String, String, Genotype>, Node<String>> phenotypesCache = CacheBuilder
              .newBuilder()
              .maximumSize(10000)
              .build(new CacheLoader<Triplet<String, String, Genotype>, Node<String>>() {
                @Override
                public Node<String> load(Triplet<String, String, Genotype> key) throws Exception {
                  Mapper<String> mapper = mappers.get(new Pair<>(key.getFirst(), key.getSecond()));
                  try {
                    return mapper.map(key.getThird());
                  } catch (MappingException e) {
                    return Node.EMPTY_TREE;
                  }
                }
              });
      LoadingCache<List<Genotype>, Double> genotypeDistancesCache = CacheBuilder
              .newBuilder()
              .maximumSize(10000)
              .build(new CacheLoader<List<Genotype>, Double>() {
                @Override
                public Double load(List<Genotype> key) throws Exception {
                  return gd.d(key.get(0), key.get(1));
                }
              });
      LoadingCache<List<Node<String>>, Double> phenotypeDistancesCache = CacheBuilder
              .newBuilder()
              .maximumSize(10000)
              .build(new CacheLoader<List<Node<String>>, Double>() {
                @Override
                public Double load(List<Node<String>> key) throws Exception {
                  if ((key.get(0).equals(Node.EMPTY_TREE))||(key.get(1).equals(Node.EMPTY_TREE))) {
                    return Double.NaN;
                  }
                  return pd.d(Utils.contents(key.get(0).leaves()), Utils.contents(key.get(1).leaves()));
                }
              });
      for (Map.Entry<String[], Pair<Mapper<String>, GeneticOperator>> entry : map.entrySet()) {
        for (List<Genotype> parents : parentsList) {
          List<Genotype> children = entry.getValue().getSecond().apply(parents);
          boolean binary = children.size() > 1;
          //compute/retrieve distances in genotype
          Double gd0 = genotypeDistancesCache.getUnchecked(Arrays.asList(parents.get(0), children.get(0)));
          Double gd1 = null;
          if (binary) {
            gd1 = genotypeDistancesCache.getUnchecked(Arrays.asList(parents.get(1), children.get(1)));
          }
          //compute/retrieve phenotypes
          Node<String> phenotypeParent0 = phenotypesCache.getUnchecked(new Triplet<>(entry.getKey()[0], entry.getKey()[1], parents.get(0)));
          Node<String> phenotypeChild0 = phenotypesCache.getUnchecked(new Triplet<>(entry.getKey()[0], entry.getKey()[1], children.get(0)));
          Node<String> phenotypeParent1 = Node.EMPTY_TREE;
          Node<String> phenotypeChild1 = Node.EMPTY_TREE;
          if (binary) {
            phenotypeParent1 = phenotypesCache.getUnchecked(new Triplet<>(entry.getKey()[0], entry.getKey()[1], parents.get(1)));
            phenotypeChild1 = phenotypesCache.getUnchecked(new Triplet<>(entry.getKey()[0], entry.getKey()[1], children.get(1)));
          }
          //compute phenotype distances
          Double pd0 = phenotypeDistancesCache.getUnchecked(Arrays.asList(phenotypeParent0, phenotypeChild0));
          Double pd1 = null;
          if (binary) {
            pd0 = phenotypeDistancesCache.getUnchecked(Arrays.asList(phenotypeParent1, phenotypeChild1));
          }
          //get tree measures
          Integer parent0Lenght = (phenotypeParent0.equals(Node.EMPTY_TREE)) ? null : phenotypeParent0.leaves().size();
          Integer parent1Lenght = (phenotypeParent1.equals(Node.EMPTY_TREE)) ? null : phenotypeParent1.leaves().size();
          Integer child0Lenght = (phenotypeChild0.equals(Node.EMPTY_TREE)) ? null : phenotypeChild0.leaves().size();
          Integer child1Lenght = (phenotypeChild1.equals(Node.EMPTY_TREE)) ? null : phenotypeChild1.leaves().size();
          Integer parent0Depth = (phenotypeParent0.equals(Node.EMPTY_TREE)) ? null : phenotypeParent0.depth();
          Integer parent1Depth = (phenotypeParent1.equals(Node.EMPTY_TREE)) ? null : phenotypeParent1.depth();
          Integer child0Depth = (phenotypeChild0.equals(Node.EMPTY_TREE)) ? null : phenotypeChild0.depth();
          Integer child1Depth = (phenotypeChild1.equals(Node.EMPTY_TREE)) ? null : phenotypeChild1.depth();
          Integer parent0Size = (phenotypeParent0.equals(Node.EMPTY_TREE)) ? null : phenotypeParent0.size();
          Integer parent1Size = (phenotypeParent1.equals(Node.EMPTY_TREE)) ? null : phenotypeParent1.size();
          Integer child0Size = (phenotypeChild0.equals(Node.EMPTY_TREE)) ? null : phenotypeChild0.size();
          Integer child1Size = (phenotypeChild1.equals(Node.EMPTY_TREE)) ? null : phenotypeChild1.size();
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
  
  private void evolve() throws IOException, ExecutionException, InterruptedException {
    Random random = new Random(1);
    Grammar<String> grammar = Utils.parseFromFile(new File("grammars/text.bnf"));
    Configuration<String> configuration = new Configuration<>(
            100,
            100,
            new RandomInitializer(1024, random),
            new AnyValidator(),
            new WeightedHierarchicalMapper<>(8, grammar), //new StandardGEMapper<>(8, 10, grammar),
            Arrays.asList(
                    new Configuration.GeneticOperatorConfiguration(new Copy(), new TournamentSelector(100, random), 0.01d),
                    new Configuration.GeneticOperatorConfiguration(new TwoPointsCrossover(random), new TournamentSelector(5, random), 0.8d),
                    new Configuration.GeneticOperatorConfiguration(new ProbabilisticMutation(random, 0.01), new TournamentSelector(5, random), 0.19d)
            ),
            new DistanceFitness<>(Arrays.asList("Hello".split("")), new EditDistance<String>()));
    Evolver<String> evolver = new StandardEvolver<>(configuration);
    List<EvolutionListener<String>> listeners = new ArrayList<>();
    listeners.add(new SimpleGenerationPrinter<String>(System.out, "g=%3d pop=%3d f=%3.0f %s%n"));
    evolver.go(listeners);
  }

}
