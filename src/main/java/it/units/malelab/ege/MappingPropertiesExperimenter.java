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
import java.util.Arrays;
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
    final int genotypeSize = 256;
    final int n = 100;
    //prepare problems and methods
    List<String> problems = Lists.newArrayList(
            "bool-parity5", "bool-mopm3",
            "sr-keijzer6", "sr-nguyen7", "sr-pagie1", "sr-vladislavleva4",
            "other-klandscapes3", "other-klandscapes7", "other-text"
    );
    List<String> methods = Lists.newArrayList("ge-8", "pige-16", "sge-6", "hge", "whge-3");
    //methods = Lists.newArrayList("ge-8", "pige-16", "hge", "whge-3");
    PrintStream filePrintStream = null;
    if (args.length > 0) {
      filePrintStream = new PrintStream(args[0]);
    } else {
      filePrintStream = System.out;
    }
    //prepare distances
    Distance<Node<String>> phenotypeDistance = new CachedDistance<>(new LeavesEdit<String>());
    Distance<Sequence> genotypeDistance = new CachedDistance<>(new Hamming());
    //iterate
    for (String problemName : problems) {
      for (String methodName : methods) {
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
        if (methodName.startsWith("ge-")) {
          int codonSize = Integer.parseInt(methodName.replace("ge-", ""));
          mapper = new StandardGEMapper<>(codonSize, 1, problem.getGrammar());
        } else if (methodName.startsWith("pige-")) {
          int codonSize = Integer.parseInt(methodName.replace("pige-", ""));
          mapper = new PiGEMapper<>(codonSize, 1, problem.getGrammar());
        } else if (methodName.startsWith("sge-")) {
          int depth = Integer.parseInt(methodName.replace("sge-", ""));
          mapper = new SGEMapper<>(depth, problem.getGrammar());
        } else if (methodName.equals("hge")) {
          mapper = new HierarchicalMapper<>(problem.getGrammar());
        } else if (methodName.startsWith("whge-")) {
          int depth = Integer.parseInt(methodName.replace("whge-", ""));
          mapper = new WeightedHierarchicalMapper<>(depth, problem.getGrammar());
        }
        //prepare things
        Random random = new Random(1);
        Set<Sequence> genotypes = new LinkedHashSet<>(n);
        //build genotypes
        if (methodName.startsWith("sge-")) {
          SGEGenotypeFactory<String> factory = new SGEGenotypeFactory<>((SGEMapper) mapper);
          while (genotypes.size() < n) {
            genotypes.add(factory.build(random));
          }
        } else {
          BitsGenotypeFactory factory = new BitsGenotypeFactory(genotypeSize);
          while (genotypes.size() < n) {
            genotypes.add(factory.build(random));
          }
        }
        //build and fill map
        Multimap<Node<String>, Sequence> multimap = HashMultimap.create();
        for (Sequence genotype : genotypes) {
          Node<String> phenotype;
          try {
            if (methodName.startsWith("sge-")) {
              phenotype = mapper.map((SGEGenotype<String>) genotype, new HashMap<>());
            } else {
              phenotype = mapper.map((BitsGenotype) genotype, new HashMap<>());
            }
          } catch (MappingException e) {
            phenotype = Node.EMPTY_TREE;
          }
          multimap.put(phenotype, genotype);
        }
        //compute distances
        List<Pair<Double, Double>> allDistances = new ArrayList<>();
        Multimap<Node<String>, Pair<Double, Double>> perPhenotypeDistances = ArrayListMultimap.create();
        for (Map.Entry<Node<String>, Sequence> entry1 : multimap.entries()) {
          for (Map.Entry<Node<String>, Sequence> entry2 : multimap.entries()) {
            double gDistance = genotypeDistance.d(entry1.getValue(), entry2.getValue());
            double pDistance = phenotypeDistance.d(entry1.getKey(), entry2.getKey());
            allDistances.add(new Pair<>(gDistance, pDistance));
            if (entry1.getKey() == entry2.getKey()) {
              perPhenotypeDistances.put(entry1.getKey(), new Pair<>(gDistance, pDistance));
            }
          }
        }
        //compute properties
        double invalidity = (double)multimap.get(Node.EMPTY_TREE).size() / (double) genotypes.size();
        double redundancy = 1 - (double) multimap.keySet().size() / (double) genotypes.size();
        double locality = Utils.pearsonCorrelation(allDistances);
        double[] sizes = new double[multimap.keySet().size()];
        int c = 0;
        for (Node<String> phenotype : multimap.keySet()) {
          sizes[c] = multimap.get(phenotype).size();
          c = c+1;
        }
        System.out.println(Arrays.toString(sizes));
        double nonUniformity = Math.sqrt(StatUtils.variance(sizes));
        //compute locality
        filePrintStream.printf("%s;%s;invalidity;%6.4f %n", problemName, methodName, invalidity);
        filePrintStream.printf("%s;%s;redundancy;%6.4f %n", problemName, methodName, redundancy);
        filePrintStream.printf("%s;%s;locality;%6.4f %n", problemName, methodName, locality);
        filePrintStream.printf("%s;%s;nonUniformity;%6.2f %n", problemName, methodName, nonUniformity);
      }
    }
    if (filePrintStream != null) {
      filePrintStream.close();
    }
  }
  
public double[] firsts(List<Pair<Double, Double>> pairs) {
    double[] values = new double[pairs.size()];
    for (int i = 0; i<values.length; i++) {
      values[i] = pairs.get(i).getFirst();
    }
    return values;
  }
  
}
