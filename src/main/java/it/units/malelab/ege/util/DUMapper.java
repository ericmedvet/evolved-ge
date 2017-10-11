/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util;

import com.google.common.collect.Multiset;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 *
 * @author eric
 */
public class DUMapper {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    // TODO code application logic here
  }
  
  private static double[][][] getGomeaData(String baseDir, String fileNamePattern, int generations, int genotypeSize) throws IOException {
    double[][] diversities = new double[generations][];
    double[][] usages = new double[generations][];
    for (int g = 0; g<generations; g++) {
      usages[g] = new double[genotypeSize];
      diversities[g] = new double[genotypeSize];
      BufferedReader reader = Files.newBufferedReader(FileSystems.getDefault().getPath("baseDir", String.format(fileNamePattern, g)));
      String line;      
      int populationSize = 0;
      while ((line=reader.readLine())!=null) {
        populationSize = populationSize+1;
        String[] pieces = line.split(" ");
        String genotype = pieces[0];
        for (int i = 2; i<pieces.length; i++) {
          int intronIndex = Integer.parseInt(pieces[i]);
          usages[g][intronIndex] = usages[g][intronIndex]+1;
        }
        
      }
      for (int i = 0; i<genotypeSize; i++) {
        usages[g][i] = (populationSize-usages[g][i])/populationSize;
      }
    }
    return null;
  }
  
  private double multisetDiversity(Multiset m, Set d) {
    double[] counts = new double[d.size()];
    int i = 0;
    for (Object possibleValue : d) {
      counts[i] = m.count(possibleValue);
      i = i + 1;
    }
    double sumOfSquares = 0;
    double sum = m.size();
    double mean = m.size() / counts.length;
    for (double count : counts) {
      sumOfSquares = sumOfSquares + Math.pow(count - mean, 2);
    }
    double normalizedVar = sumOfSquares / (sum * sum * (1 - 1 / (double) counts.length));
    return Math.max(Math.min(1 - normalizedVar, 1d), 0d);
  }
  
  
  
  
  private static void saveImage(String fileName, double[][]... data) {
    BufferedImage bi = new BufferedImage(data[0][0].length, data[0].length, BufferedImage.TYPE_INT_ARGB);
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
        bi.setRGB(x, y, color.getRGB());
      }
    }
    try {
      ImageIO.write(bi, "PNG", new File(fileName));
    } catch (IOException ex) {
      System.err.printf("Cannot save file \"%s\": %s", fileName, ex.getMessage());
    }
  }
  
}
