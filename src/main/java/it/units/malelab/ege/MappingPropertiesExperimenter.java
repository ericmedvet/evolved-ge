/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import it.units.malelab.ege.benchmark.KLandscapes;
import it.units.malelab.ege.benchmark.Text;
import it.units.malelab.ege.benchmark.booleanfunction.MultipleOutputParallelMultiplier;
import it.units.malelab.ege.benchmark.booleanfunction.Parity;
import it.units.malelab.ege.benchmark.symbolicregression.HarmonicCurve;
import it.units.malelab.ege.benchmark.symbolicregression.Nguyen7;
import it.units.malelab.ege.benchmark.symbolicregression.Pagie1;
import it.units.malelab.ege.benchmark.symbolicregression.Vladislavleva4;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.Sequence;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.core.mapper.Mapper;
import it.units.malelab.ege.core.mapper.MappingException;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.ge.genotype.SGEGenotype;
import it.units.malelab.ege.ge.genotype.SGEGenotypeFactory;
import it.units.malelab.ege.ge.mapper.HierarchicalMapper;
import it.units.malelab.ege.ge.mapper.PiGEMapper;
import it.units.malelab.ege.ge.mapper.SGEMapper;
import it.units.malelab.ege.ge.mapper.StandardGEMapper;
import it.units.malelab.ege.ge.mapper.WeightedHierarchicalMapper;
import it.units.malelab.ege.util.Pair;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.util.distance.CachedDistance;
import it.units.malelab.ege.util.distance.Distance;
import it.units.malelab.ege.util.distance.Hamming;
import it.units.malelab.ege.util.distance.LeavesEdit;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.apache.commons.math3.stat.StatUtils;

/**
 *
 * @author eric
 */
public class MappingPropertiesExperimenter {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    final int n = 10000;
    final int nDist = 10000;
    //prepare problems and methods
    List<String> problems = Lists.newArrayList(
            "bool-parity5", "bool-mopm3",
            "sr-keijzer6", "sr-nguyen7", "sr-pagie1", "sr-vladislavleva4",
            "other-klandscapes3", "other-klandscapes7",
            "other-text"
    );
    List<String> mappers = new ArrayList<>();
    for (int gs : new int[]{64, 128, 256, 512, 1024}) {
      mappers.add("ge-" + gs + "-2");
      mappers.add("ge-" + gs + "-4");
      mappers.add("ge-" + gs + "-8");
      mappers.add("ge-" + gs + "-12");
      mappers.add("pige-" + gs + "-4");
      mappers.add("pige-" + gs + "-8");
      mappers.add("pige-" + gs + "-16");
      mappers.add("pige-" + gs + "-24");
      mappers.add("hge-" + gs + "-0");
      mappers.add("whge-" + gs + "-2");
      mappers.add("whge-" + gs + "-3");
      mappers.add("whge-" + gs + "-5");
    }
    mappers.add("sge-0-5");
    mappers.add("sge-0-6");
    mappers.add("sge-0-7");
    mappers.add("sge-0-8");
    mappers.clear();
    mappers.addAll(Lists.newArrayList("ge-1024-8", "pige-1024-16", "hge-1024-0", "whge-1024-3", "sge-0-6"));
    PrintStream filePrintStream = null;
    if (args.length > 0) {
      filePrintStream = new PrintStream(args[0]);
    } else {
      filePrintStream = System.out;
    }
    filePrintStream.printf("problem;mapper;genotypeSize;param;property;value%n");
    //prepare distances
    Distance<Node<String>> phenotypeDistance = new CachedDistance<>(new LeavesEdit<String>());
    Distance<Sequence> genotypeDistance = new CachedDistance<>(new Hamming());
    //iterate
    for (String problemName : problems) {
      for (String mapperName : mappers) {
        System.out.printf("%20.20s, %20.20s", problemName, mapperName);
        //build problem
        Problem<String, NumericFitness> problem = null;
        if (problemName.equals("bool-parity5")) {
          problem = new Parity(5);
        } else if (problemName.equals("bool-mopm3")) {
          problem = new MultipleOutputParallelMultiplier(3);
        } else if (problemName.equals("sr-keijzer6")) {
          problem = new HarmonicCurve();
        } else if (problemName.equals("sr-nguyen7")) {
          problem = new Nguyen7(1);
        } else if (problemName.equals("sr-pagie1")) {
          problem = new Pagie1();
        } else if (problemName.equals("sr-vladislavleva4")) {
          problem = new Vladislavleva4(1);
        } else if (problemName.equals("other-klandscapes3")) {
          problem = new KLandscapes(3);
        } else if (problemName.equals("other-klandscapes7")) {
          problem = new KLandscapes(7);
        } else if (problemName.equals("other-text")) {
          problem = new Text();
        }
        //build configuration and evolver
        Mapper mapper = null;
        int genotypeSize = Integer.parseInt(mapperName.split("-")[1]);
        int mapperMainParam = Integer.parseInt(mapperName.split("-")[2]);
        if (mapperName.split("-")[0].equals("ge")) {
          mapper = new StandardGEMapper<>(mapperMainParam, 1, problem.getGrammar());
        } else if (mapperName.split("-")[0].equals("pige")) {
          mapper = new PiGEMapper<>(mapperMainParam, 1, problem.getGrammar());
        } else if (mapperName.split("-")[0].equals("sge")) {
          mapper = new SGEMapper<>(mapperMainParam, problem.getGrammar());
        } else if (mapperName.split("-")[0].equals("hge")) {
          mapper = new HierarchicalMapper<>(problem.getGrammar());
        } else if (mapperName.split("-")[0].equals("whge")) {
          mapper = new WeightedHierarchicalMapper<>(mapperMainParam, false, true, problem.getGrammar());
        }
        //prepare things
        Random random = new Random(1);
        Set<Sequence> genotypes = new LinkedHashSet<>(n);
        //build genotypes
        if (mapperName.split("-")[0].equals("sge")) {
          SGEGenotypeFactory<String> factory = new SGEGenotypeFactory<>((SGEMapper) mapper);
          while (genotypes.size() < n) {
            genotypes.add(factory.build(random));
          }
          genotypeSize = factory.getBitSize();
        } else {
          BitsGenotypeFactory factory = new BitsGenotypeFactory(genotypeSize);
          while (genotypes.size() < n) {
            genotypes.add(factory.build(random));
          }
        }
        //build and fill map
        Multimap<Node<String>, Sequence> multimap = HashMultimap.create();
        int progress = 0;
        for (Sequence genotype : genotypes) {
          Node<String> phenotype;
          try {
            if (mapperName.split("-")[0].equals("sge")) {
              phenotype = mapper.map((SGEGenotype<String>) genotype, new HashMap<>());
            } else {
              phenotype = mapper.map((BitsGenotype) genotype, new HashMap<>());
            }
          } catch (MappingException e) {
            phenotype = Node.EMPTY_TREE;
          }
          multimap.put(phenotype, genotype);
          progress = progress+1;
          if (progress % Math.round(n/10) == 0) {
            System.out.print(".");
          }          
        }
        System.out.println();
        //compute distances
        List<Pair<Double, Double>> allDistances = new ArrayList<>();
        List<Pair<Double, Double>> allValidDistances = new ArrayList<>();
        Multimap<Node<String>, Double> genotypeDistances = ArrayListMultimap.create();
        for (Node<String> phenotype : multimap.keySet()) {
          for (Sequence genotype1 : multimap.get(phenotype)) {
            for (Sequence genotype2 : multimap.get(phenotype)) {
              double gDistance = genotypeDistance.d(genotype1, genotype2);
              genotypeDistances.put(phenotype, gDistance);
              if (genotypeDistances.get(phenotype).size() > nDist) {
                break;
              }
            }
            if (genotypeDistances.get(phenotype).size() > nDist) {
              break;
            }
          }
        }
        List<Map.Entry<Node<String>, Sequence>> entries = new ArrayList<>(multimap.entries());
        Collections.shuffle(entries, random);
        for (Map.Entry<Node<String>, Sequence> entry1 : entries) {
          for (Map.Entry<Node<String>, Sequence> entry2 : entries) {
            double gDistance = genotypeDistance.d(entry1.getValue(), entry2.getValue());
            double pDistance = phenotypeDistance.d(entry1.getKey(), entry2.getKey());
            allDistances.add(new Pair<>(gDistance, pDistance));
            if (!Node.EMPTY_TREE.equals(entry1.getKey()) && !Node.EMPTY_TREE.equals(entry2.getKey())) {
              allValidDistances.add(new Pair<>(gDistance, pDistance));
            }
            if (allDistances.size() > nDist) {
              break;
            }
          }
          if (allDistances.size() > nDist) {
            break;
          }
        }
        //compute properties
        double invalidity = (double) multimap.get(Node.EMPTY_TREE).size() / (double) genotypes.size();
        double redundancy = 1 - (double) multimap.keySet().size() / (double) genotypes.size();
        double validRedundancy = redundancy;
        if (multimap.keySet().contains(Node.EMPTY_TREE)) {
          validRedundancy = 1 - ((double) multimap.keySet().size() - 1d) / (double) (genotypes.size() - multimap.get(Node.EMPTY_TREE).size());
        }
        double locality = Utils.pearsonCorrelation(allDistances);
        double validLocality = Utils.pearsonCorrelation(allValidDistances);
        double[] sizes = new double[multimap.keySet().size()];
        double[] meanGenotypeDistances = new double[multimap.keySet().size()];
        int invalidIndex = -1;
        int c = 0;
        for (Node<String> phenotype : multimap.keySet()) {
          if (Node.EMPTY_TREE.equals(phenotype)) {
            invalidIndex = c;
          }
          sizes[c] = multimap.get(phenotype).size();
          double[] distances = new double[genotypeDistances.get(phenotype).size()];
          int k = 0;
          for (Double distance : genotypeDistances.get(phenotype)) {
            distances[k] = distance;
            k = k + 1;
          }
          meanGenotypeDistances[c] = StatUtils.mean(distances);
          c = c + 1;
        }
        double nonUniformity = Math.sqrt(StatUtils.variance(sizes)) / StatUtils.mean(sizes);
        double nonSynonymousity = StatUtils.mean(meanGenotypeDistances) / StatUtils.mean(firsts(allDistances));
        double validNonUniformity = nonUniformity;
        double validNonSynonymousity = nonSynonymousity;
        if (invalidIndex != -1) {
          double[] validSizes = new double[multimap.keySet().size() - 1];
          double[] validMeanGenotypeDistances = new double[multimap.keySet().size() - 1];
          if (invalidIndex > 0) {
            System.arraycopy(sizes, 0, validSizes, 0, invalidIndex);
            System.arraycopy(meanGenotypeDistances, 0, validMeanGenotypeDistances, 0, invalidIndex);
          }
          System.arraycopy(sizes, invalidIndex + 1, validSizes, invalidIndex, sizes.length - invalidIndex - 1);
          System.arraycopy(meanGenotypeDistances, invalidIndex + 1, validMeanGenotypeDistances, invalidIndex, meanGenotypeDistances.length - invalidIndex - 1);
          validNonUniformity = Math.sqrt(StatUtils.variance(validSizes)) / StatUtils.mean(validSizes);
          validNonSynonymousity = StatUtils.mean(validMeanGenotypeDistances) / StatUtils.mean(firsts(allValidDistances));
        }
        //compute locality
        filePrintStream.printf("%s;%s;%d;%d;invalidity;%f %n", problemName, mapperName.split("-")[0], genotypeSize, mapperMainParam, invalidity);
        filePrintStream.printf("%s;%s;%d;%d;redundancy;%f %n", problemName, mapperName.split("-")[0], genotypeSize, mapperMainParam, redundancy);
        filePrintStream.printf("%s;%s;%d;%d;validRedundancy;%f %n", problemName, mapperName.split("-")[0], genotypeSize, mapperMainParam, validRedundancy);
        filePrintStream.printf("%s;%s;%d;%d;locality;%f %n", problemName, mapperName.split("-")[0], genotypeSize, mapperMainParam, locality);
        filePrintStream.printf("%s;%s;%d;%d;validLLocality;%f %n", problemName, mapperName.split("-")[0], genotypeSize, mapperMainParam, validLocality);
        filePrintStream.printf("%s;%s;%d;%d;nonUniformity;%f %n", problemName, mapperName.split("-")[0], genotypeSize, mapperMainParam, nonUniformity);
        filePrintStream.printf("%s;%s;%d;%d;validNonUniformity;%f %n", problemName, mapperName.split("-")[0], genotypeSize, mapperMainParam, validNonUniformity);
        filePrintStream.printf("%s;%s;%d;%d;nonSynonymousity;%f %n", problemName, mapperName.split("-")[0], genotypeSize, mapperMainParam, nonSynonymousity);
        filePrintStream.printf("%s;%s;%d;%d;validNonSynonymousity;%f %n", problemName, mapperName.split("-")[0], genotypeSize, mapperMainParam, validNonSynonymousity);
      }
    }
    if (filePrintStream != null) {
      filePrintStream.close();
    }
  }

  private static double[] firsts(List<Pair<Double, Double>> pairs) {
    double[] values = new double[pairs.size()];
    for (int i = 0; i < values.length; i++) {
      values[i] = pairs.get(i).getFirst();
    }
    return values;
  }

}
