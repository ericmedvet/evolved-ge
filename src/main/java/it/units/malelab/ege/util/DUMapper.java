/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
  public static void main(String[] args) throws IOException {
    double[][][] gomeaData = getGomeaData("/home/eric/experiments/dumapper/gomea-1", "eric_population_%d.dat", 50, 127);
    saveImages("/home/eric/experiments/dumapper/gomea-1.%s.png", false, 4, gomeaData);
    double[][][] gsgpData = getGsgpData("/home/eric/experiments/dumapper/gsgp-1", "blocks.txt", 11, 20, 20);
    saveImages("/home/eric/experiments/dumapper/gsgp-1.%s.png", false, 10, gsgpData);
  }
  
  private static void saveImages(String fileName, boolean margin, int scale, double[][][] data) {
    saveImage(String.format(fileName, "d"), margin, scale, data[0]);
    saveImage(String.format(fileName, "u"), margin, scale, data[1]);
    saveImage(String.format(fileName, "du"), margin, scale, data[0], data[1]);
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
        diversities[g][i] = multisetDiversity(symbols[g][i], domains[i]);
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
          int gene = Integer.parseInt(pieces[i]);
          genes[i] = gene;
          maxGene = Math.max(maxGene, gene);
          popGenes[i][p] = gene;
        }
        for (int i = 0; i < genotypeSize; i++) {
          usages[g][i] = usages[g][i]+genes[i]/maxGene;
        }
      }
      for (int i = 0; i < genotypeSize; i++) {
        usages[g][i] = usages[g][i]/populationSize;
        diversities[g][i] = normalizedVar(popGenes[i]);
      }
    }
    reader.close();
    return new double[][][]{diversities, usages};
  }

  private static double multisetDiversity(Multiset m, Set d) {
    double[] counts = new double[d.size()];
    int i = 0;
    for (Object possibleValue : d) {
      counts[i] = m.count(possibleValue);
      i = i + 1;
    }
    return normalizedVar(counts);
  }
  
  private static double normalizedVar(double[] counts) {
    double sum = 0;
    for (double count : counts) {
      sum = sum + count;
    }
    double sumOfSquares = 0;    
    double mean = sum / counts.length;
    for (double count : counts) {
      sumOfSquares = sumOfSquares + Math.pow(count - mean, 2);
    }
    double normalizedVar = sumOfSquares / (sum * sum * (1 - 1 / (double) counts.length));
    return Math.max(Math.min(1 - normalizedVar, 1d), 0d);
    
  }

  private static void saveImage(String fileName, boolean margin, int scale, double[][]... data) {
    BufferedImage bi = new BufferedImage(data[0].length*scale, data[0][0].length*scale, BufferedImage.TYPE_INT_ARGB);
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
        if (scale==1) {
          bi.setRGB(y, x, color.getRGB());
        } else {
          for (int ix = x*scale+(margin?1:0); ix<(x+1)*scale-(margin?1:0); ix++) {
            for (int iy = y*scale+(margin?1:0); iy<(y+1)*scale-(margin?1:0); iy++) {
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
