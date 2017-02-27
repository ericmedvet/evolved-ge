/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.event.EvolutionEndEvent;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.mapper.StandardGEMapper;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 *
 * @author eric
 */
public class EvolutionImageSaverListener<T> implements EvolutionListener<BitsGenotype, T>, WithConstants {

  private final Set<Class<? extends EvolutionEvent>> eventClasses;
  private final Map<String, Object> constants;
  private final String basePath;
  private final List<Individual<BitsGenotype, T>> bests;
  private final List<double[]> bestBitDivesities;  

  public EvolutionImageSaverListener(
          Map<String, Object> constants,
          String basePath) {
    eventClasses = new LinkedHashSet<>();
    eventClasses.add(GenerationEvent.class);
    eventClasses.add(EvolutionEndEvent.class);
    this.constants = new LinkedHashMap<>(constants);
    this.basePath = basePath;
    bests = new ArrayList<>();
    bestBitDivesities = new ArrayList<>();
  }

  @Override
  public void listen(EvolutionEvent<BitsGenotype, T> event) {
    List<Individual<BitsGenotype, T>> population = new ArrayList<>(((GenerationEvent) event).getPopulation());
    //get best
    Individual<BitsGenotype, T> best = population.get(0);
    for (Individual<BitsGenotype, T> individual : population) {
      if (individual.getFitness().compareTo(best.getFitness()) < 0) {
        best = individual;
      }
    }
    bests.add(best);
    //get diversities
    double[] diversity = new double[best.getGenotype().size()];
    double[] counts = new double[best.getGenotype().size()];
    for (Individual<BitsGenotype, T> individual : population) {
      for (int i = 0; i < Math.min(best.getGenotype().size(), individual.getGenotype().size()); i++) {
        counts[i] = counts[i] + 1;
        diversity[i] = diversity[i] + (individual.getGenotype().get(i) ? 1 : 0);
      }
    }
    for (int i = 0; i < diversity.length; i++) {
      diversity[i] = 1 - Math.abs(diversity[i] / counts[i] - 0.5) * 2;
    }
    bestBitDivesities.add(diversity);
    if (event instanceof EvolutionEndEvent) {
      //save and clear
      if (!bests.isEmpty()) {
        //save
        double[][] bits = new double[bests.size()][];
        double[][] diversities = new double[bests.size()][];
        double[][] usages = new double[bests.size()][];
        for (int i = 0; i < bits.length; i++) {
          double[] bit = new double[bests.get(i).getGenotype().size()];
          for (int j = 0; j < bit.length; j++) {
            bit[j] = bests.get(i).getGenotype().get(j) ? 1 : 0;
          }
          bits[i] = bit;
          diversities[i] = bestBitDivesities.get(i);
          int[] bitUsages = (int[]) bests.get(i).getOtherInfo().get(StandardGEMapper.BIT_USAGES_INDEX_NAME);
          if (bitUsages != null) {
            int max = 0;
            for (int j = 0; j < bitUsages.length; j++) {
              max = Math.max(max, bitUsages[j]);
            }
            usages[i] = new double[bitUsages.length];
            for (int j = 0; j < bitUsages.length; j++) {
              usages[i][j] = (double) bitUsages[j] / (double) max;
            }
          } else {
            usages[i] = new double[bests.get(i).getGenotype().size()];
          }
        }
        String baseFileName = "";
        for (Object value : constants.values()) {
          baseFileName = baseFileName + value.toString() + "-";
        }
        saveImage(basePath + File.separator + baseFileName + "genotype.png", bits);
        saveImage(basePath + File.separator + baseFileName + "diversity.png", diversities);
        saveImage(basePath + File.separator + baseFileName + "usage.png", usages);
        saveImage(basePath + File.separator + baseFileName + "genotype_diversity.png", bits, diversities);
        saveImage(basePath + File.separator + baseFileName + "genotype_usage.png", bits, usages);
        saveImage(basePath + File.separator + baseFileName + "diversity_usage.png", diversities, usages);
        saveImage(basePath + File.separator + baseFileName + "genotype_diversity_usage.png", bits, diversities, usages);
      }
      //clear
      bests.clear();
      bestBitDivesities.clear();
    }
  }

  @Override
  public Set<Class<? extends EvolutionEvent>> getEventClasses() {
    return eventClasses;
  }

  @Override
  public void updateConstants(Map<String, Object> newConstants) {
    for (String key : constants.keySet()) {
      if (newConstants.containsKey(key)) {
        constants.put(key, newConstants.get(key));
      }
    }
  }

  private void saveImage(String fileName, double[][]... data) {
    BufferedImage bi = new BufferedImage(data[0][0].length, data[0].length, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < data[0].length; y++) {
      for (int x = 0; x < data[0][y].length; x++) {
        Color color;
        if (data.length == 1) {
          color = new Color((float) data[0][y][x], (float) data[0][y][x], (float) data[0][y][x], 1);
        } else {
          color = new Color(
                  (float) data[0][y][x],
                  data.length >= 2 ? (float) data[1][y][x] : 1,
                  data.length >= 3 ? (float) data[2][y][x] : 1,
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
