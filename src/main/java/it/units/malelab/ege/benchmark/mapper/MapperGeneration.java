/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.mapper;

import it.units.malelab.ege.core.LeavesJoiner;
import it.units.malelab.ege.core.Problem;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.util.Utils;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author eric
 */
public class MapperGeneration extends Problem<String, MultiObjectiveFitness> {
  
  public MapperGeneration(int genotypeSize, int n, Random random, Problem<String, NumericFitness>... problems) throws IOException {
    super(Utils.parseFromFile(new File("grammars/mapper.bnf")),
            new MappingPropertiesFitness(genotypeSize, n, random, problems),
            null,
            new LeavesJoiner<String>()
    );
    
  }

}
