/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.util.Utils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class MapperGenerationValidatorDistributedExperimenter extends MapperGenerationDistributedExperimenter {

  private final static Logger L = Logger.getLogger(MapperGenerationValidatorDistributedExperimenter.class.getName());

  private final String mappersFileName;

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

    args = new String[]{"hi", "9001", "/home/eric/experiments/ge/dist", "map.gen.validation", "/home/eric/experiments/ge/dist/mappers.big.txt"};
    String keyPhrase = args[0];
    int port = Integer.parseInt(args[1]);
    String baseResultDirName = args[2];
    String baseResultFileName = args[3];
    String mappersFileName = args[4];
    MapperGenerationValidatorDistributedExperimenter experimenter = new MapperGenerationValidatorDistributedExperimenter(mappersFileName, keyPhrase, port, baseResultDirName, baseResultFileName);
    experimenter.start();
  }

  public MapperGenerationValidatorDistributedExperimenter(String mappersFileName, String keyPhrase, int port, String baseResultDirName, String baseResultFileName) throws IOException {
    super(keyPhrase, port, baseResultDirName, baseResultFileName);
    this.mappersFileName = mappersFileName;
  }

  @Override
  public void start() throws IOException {
    
    for (String baselineName : baselines.keySet()) {
      MultiObjectiveFitness<Double> mof = baseMOF.compute(baselines.get(baselineName));
      System.out.printf("%s: redundancy=%5.3f non-locality=%5.3f non-uniformity=%5.3f%n",
              baselineName, mof.getValue()[0], mof.getValue()[1], mof.getValue()[2]
      );
    }
    
    System.exit(0);
    
    //baseline jobs
    for (Map.Entry<String, Node<String>> baselineEntry : baselines.entrySet()) {
      String mapperName = baselineEntry.getKey();
      Node<String> mapper = baselineEntry.getValue();
      submitValidationJobs(mapper, mapperName, 0, 0);
    }
    //read mappers file
    BufferedReader br = new BufferedReader(new FileReader(mappersFileName));
    List<String> headers = Arrays.asList(br.readLine().split(";"));
    List<String> summaryHeaders = Arrays.asList("mapper.name", "outer.run", "i", "redundancy", "non.locality", "non.uniformity");
    String previousMapperName = null;
    String previousOuterRun = null;
    Map<String, MultiObjectiveFitness<Double>> currentMap = new LinkedHashMap<>();
    int count = 0;
    while (true) {
      String line = br.readLine();
      Map<String, String> data = new HashMap<>();
      if (line != null) {
        count++;
        String[] values = line.split(";");
        for (int i = 0; i < headers.size(); i++) {
          data.put(headers.get(i), values[i]);
        }
      }
      if (((line == null) || !data.get("mapper.name").equals(previousMapperName) || !data.get("outer.run").equals(previousOuterRun)) && !currentMap.isEmpty()) {
        //select subset and run
        Set<String> selectedSerializedMappers = Utils.maximallySparseSubset(currentMap, nOfFirstRank);
        L.info(String.format("%s/%s: read %d, collected %d, selected %d.",
                previousMapperName, previousOuterRun,
                count, currentMap.size(), selectedSerializedMappers.size()));
        int innerCount = 0;
        PrintStream mappersPropsPs = master.getPrintStreamFactory().get(summaryHeaders, baseResultFileName + ".mappers.properties");
        for (String selectedSerializedMapper : selectedSerializedMappers) {
          Node<String> mapper = deserializeBase64(selectedSerializedMapper);
          submitValidationJobs(mapper, previousMapperName, Integer.parseInt(previousOuterRun), innerCount);
          //print summary
          mappersPropsPs.print(data.get("mapper.name")+";");
          mappersPropsPs.print(data.get("outer.run")+";");
          mappersPropsPs.print(innerCount+";");
          mappersPropsPs.print(currentMap.get(selectedSerializedMapper).getValue()[0]+";");
          mappersPropsPs.print(currentMap.get(selectedSerializedMapper).getValue()[1]+";");
          mappersPropsPs.print(currentMap.get(selectedSerializedMapper).getValue()[2]);
          mappersPropsPs.println();
          innerCount++;
        }
        //reset
        currentMap.clear();
        count = 0;
        if (line == null) {
          break;
        }
      }
      currentMap.put(data.get("mapper.serialized.base64"), new MultiObjectiveFitness<Double>(
              Double.parseDouble(data.get("redundancy")),
              Double.parseDouble(data.get("non.locality")),
              Double.parseDouble(data.get("non.uniformity"))
      ));
      previousMapperName = data.get("mapper.name");
      previousOuterRun = data.get("outer.run");
    }
    br.close();
    //baseline jobs
    for (Map.Entry<String, Node<String>> baselineEntry : baselines.entrySet()) {
      String mapperName = baselineEntry.getKey();
      Node<String> mapper = baselineEntry.getValue();
      if (validateMappers) {
        submitValidationJobs(mapper, mapperName, 0, 0);
      }
    }
  }

  private Node<String> deserializeBase64(String s) {
    ObjectInputStream ois = null;
    Node<String> tree = null;
    try {
      byte[] buffer = Base64.getDecoder().decode(s);
      ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
      ois = new ObjectInputStream(bais);
      tree = (Node<String>) ois.readObject();
    } catch (IOException ex) {
      //ignore
    } catch (ClassNotFoundException ex) {
      //ignore
    } finally {
      if (ois != null) {
        try {
          ois.close();
        } catch (IOException ex) {
          //ignore
        }
      }
    }
    return tree;
  }

}
