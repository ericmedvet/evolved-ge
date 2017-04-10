/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.ge.listener;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.listener.AbstractListener;
import it.units.malelab.ege.core.listener.WithConstants;
import it.units.malelab.ege.core.listener.event.EvolutionEndEvent;
import it.units.malelab.ege.core.listener.event.EvolutionEvent;
import it.units.malelab.ege.core.listener.event.GenerationEvent;
import it.units.malelab.ege.ge.GEIndividual;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.ge.mapper.StandardGEMapper;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 *
 * @author eric
 */
public class EvolutionImageSaverListener<T, F extends Fitness> extends AbstractListener<T, F> implements WithConstants {
  
  public static enum ImageType {
    DIVERSITY,
    USAGE,
    DU,
    BEST_BITS,
    BEST_USAGE
  };

  private final Map<String, Object> constants;
  private final String basePath;
  private final EnumSet<ImageType> types;
  
  private final List<double[]> evolutionDiversities;
  private final List<double[]> evolutionUsages;
  private final List<double[]> evolutionBestUsages;
  private final List<double[]> evolutionBestBits;

  public EvolutionImageSaverListener(
          Map<String, Object> constants,
          String basePath, ImageType... types) {
    super(GenerationEvent.class, EvolutionEndEvent.class);
    this.constants = new LinkedHashMap<>(constants);
    this.basePath = basePath;
    evolutionDiversities = new ArrayList<>();
    evolutionUsages = new ArrayList<>();
    evolutionBestUsages = new ArrayList<>();
    evolutionBestBits = new ArrayList<>();
    this.types = EnumSet.copyOf(Arrays.asList(types));
  }

  @Override
  public void listen(EvolutionEvent<T, F> event) {
    if (types.isEmpty()) {
      return;
    }
    List<GEIndividual<BitsGenotype, T, F>> population = new ArrayList<>();
    List<List<Individual<T, F>>> rankedPopulation = ((GenerationEvent)event).getRankedPopulation();
    for (List<Individual<T, F>> rank : rankedPopulation) {
      for (Individual<T, F> individual : rank) {
      if (individual instanceof GEIndividual) {
        if (((GEIndividual)individual).getGenotype() instanceof BitsGenotype) {
          population.add((GEIndividual<BitsGenotype, T, F>)individual);
        }
      }
      }
    }
    //update best bits
    GEIndividual<BitsGenotype, T, F> best = (GEIndividual<BitsGenotype, T, F>)rankedPopulation.get(0).get(0);
    if (types.contains(ImageType.BEST_BITS)) {
      double[] bestBits = new double[best.getGenotype().size()];
      for (int i = 0; i < bestBits.length; i++) {
        bestBits[i] = best.getGenotype().get(i) ? 1 : 0;
      }
      evolutionBestBits.add(bestBits);
    }
    //update diversities
    if (types.contains(ImageType.DIVERSITY)||types.contains(ImageType.DU)) {
      double[] diversities = new double[best.getGenotype().size()];
      double[] counts = new double[best.getGenotype().size()];
      for (GEIndividual<BitsGenotype, T, F> individual : population) {
        for (int i = 0; i < Math.min(best.getGenotype().size(), individual.getGenotype().size()); i++) {
          counts[i] = counts[i] + 1;
          diversities[i] = diversities[i] + (individual.getGenotype().get(i) ? 1 : 0);
        }
      }
      for (int i = 0; i < diversities.length; i++) {
        diversities[i] = 1 - Math.abs(diversities[i] / counts[i] - 0.5) * 2;
      }
      evolutionDiversities.add(diversities);
    }
    //update usages
    if (types.contains(ImageType.USAGE)||types.contains(ImageType.DU)) {
      double[] usages = new double[best.getGenotype().size()];
      double count = 0;
      for (GEIndividual<BitsGenotype, T, F> individual : population) {
        int[] bitUsages = (int[]) individual.getOtherInfo().get(StandardGEMapper.BIT_USAGES_INDEX_NAME);
        if (bitUsages != null) {
          double maxUsage = 0;
          for (int bitUsage : bitUsages) {
            maxUsage = Math.max(maxUsage, (double) bitUsage);
          }
          for (int i = 0; i < Math.min(bitUsages.length, usages.length); i++) {
            usages[i] = usages[i] + (double) bitUsages[i] / maxUsage;
          }
          count = count + 1;
        }
      }
      if (count > 0) {
        for (int i = 0; i < usages.length; i++) {
          usages[i] = usages[i] / count;
        }
      }
      evolutionUsages.add(usages);
    }
    //update best usages
    if (types.contains(ImageType.BEST_BITS)) {
      double[] bestUsages = new double[best.getGenotype().size()];
      int[] bitUsages = (int[]) best.getOtherInfo().get(StandardGEMapper.BIT_USAGES_INDEX_NAME);
      if (bitUsages != null) {
        double maxUsage = 0;
        for (int bitUsage : bitUsages) {
          maxUsage = Math.max(maxUsage, (double) bitUsage);
        }
        for (int i = 0; i < Math.min(bitUsages.length, bestUsages.length); i++) {
          bestUsages[i] = (double) bitUsages[i] / maxUsage;
        }
      }
      evolutionBestUsages.add(bestUsages);
    }
    if (event instanceof EvolutionEndEvent) {
      //save and clear
      if (!evolutionBestBits.isEmpty()) {
        //save
        String baseFileName = "";
        for (Object value : constants.values()) {
          baseFileName = baseFileName + value.toString() + "-";
        }
        if (types.contains(ImageType.DIVERSITY)) {
          saveCSV(basePath + File.separator + baseFileName + "diversitiy.csv", toArray(evolutionDiversities));
          saveImage(basePath + File.separator + baseFileName + "diversity.png", toArray(evolutionDiversities));
        }
        if (types.contains(ImageType.USAGE)) {
          saveImage(basePath + File.separator + baseFileName + "usage.png", toArray(evolutionUsages));
          saveCSV(basePath + File.separator + baseFileName + "usage.csv", toArray(evolutionUsages));
        }
        if (types.contains(ImageType.DU)) {
          saveImage(basePath + File.separator + baseFileName + "diversity_usage.png", toArray(evolutionDiversities), toArray(evolutionUsages));
        }
        if (types.contains(ImageType.BEST_BITS)) {
          saveCSV(basePath + File.separator + baseFileName + "bestBits.csv", toArray(evolutionBestBits));
          saveCSV(basePath + File.separator + baseFileName + "bestBits.csv", toArray(evolutionBestBits));
        }
        if (types.contains(ImageType.BEST_USAGE)) {
          saveImage(basePath + File.separator + baseFileName + "bestUsage.png", toArray(evolutionBestUsages));
          saveCSV(basePath + File.separator + baseFileName + "bestUsage.csv", toArray(evolutionBestUsages));
        }
      }
      //clear
      evolutionBestBits.clear();
      evolutionBestUsages.clear();
      evolutionUsages.clear();
      evolutionDiversities.clear();
    }
  }

  @Override
  public void updateConstants(Map<String, Object> newConstants) {
    for (String key : constants.keySet()) {
      if (newConstants.containsKey(key)) {
        constants.put(key, newConstants.get(key));
      }
    }
  }

  private double[][] toArray(List<double[]> list) {
    double[][] data = new double[list.size()][];
    for (int i = 0; i < list.size(); i++) {
      data[i] = list.get(i);
    }
    return data;
  }

  private void saveCSV(String fileName, double[][] data) {
    try (PrintStream ps = new PrintStream(fileName)) {
      for (int g = 0; g < data.length; g++) {
        for (int i = 0; i < data[g].length; i++) {
          ps.printf("%6.4f", data[g][i]);
          if (i == data[g].length - 1) {
            ps.println();
          } else {
            ps.print(";");
          }
        }
      }
    } catch (FileNotFoundException ex) {
      System.err.printf("Cannot save file \"%s\": %s", fileName, ex.getMessage());
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

  public static void printLegend(String fileName) throws IOException {
    double n = 200;
    double d = 5;
    BufferedImage bi = new BufferedImage((int) n, (int) n, BufferedImage.TYPE_INT_ARGB);
    for (double x = 0; x < n; x++) {
      for (double y = 0; y < n; y++) {
        bi.setRGB((int) x, (int) y, new Color((float) (Math.floor(x / n*d)/d), (float) (Math.floor(y / n*d)/d), 0, 1).getRGB());
      }
    }
    ImageIO.write(bi, "PNG", new File(fileName));
  }

}
