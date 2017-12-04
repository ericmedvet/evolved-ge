/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import it.units.malelab.ege.benchmark.KLandscapes;
import it.units.malelab.ege.benchmark.Text;
import it.units.malelab.ege.benchmark.booleanfunction.MultipleOutputParallelMultiplier;
import it.units.malelab.ege.benchmark.symbolicregression.Nguyen7;
import it.units.malelab.ege.benchmark.symbolicregression.Pagie1;
import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.evolver.StandardConfiguration;
import it.units.malelab.ege.core.evolver.StandardEvolver;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.initializer.RandomInitializer;
import it.units.malelab.ege.core.listener.CollectorGenerationLogger;
import it.units.malelab.ege.core.listener.EvolutionImageSaverListener;
import it.units.malelab.ege.core.listener.EvolverListener;
import it.units.malelab.ege.core.listener.collector.BestPrinter;
import it.units.malelab.ege.core.listener.collector.Diversity;
import it.units.malelab.ege.core.listener.collector.NumericFirstBest;
import it.units.malelab.ege.core.listener.collector.Population;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.operator.GeneticOperator;
import it.units.malelab.ege.core.ranker.ComparableRanker;
import it.units.malelab.ege.core.selector.IndividualComparator;
import it.units.malelab.ege.core.selector.LastWorst;
import it.units.malelab.ege.core.selector.Tournament;
import it.units.malelab.ege.core.validator.Any;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.ge.genotype.SGEGenotype;
import it.units.malelab.ege.ge.genotype.SGEGenotypeFactory;
import it.units.malelab.ege.ge.mapper.HierarchicalMapper;
import it.units.malelab.ege.ge.mapper.SGEMapper;
import it.units.malelab.ege.ge.mapper.StandardGEMapper;
import it.units.malelab.ege.ge.mapper.WeightedHierarchicalMapper;
import it.units.malelab.ege.ge.operator.LengthPreservingTwoPointsCrossover;
import it.units.malelab.ege.ge.operator.ProbabilisticMutation;
import it.units.malelab.ege.ge.operator.SGECrossover;
import it.units.malelab.ege.ge.operator.SGEMutation;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import org.apache.commons.math3.stat.StatUtils;

/**
 *
 * @author eric
 */
public class DUMapper {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

    /*double[][][] sgeData = buildSGEData(100, 6, new Nguyen7(0));
     saveImages("/home/eric/experiments/dumapper/sge6-nguyen7-1_%s.png", false, 4, sgeData);
     double[][][] whgeData = buildGEData("whge", 100, 512, new Nguyen7(0));
     saveImages("/home/eric/experiments/dumapper/whge-nguyen7-1_%s.png", false, 4, whgeData);
     double[][][] geData = buildGEData("ge", 100, 512, new Nguyen7(0));
     saveImages("/home/eric/experiments/dumapper/ge-nguyen7-1_%s.png", false, 4, geData);
     System.exit(0);
    
     double[][][] neatData = getNeatData("/home/eric/experiments/dumapper/neat/NEATPopulations", "targetANDcollision_100.0w1_0.1w2_(phase1_from1to300)_NEATPopulationEvolved(%sof300)_100pop_300gen_10cars_3x30.0sec_Run1.eg", 300);
     saveImages("/home/eric/experiments/dumapper/neat-1_%s.png", false, 4, neatData);
     System.exit(0);*/
    /*double[][][] gomeaData = getGomeaData("/home/eric/experiments/dumapper/gomea-1/LT_nguyen7/30393", "population_%d.dat", 100, 127);
     saveImages("/home/eric/experiments/dumapper/gomea-lt-nguyen7-30393_%s.png", false, 4, gomeaData);
     System.exit(0);*/
    /*double[][][] gsgpData = getGsgpData("/home/eric/experiments/dumapper/gsgp/nguyen7/Same_Init_Pop/Tournament_size_4/1/", "blocks.txt", 101, 100, 100);
     saveImages("/home/eric/experiments/dumapper/gsgp-same-t4-nguyen7-1_%s.png", false, 2, gsgpData);*/
    /*
     //many gomea runs
     String baseDir = "/home/eric/experiments/dumapper/gomea-1/LT_nguyen7";
     File dir = new File(baseDir);
     int i = 0;
     int runs = 10;
     double[][][][] datas = new double[runs][][][];
     for (File runDir : dir.listFiles()) {
     if (runDir.isDirectory()) {
     System.out.println(runDir);
     datas[i] = getGomeaData(runDir.toString(), "population_%d.dat", 100, 127);
     saveImages(String.format("/home/eric/experiments/dumapper/gomea-lt-nguyen7-%d_%%s.png", i), false, 4, datas[i]);
     i = i+1;
     if (i>=runs) {
     break;
     }
     }
     }
     saveImages("/home/eric/experiments/dumapper/gomea-lt-nguyen7-10runs_%s.png", false, 4, merge(datas));*/

    /*//many gsgp runs
     String baseDir = "/home/eric/experiments/dumapper/gsgp/nguyen7/Different_Init_Pop/Tournament_size_4";
     File dir = new File(baseDir);
     int i = 0;
     int runs = 10;
     double[][][][] datas = new double[runs][][][];
     for (File runDir : dir.listFiles()) {
     if (runDir.isDirectory()) {
     System.out.println(runDir);
     datas[i] = getGsgpData(runDir.toString(), "blocks.txt", 101, 100, 100);
     saveImages(String.format("/home/eric/experiments/dumapper/gsgp-diff-t4-nguyen7-%d_%%s.png", i), false, 4, datas[i]);
     i = i+1;
     if (i>=runs) {
     break;
     }
     }
     }
     saveImages("/home/eric/experiments/dumapper/gsgp-diff-t4-nguyen7-10runs_%s.png", false, 4, merge(datas));*/
    /*//many runs whge
     double[][][][] datas = new double[10][][][];
     for (int i = 0; i<10; i++) {
     datas[i] = buildGEData("whge", 100, 512, new Nguyen7(0), i);
     saveImages(String.format("/home/eric/experiments/dumapper/whge-nguyen7-%d_%%s.png", i), false, 1, datas[i]);
     }
     saveImages("/home/eric/experiments/dumapper/whge-nguyen7-10runs_%s.png", false, 1, merge(datas));*/
    /*//gsgp diff tournament size
     saveImages("/home/eric/experiments/dumapper/gsgp-same-random-nguyen7-1_%s.png", false, 2, getGsgpData("/home/eric/experiments/dumapper/gsgp/nguyen7/Same_Init_Pop/random_selection/1/", "blocks.txt", 51, 100, 100));
     saveImages("/home/eric/experiments/dumapper/gsgp-same-t2-nguyen7-1_%s.png", false, 2, getGsgpData("/home/eric/experiments/dumapper/gsgp/nguyen7/Same_Init_Pop/Tournament_size_2/1/", "blocks.txt", 51, 100, 100));
     saveImages("/home/eric/experiments/dumapper/gsgp-same-t4-nguyen7-1_%s.png", false, 2, getGsgpData("/home/eric/experiments/dumapper/gsgp/nguyen7/Same_Init_Pop/Tournament_size_4/1/", "blocks.txt", 51, 100, 100));
     saveImages("/home/eric/experiments/dumapper/gsgp-same-t6-nguyen7-1_%s.png", false, 2, getGsgpData("/home/eric/experiments/dumapper/gsgp/nguyen7/Same_Init_Pop/Tournament_size_6/1/", "blocks.txt", 51, 100, 100));
     saveImages("/home/eric/experiments/dumapper/gsgp-same-t8-nguyen7-1_%s.png", false, 2, getGsgpData("/home/eric/experiments/dumapper/gsgp/nguyen7/Same_Init_Pop/Tournament_size_8/1/", "blocks.txt", 51, 100, 100));
     saveImages("/home/eric/experiments/dumapper/gsgp-same-t10-nguyen7-1_%s.png", false, 2, getGsgpData("/home/eric/experiments/dumapper/gsgp/nguyen7/Same_Init_Pop/Tournament_size_10/1/", "blocks.txt", 51, 100, 100));*/
    /*//different whge problems
     saveImages("/home/eric/experiments/dumapper/whge200-nguyen7-1_%s.png", false, 2, buildGEData("whge", 50, 200, new Nguyen7(0), 1));
     saveImages("/home/eric/experiments/dumapper/whge200-pagie1-1_%s.png", false, 2, buildGEData("whge", 50, 200, new Pagie1(), 1));
     saveImages("/home/eric/experiments/dumapper/whge200-text-1_%s.png", false, 2, buildGEData("whge", 50, 200, new Text(), 1));
     saveImages("/home/eric/experiments/dumapper/whge200-kland4-1_%s.png", false, 2, buildGEData("whge", 50, 200, new KLandscapes(4), 1));
     saveImages("/home/eric/experiments/dumapper/whge200-kland8-1_%s.png", false, 2, buildGEData("whge", 50, 200, new KLandscapes(8), 1));
     saveImages("/home/eric/experiments/dumapper/whge200-mopm2-1_%s.png", false, 2, buildGEData("whge", 50, 200, new MultipleOutputParallelMultiplier(2), 1));
     saveImages("/home/eric/experiments/dumapper/whge200-mopm4-1_%s.png", false, 2, buildGEData("whge", 50, 200, new MultipleOutputParallelMultiplier(4), 1));*/
    /*//diff selective pressure whge
     for (int i = 2; i<=10; i = i+2) {
     saveImages(String.format("/home/eric/experiments/dumapper/whge-kland5-t%d-_%%s.png", i), false, 1, buildGEData("whge", 50, 200, new KLandscapes(5), 1, i));
     }*/
    /*saveImages("/home/eric/experiments/dumapper/gsgp-nguyen7-1_%s.png", false, 2, getGsgpData("/home/eric/experiments/dumapper/gsgp/nguyen7/Same_Init_Pop/Tournament_size_4/1/", "blocks.txt", 51, 100, 100));
     saveImages("/home/eric/experiments/dumapper/gsgp-airfoil-1_%s.png", false, 2, getGsgpData("/home/eric/experiments/dumapper/gsgp/airfoil/1/", "blocks.txt", 51, 100, 100));
     saveImages("/home/eric/experiments/dumapper/gsgp-concrete-1_%s.png", false, 2, getGsgpData("/home/eric/experiments/dumapper/gsgp/concrete/1/", "blocks.txt", 51, 100, 100));
     saveImages("/home/eric/experiments/dumapper/gsgp-slump-1_%s.png", false, 2, getGsgpData("/home/eric/experiments/dumapper/gsgp/slump/1/", "blocks.txt", 51, 100, 100));
     saveImages("/home/eric/experiments/dumapper/gsgp-yacht-1_%s.png", false, 2, getGsgpData("/home/eric/experiments/dumapper/gsgp/yacht/1/", "blocks.txt", 51, 100, 100));*/
    saveImages("/home/eric/experiments/dumapper/neat-1_%s.png", false, 4, getNeatData("/home/eric/experiments/dumapper/neat/1-150T_151-400T+C", "pop-%s.eg", 300));

  }

  private static double[][][] merge(double[][][][] datas) {
    double[][][] merged = new double[][][]{
      new double[datas[0][0].length][datas[0][0][0].length],
      new double[datas[0][0].length][datas[0][0][0].length]
    };
    for (double[][][] data : datas) {
      for (int t = 0; t < merged.length; t++) {
        for (int g = 0; g < merged[0].length; g++) {
          for (int i = 0; i < merged[0][0].length; i++) {
            merged[t][g][i] = merged[t][g][i] + data[t][g][i] / (double) datas.length;
          }
        }
      }
    }
    return merged;
  }

  private static void saveImages(String fileName, boolean margin, int scale, double[][][] data) {
    saveImage(String.format(fileName, "d"), margin, scale, data[0]);
    saveImage(String.format(fileName, "u"), margin, scale, data[1]);
    saveImage(String.format(fileName, "du"), margin, scale, data[0], data[1]);
  }

  private static double[][][] buildGEData(String mapperName, int generations, int genotypeSize, Problem problem, long seed, int tournamentSize) throws InterruptedException, ExecutionException {
    Mapper<BitsGenotype, String> mapper;
    if (mapperName.equals("whge")) {
      mapper = new WeightedHierarchicalMapper<>(2, problem.getGrammar());
    } else if (mapperName.equals("hge")) {
      mapper = new HierarchicalMapper<>(problem.getGrammar());
    } else {
      mapper = new StandardGEMapper<>(8, 10, problem.getGrammar());
    }
    StandardConfiguration<BitsGenotype, String, NumericFitness> configuration = new StandardConfiguration<>(
            500,
            generations,
            new RandomInitializer<>(new BitsGenotypeFactory(genotypeSize)),
            new Any<BitsGenotype>(),
            mapper,
            new Utils.MapBuilder<GeneticOperator<BitsGenotype>, Double>()
            .put(new LengthPreservingTwoPointsCrossover(), 0.8d)
            .put(new ProbabilisticMutation(0.01), 0.2d).build(),
            new ComparableRanker<>(new IndividualComparator<BitsGenotype, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
            new Tournament<Individual<BitsGenotype, String, NumericFitness>>(tournamentSize),
            new LastWorst<Individual<BitsGenotype, String, NumericFitness>>(),
            500,
            true,
            problem);
    StandardEvolver evolver = new StandardEvolver(configuration, false);
    List<EvolverListener> listeners = new ArrayList<>();
    final EvolutionImageSaverListener evolutionImageSaverListener = new EvolutionImageSaverListener(Collections.EMPTY_MAP, null, EvolutionImageSaverListener.ImageType.DU);
    listeners.add(evolutionImageSaverListener);
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP, System.out, true, 10, " ", " | ",
            new Population(),
            new NumericFirstBest(false, problem.getTestingFitnessComputer(), "%6.2f"),
            new Diversity(),
            new BestPrinter(problem.getPhenotypePrinter(), "%30.30s")
    ));
    ExecutorService executorService = Executors.newCachedThreadPool();
    evolver.solve(executorService, new Random(seed), listeners);
    return evolutionImageSaverListener.getLastEvolutionData();
  }

  private static double[][][] buildSGEData(int generations, int depth, Problem problem) throws InterruptedException, ExecutionException {
    SGEMapper<String> m = new SGEMapper<>(depth, problem.getGrammar());
    StandardConfiguration<SGEGenotype<String>, String, NumericFitness> configuration = new StandardConfiguration<>(
            500,
            generations,
            new RandomInitializer<>(new SGEGenotypeFactory<>(m)),
            new Any<SGEGenotype<String>>(),
            m,
            new Utils.MapBuilder<GeneticOperator<SGEGenotype<String>>, Double>()
            .put(new SGECrossover<String>(), 0.8d)
            .put(new SGEMutation<>(0.01d, m), 0.2d).build(),
            new ComparableRanker<>(new IndividualComparator<SGEGenotype<String>, String, NumericFitness>(IndividualComparator.Attribute.FITNESS)),
            new Tournament<Individual<SGEGenotype<String>, String, NumericFitness>>(3),
            new LastWorst<Individual<SGEGenotype<String>, String, NumericFitness>>(),
            500,
            true,
            problem);
    StandardEvolver evolver = new StandardEvolver(configuration, false);
    List<EvolverListener> listeners = new ArrayList<>();
    final EvolutionImageSaverListener evolutionImageSaverListener = new EvolutionImageSaverListener(Collections.EMPTY_MAP, null, EvolutionImageSaverListener.ImageType.DU);
    listeners.add(evolutionImageSaverListener);
    listeners.add(new CollectorGenerationLogger<>(
            Collections.EMPTY_MAP, System.out, true, 10, " ", " | ",
            new Population(),
            new NumericFirstBest(false, problem.getTestingFitnessComputer(), "%6.2f"),
            new Diversity(),
            new BestPrinter(problem.getPhenotypePrinter(), "%30.30s")
    ));
    ExecutorService executorService = Executors.newCachedThreadPool();
    evolver.solve(executorService, new Random(1), listeners);
    return evolutionImageSaverListener.getLastEvolutionData();
  }

  private static double[][][] getGomeaData(String baseDir, String fileNamePattern, int generations, int genotypeSize) throws IOException {
    double[][] usages = new double[generations][];
    Set<Character>[] domains = new Set[genotypeSize];
    Multiset<Character>[][] symbols = new Multiset[generations][];
    for (int i = 0; i < genotypeSize; i++) {
      domains[i] = new HashSet<>();
    }
    for (int g = 0; g < generations; g++) {
      symbols[g] = new Multiset[genotypeSize];
      for (int i = 0; i < genotypeSize; i++) {
        symbols[g][i] = HashMultiset.create();
      }
      usages[g] = new double[genotypeSize];
      BufferedReader reader = Files.newBufferedReader(FileSystems.getDefault().getPath(baseDir, String.format(fileNamePattern, g)));
      String line;
      int populationSize = 0;
      while ((line = reader.readLine()) != null) {
        populationSize = populationSize + 1;
        String[] pieces = line.split(" ");
        String genotype = pieces[0];
        for (int i = 0; i < genotypeSize; i++) {
          domains[i].add(genotype.charAt(i));
          symbols[g][i].add(genotype.charAt(i));
        }
        for (int i = 2; i < pieces.length; i++) {
          int intronIndex = Integer.parseInt(pieces[i]);
          usages[g][intronIndex] = usages[g][intronIndex] + 1;
        }
      }
      for (int i = 0; i < genotypeSize; i++) {
        usages[g][i] = (populationSize - usages[g][i]) / populationSize;
      }
      reader.close();
    }
    double[][] diversities = new double[generations][];
    for (int g = 0; g < generations; g++) {
      diversities[g] = new double[genotypeSize];
      for (int i = 0; i < genotypeSize; i++) {
        diversities[g][i] = Utils.multisetDiversity(symbols[g][i], domains[i]);
      }
    }
    return new double[][][]{diversities, usages};
  }

  private static double[][][] getNeatData(String baseDir, String fileNamePattern, int generations) throws IOException {
    List<List<Map<Integer, Pair<Double, Double>>>> data = new ArrayList<>();
    int maxInnovationNumber = 0;
    for (int g = 0; g < generations; g++) {
      List<Map<Integer, Pair<Double, Double>>> currentPopulation = new ArrayList<>();
      BufferedReader reader = Files.newBufferedReader(FileSystems.getDefault().getPath(baseDir, String.format(fileNamePattern, g + 1)));
      String line;
      boolean isInPopulation = false;
      Map<Integer, Pair<Double, Double>> currentIndividual = null;
      while ((line = reader.readLine()) != null) {
        if (line.equals("[NEAT-POPULATION:SPECIES]")) {
          isInPopulation = true;
          continue;
        }
        if (!isInPopulation) {
          continue;
        }
        if (line.startsWith("\"g\"")) {
          if (currentIndividual != null) {
            //save current individual
            currentPopulation.add(currentIndividual);
          }
          currentIndividual = new HashMap<>();
        }
        if (line.startsWith("\"n\"") || line.startsWith("\"l\"")) {
          String[] pieces = line.split(",");
          int innovationNumber = Integer.parseInt(pieces[pieces.length - 1]);
          maxInnovationNumber = Math.max(innovationNumber, maxInnovationNumber);
          double used = 1;
          double value = 1;
          if (line.startsWith("\"l\"")) {
            value = Double.parseDouble(pieces[pieces.length - 2]);
            used = Double.parseDouble(pieces[2]);
          }
          currentIndividual.put(innovationNumber, new Pair<>(used, value));
        }
      }
      reader.close();
      data.add(currentPopulation);
    }
    //populate arrays
    double[][] usages = new double[generations][];
    double[][] diversities = new double[generations][];
    for (int g = 0; g < generations; g++) {
      usages[g] = new double[maxInnovationNumber];
      diversities[g] = new double[maxInnovationNumber];
      List<Map<Integer, Pair<Double, Double>>> currentPopulation = data.get(g);
      //populate usages
      double[][] values = new double[maxInnovationNumber][];
      double[] localUsages = new double[maxInnovationNumber];
      for (int i = 0; i < maxInnovationNumber; i++) {
        values[i] = new double[currentPopulation.size()];
      }
      for (int p = 0; p < currentPopulation.size(); p++) {
        for (int i = 0; i < maxInnovationNumber; i++) {
          double value = 0;
          double used = 0;
          if (currentPopulation.get(p).containsKey(i)) {
            Pair<Double, Double> pair = currentPopulation.get(p).get(i);
            value = pair.getSecond();
            used = pair.getFirst();
          }
          values[i][p] = value;
          localUsages[i] = localUsages[i] + used;
        }
      }
      for (int i = 0; i < maxInnovationNumber; i++) {
        usages[g][i] = localUsages[i] / (double) currentPopulation.size();
        diversities[g][i] = normalizedVar(values[i]);
      }
    }
    return new double[][][]{diversities, usages};
  }

  private static double[][][] getNeatData2(String baseDir, String fileNamePattern, int generations) throws IOException {
    List<List<Map<Integer, Pair<Double, Double>>>> data = new ArrayList<>();
    int maxInnovationNumber = 0;
    for (int g = 0; g < generations; g++) {
      List<Map<Integer, Pair<Double, Double>>> currentPopulation = new ArrayList<>();
      BufferedReader reader = Files.newBufferedReader(FileSystems.getDefault().getPath(baseDir, String.format(fileNamePattern, g + 1)));
      String line;
      boolean isInPopulation = false;
      Map<Integer, Pair<Double, Double>> currentIndividual = null;
      Set<Integer> currentIndividualConnectedNodes = new HashSet<>();
      while ((line = reader.readLine()) != null) {
        if (line.equals("[NEAT-POPULATION:SPECIES]")) {
          isInPopulation = true;
          continue;
        }
        if (!isInPopulation) {
          continue;
        }
        if (line.startsWith("\"g\"")) {
          if (currentIndividual != null) {
            //compute data for nodes
            for (int usedNode : currentIndividualConnectedNodes) {
              currentIndividual.put(usedNode, new Pair<>(1d, 1d));
            }
            //save current individual
            currentPopulation.add(currentIndividual);
          }
          currentIndividual = new HashMap<>();
          currentIndividualConnectedNodes.clear();
        }
        if (line.startsWith("\"n\"") || line.startsWith("\"l\"")) {
          String[] pieces = line.split(",");
          int innovationNumber = Integer.parseInt(pieces[pieces.length - 1]);
          maxInnovationNumber = Math.max(innovationNumber, maxInnovationNumber);
          double used = 0;
          double value = 0;
          if (line.startsWith("\"l\"")) {
            value = Double.parseDouble(pieces[pieces.length - 2]);
            used = Double.parseDouble(pieces[2]);
            //get connected nodes
            if (used == 1) {
              currentIndividualConnectedNodes.add(Integer.parseInt(pieces[3]));
              currentIndividualConnectedNodes.add(Integer.parseInt(pieces[4]));
            }
          }
          currentIndividual.put(innovationNumber, new Pair<>(used, value));
        }
      }
      reader.close();
      data.add(currentPopulation);
    }
    //populate arrays
    double[][] usages = new double[generations][];
    double[][] diversities = new double[generations][];
    for (int g = 0; g < generations; g++) {
      usages[g] = new double[maxInnovationNumber];
      diversities[g] = new double[maxInnovationNumber];
      List<Map<Integer, Pair<Double, Double>>> currentPopulation = data.get(g);
      //populate usages
      double[][] values = new double[maxInnovationNumber][];
      double[] localUsages = new double[maxInnovationNumber];
      for (int i = 0; i < maxInnovationNumber; i++) {
        values[i] = new double[currentPopulation.size()];
      }
      for (int p = 0; p < currentPopulation.size(); p++) {
        for (int i = 0; i < maxInnovationNumber; i++) {
          double value = 0;
          double used = 0;
          if (currentPopulation.get(p).containsKey(i)) {
            Pair<Double, Double> pair = currentPopulation.get(p).get(i);
            value = pair.getSecond();
            used = pair.getFirst();
          }
          values[i][p] = value;
          localUsages[i] = localUsages[i] + used;
        }
      }
      for (int i = 0; i < maxInnovationNumber; i++) {
        usages[g][i] = localUsages[i] / (double) currentPopulation.size();
        diversities[g][i] = normalizedVar(values[i]);
      }
    }
    return new double[][][]{diversities, usages};
  }

  private static double[][][] getNeatData3(String baseDir, String fileNamePattern, int generations) throws IOException {
    List<List<Map<Integer, Multimap<Integer, Integer>>>> data = new ArrayList<>();
    Map<Integer, String> nodeTypesMap = new HashMap<>();
    for (int g = 0; g < generations; g++) {
      List<Map<Integer, Multimap<Integer, Integer>>> currentPopulation = new ArrayList<>();
      BufferedReader reader = Files.newBufferedReader(FileSystems.getDefault().getPath(baseDir, String.format(fileNamePattern, g + 1)));
      String line;
      boolean isInPopulation = false;
      Map<Integer, Multimap<Integer, Integer>> currentIndividual = null;
      while ((line = reader.readLine()) != null) {
        if (line.equals("[NEAT-POPULATION:SPECIES]")) {
          isInPopulation = true;
          continue;
        }
        if (!isInPopulation) {
          continue;
        }
        if (line.startsWith("\"g\"")) {
          if (currentIndividual != null) {
            //save current individual
            currentPopulation.add(currentIndividual);
          }
          currentIndividual = new HashMap<>();
        }
        if (line.startsWith("\"n\"")) {
          String[] pieces = line.split(",");
          nodeTypesMap.put(Integer.parseInt(pieces[4]), pieces[3].replaceAll("\"", ""));
          currentIndividual.put(Integer.parseInt(pieces[4]), (Multimap) HashMultimap.create());
        } else if (line.startsWith("\"l\"")) {
          String[] pieces = line.split(",");
          int from = Integer.parseInt(pieces[3]);
          int to = Integer.parseInt(pieces[4]);
          if (currentIndividual.get(from) == null) {
            currentIndividual.put(from, (Multimap) HashMultimap.create());
          }
          if (currentIndividual.get(to) == null) {
            currentIndividual.put(to, (Multimap) HashMultimap.create());
          }
          currentIndividual.get(from).put(1, to);
          currentIndividual.get(to).put(-1, from);
        }
      }
      reader.close();
      data.add(currentPopulation);
    }
    //build node innovation numbers
    String[] nodeTypes = new String[]{"i", "b", "h", "o"};
    List<Integer> nodeINs = new ArrayList<>();
    for (String nodeType : nodeTypes) {
      List<Integer> typeNodeINs = new ArrayList<>();
      for (Integer in : nodeTypesMap.keySet()) {
        if (nodeTypesMap.get(in).equals(nodeType)) {
          typeNodeINs.add(in);
        }
      }
      Collections.sort(typeNodeINs);
      nodeINs.addAll(typeNodeINs);
    }
    //populate arrays
    double[][] usages = new double[generations][];
    double[][] diversities = new double[generations][];
    for (int g = 0; g < generations; g++) {
      usages[g] = new double[nodeINs.size()];
      diversities[g] = new double[nodeINs.size()];
      List<Map<Integer, Multimap<Integer, Integer>>> currentPopulation = data.get(g);
      //populate usages, diversities
      int i = 0;
      for (int nodeIN : nodeINs) {
        double[] localUsages = new double[currentPopulation.size()];
        Multiset<Set<Integer>> froms = HashMultiset.create();
        Multiset<Set<Integer>> tos = HashMultiset.create();
        int c = 0;
        for (Map<Integer, Multimap<Integer, Integer>> currentIndividual : currentPopulation) {
          if (nodeTypesMap.get(nodeIN).equals("i") || nodeTypesMap.get(nodeIN).equals("b")) {
            if (currentIndividual.containsKey(nodeIN)) {
              localUsages[c] = currentIndividual.get(nodeIN).get(1).isEmpty() ? 0 : 1;
              tos.add(new HashSet<>(currentIndividual.get(nodeIN).get(1)));
            } else {
              tos.add(Collections.EMPTY_SET);
            }
          } else if (nodeTypesMap.get(nodeIN).equals("h")) {
            if (currentIndividual.containsKey(nodeIN)) {
              localUsages[c] = (currentIndividual.get(nodeIN).get(-1).isEmpty() ? 0 : 0.5) + (currentIndividual.get(nodeIN).get(1).isEmpty() ? 0 : 0.5);
              tos.add(new HashSet<>(currentIndividual.get(nodeIN).get(1)));
              froms.add(new HashSet<>(currentIndividual.get(nodeIN).get(-1)));
            } else {
              tos.add(Collections.EMPTY_SET);
              froms.add(Collections.EMPTY_SET);
            }
          } else if (nodeTypesMap.get(nodeIN).equals("o")) {
            if (currentIndividual.containsKey(nodeIN)) {
              localUsages[c] = currentIndividual.get(nodeIN).get(-1).isEmpty() ? 0 : 1;
              froms.add(new HashSet<>(currentIndividual.get(nodeIN).get(-1)));
            } else {
              froms.add(Collections.EMPTY_SET);
            }
          }
          c = c + 1;
        }
        usages[g][i] = StatUtils.mean(localUsages);
        if (nodeTypesMap.get(nodeIN).equals("i") || nodeTypesMap.get(nodeIN).equals("b")) {
          diversities[g][i] = Utils.multisetDiversity(tos, tos.elementSet());
        } else if (nodeTypesMap.get(nodeIN).equals("h")) {
          diversities[g][i] = Utils.multisetDiversity(tos, tos.elementSet()) / 2 + Utils.multisetDiversity(froms, tos.elementSet()) / 2;
        } else if (nodeTypesMap.get(nodeIN).equals("o")) {
          diversities[g][i] = Utils.multisetDiversity(froms, tos.elementSet());
        }
        i = i + 1;
      }
    }
    return new double[][][]{diversities, usages};
  }

  private static double[][][] getGsgpData(String baseDir, String fileName, int generations, int genotypeSize, int populationSize) throws IOException {
    double[][] usages = new double[generations][];
    double[][] diversities = new double[generations][];
    BufferedReader reader = Files.newBufferedReader(FileSystems.getDefault().getPath(baseDir, fileName));
    for (int g = 0; g < generations; g++) {
      usages[g] = new double[genotypeSize];
      diversities[g] = new double[genotypeSize];
      double[][] popGenes = new double[genotypeSize][];
      for (int i = 0; i < genotypeSize; i++) {
        popGenes[i] = new double[populationSize];
      }
      for (int p = 0; p < populationSize; p++) {
        String line = reader.readLine();
        String[] pieces = line.split("\\s");
        double[] genes = new double[genotypeSize];
        double maxGene = 0d;
        for (int i = 0; i < genotypeSize; i++) {
          //int gene = Integer.parseInt(pieces[i]);
          double gene = Double.parseDouble(pieces[i]);
          genes[i] = gene;
          maxGene = Math.max(maxGene, gene);
          popGenes[i][p] = gene;
        }
        for (int i = 0; i < genotypeSize; i++) {
          usages[g][i] = usages[g][i] + genes[i] / maxGene;
        }
      }
      for (int i = 0; i < genotypeSize; i++) {
        usages[g][i] = usages[g][i] / populationSize;
        diversities[g][i] = normalizedVar(popGenes[i]);
      }
    }
    reader.close();
    return new double[][][]{diversities, usages};
  }

  private static double normalizedVar(double[] values) {
    double minValue = Double.POSITIVE_INFINITY;
    double maxValue = Double.NEGATIVE_INFINITY;
    //rescale
    for (double value : values) {
      minValue = Math.min(value, minValue);
      maxValue = Math.max(value, maxValue);
    }
    if (minValue == maxValue) {
      return 0;
    }
    for (int i = 0; i < values.length; i++) {
      values[i] = (values[i] - minValue) / (maxValue - minValue);
    }
    return Utils.normalizedVariance(values);
  }

  private static void saveImage(String fileName, boolean margin, int scale, double[][]... data) {
    BufferedImage bi = new BufferedImage(data[0].length * scale, data[0][0].length * scale, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < data[0].length; y++) {
      for (int x = 0; x < data[0][y].length; x++) {
        Color color;
        if (data.length == 1) {
          color = new Color((float) data[0][y][x], (float) data[0][y][x], (float) data[0][y][x], 1);
        } else {
          color = new Color(
                  (float) data[0][y][x],
                  (float) data[1][y][x],
                  data.length >= 3 ? (float) data[2][y][x] : 0,
                  data.length >= 4 ? (float) data[3][y][x] : 1
          );
        }
        if (scale == 1) {
          bi.setRGB(y, x, color.getRGB());
        } else {
          for (int ix = x * scale + (margin ? 1 : 0); ix < (x + 1) * scale - (margin ? 1 : 0); ix++) {
            for (int iy = y * scale + (margin ? 1 : 0); iy < (y + 1) * scale - (margin ? 1 : 0); iy++) {
              bi.setRGB(iy, ix, color.getRGB());
            }
          }
        }
      }
    }
    try {
      ImageIO.write(bi, "PNG", new File(fileName));
    } catch (IOException ex) {
      System.err.printf("Cannot save file \"%s\": %s", fileName, ex.getMessage());
    }
  }

}
