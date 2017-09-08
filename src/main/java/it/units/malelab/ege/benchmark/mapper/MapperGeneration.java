/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.mapper;

import it.units.malelab.ege.core.LeavesJoiner;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.PhenotypePrinter;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.util.Utils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class MapperGeneration extends Problem<String, MultiObjectiveFitness> {

  public MapperGeneration(int genotypeSize, int n, int maxMappingDepth, Random random, List<Problem<String, NumericFitness>> problems) throws IOException {
    super(Utils.parseFromFile(new File("grammars/mapper.bnf")),
            new MappingPropertiesFitness(genotypeSize, n, maxMappingDepth, random, problems),
            null,
            //new LeavesJoiner<String>()
            new PhenotypePrinter<String>() {
              private PhenotypePrinter<String> innerPhenotypePrinter = new LeavesJoiner<>();
              @Override
              public String toString(Node<String> node) {
                return innerPhenotypePrinter.toString(node.getChildren().get(0))+";"+innerPhenotypePrinter.toString(node.getChildren().get(1));
              }
            }
    );

  }

}
