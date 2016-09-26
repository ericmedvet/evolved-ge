/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.distance.EditDistance;
import it.units.malelab.ege.evolver.validator.AnyValidator;
import it.units.malelab.ege.evolver.Configuration;
import it.units.malelab.ege.evolver.Evolver;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.StandardEvolver;
import it.units.malelab.ege.evolver.fitness.DistanceFitness;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.genotype.SGEGenotype;
import it.units.malelab.ege.evolver.genotype.SGEGenotypeFactory;
import it.units.malelab.ege.evolver.listener.EvolutionListener;
import it.units.malelab.ege.evolver.listener.SimpleGenerationPrinter;
import it.units.malelab.ege.evolver.selector.TournamentSelector;
import it.units.malelab.ege.evolver.operator.Copy;
import it.units.malelab.ege.evolver.operator.ProbabilisticMutation;
import it.units.malelab.ege.evolver.operator.SGECrossover;
import it.units.malelab.ege.evolver.operator.SGEMutation;
import it.units.malelab.ege.evolver.operator.TwoPointsCrossover;
import it.units.malelab.ege.mapper.BitsSGEMapper;
import it.units.malelab.ege.mapper.SGEMapper;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class Main {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
    Main main = new Main();
    main.evolveSGE();
    //System.out.println(Utils.resolveRecursiveGrammar(Utils.parseFromFile(new File("grammars/text.bnf")), 10));
  }

  /*
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
    final Distance<BitsGenotype> gd = new GenotypeEditDistance();
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
      mappers.put(new Pair<>(grammarName, "sge-5"), new BitsStructuralGEMapper<>(5, grammar));
      mappers.put(new Pair<>(grammarName, "sge-10"), new BitsStructuralGEMapper<>(10, grammar));
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
        if (mapperEntry.getValue() instanceof BitsStructuralGEMapper) {
          operators.add(new BitsSGECrossover((BitsStructuralGEMapper) mapperEntry.getValue(), random));
        }
        for (GeneticOperator operator : operators) {
          map.put(
                  new String[]{grammarName, mapperEntry.getKey().getSecond(), operator.getClass().getSimpleName()},
                  new Pair<>(mapperEntry.getValue(), operator));
        }
      }
    }
    for (int genotypeSize : genotypeSizes) {
      List<List<BitsGenotype>> parentsList = new ArrayList<>();
      for (int i = 0; i < numberOfRepetitions; i++) {
        parentsList.add(Arrays.asList(
                Utils.randomGenotype(genotypeSize, random),
                Utils.randomGenotype(genotypeSize, random)
        ));
      }
      LoadingCache<Triplet<String, String, BitsGenotype>, Node<String>> phenotypesCache = CacheBuilder
              .newBuilder()
              .maximumSize(10000)
              .build(new CacheLoader<Triplet<String, String, BitsGenotype>, Node<String>>() {
                @Override
                public Node<String> load(Triplet<String, String, BitsGenotype> key) throws Exception {
                  Mapper<String> mapper = mappers.get(new Pair<>(key.getFirst(), key.getSecond()));
                  try {
                    return mapper.map(key.getThird());
                  } catch (MappingException e) {
                    return Node.EMPTY_TREE;
                  }
                }
              });
      LoadingCache<List<BitsGenotype>, Double> genotypeDistancesCache = CacheBuilder
              .newBuilder()
              .maximumSize(10000)
              .build(new CacheLoader<List<BitsGenotype>, Double>() {
                @Override
                public Double load(List<BitsGenotype> key) throws Exception {
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
        for (List<BitsGenotype> parents : parentsList) {
          List<BitsGenotype> children = entry.getValue().getSecond().apply(parents);
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
  */

  private String dateForFile() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
    return simpleDateFormat.format(new Date());
  }
  
  private void evolveGE() throws IOException, ExecutionException, InterruptedException {
    Random random = new Random(1);
    Grammar<String> grammar = Utils.parseFromFile(new File("grammars/text.bnf"));
    Configuration<BitsGenotype, String> configuration = new Configuration<>(
            1000,
            1000,
            new RandomInitializer(random, new BitsGenotypeFactory(64)),
            new AnyValidator<BitsGenotype>(),
            //new StandardGEMapper<>(8, 10, grammar),
            //new HierarchicalMapper<>(grammar),
            //new WeightedHierarchicalMapper<>(10, grammar),
            new BitsSGEMapper<>(10, grammar),
            Arrays.asList(
                    new Configuration.GeneticOperatorConfiguration<>(new Copy<BitsGenotype>(), new TournamentSelector(100, random), 0.01d),
                    new Configuration.GeneticOperatorConfiguration<>(new TwoPointsCrossover(random), new TournamentSelector(5, random), 0.8d),
                    new Configuration.GeneticOperatorConfiguration<>(new ProbabilisticMutation(random, 0.01), new TournamentSelector(5, random), 0.19d)
            ),
            new DistanceFitness<>(Arrays.asList("hello world".split("")), new EditDistance<String>()));
    Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(1, configuration);
    List<EvolutionListener<BitsGenotype, String>> listeners = new ArrayList<>();
    listeners.add(new SimpleGenerationPrinter<BitsGenotype, String>(System.out, "g=%3d pop=%3d f=%3.0f %s%n"));
    evolver.go(listeners);
  }

  private void evolveSGE() throws IOException, ExecutionException, InterruptedException {
    Random random = new Random(1);
    Grammar<String> grammar = Utils.parseFromFile(new File("grammars/text.bnf"));
    SGEMapper<String> mapper = new SGEMapper<>(10, grammar);
    Configuration<SGEGenotype<String>, String> configuration = new Configuration<>(
            1000,
            1000,
            new RandomInitializer(random, new SGEGenotypeFactory<>(mapper)),
            new AnyValidator<SGEGenotype<String>>(),
            mapper,
            Arrays.asList(
                    new Configuration.GeneticOperatorConfiguration<>(new Copy<SGEGenotype<String>>(), new TournamentSelector(100, random), 0.01d),
                    new Configuration.GeneticOperatorConfiguration<>(new SGECrossover<String>(random), new TournamentSelector(5, random), 0.8d),
                    new Configuration.GeneticOperatorConfiguration<>(new SGEMutation<>(mapper, random), new TournamentSelector(5, random), 0.19d)
            ),
            new DistanceFitness<>(Arrays.asList("hello world".split("")), new EditDistance<String>()));
    Evolver<SGEGenotype<String>, String> evolver = new StandardEvolver<>(1, configuration);
    List<EvolutionListener<SGEGenotype<String>, String>> listeners = new ArrayList<>();
    listeners.add(new SimpleGenerationPrinter<SGEGenotype<String>, String>(System.out, "g=%3d pop=%3d f=%3.0f %s%n"));
    evolver.go(listeners);
  }

}
