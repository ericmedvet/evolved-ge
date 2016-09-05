/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.mapper.BreathFirstMapper;
import it.units.malelab.ege.mapper.FractalMapper;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.StandardGEMapper;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eric
 */
public class Main {

  public static void main(String[] args) throws IOException {
    Grammar grammar = Utils.parseFromFile(new File("grammars/max-grammar-easy.bnf"));
    System.out.println(grammar);
    StandardGEMapper geMapper = new StandardGEMapper(4, 10, grammar);
    BreathFirstMapper bfMapper = new BreathFirstMapper(4, 10, grammar);
    FractalMapper fMapper = new FractalMapper(4, grammar);
    Random random = new Random(1);
    for (int i = 0; i < 5; i++) {
      Genotype g = Utils.randomGenotype(128, random);
      System.out.println(g.toString());
      try {
        System.out.printf("GE: %s\n", geMapper.map(g));
      } catch (MappingException ex) {
        System.err.printf("GE exception: %s\n", ex);
      }
      try {
        System.out.printf("BF: %s\n", bfMapper.map(g));
      } catch (MappingException ex) {
        System.err.printf("BF exception: %s\n", ex);
      }
      try {
        System.out.printf("F: %s\n", fMapper.map(g));
      } catch (MappingException ex) {
        System.err.printf("F exception: %s\n", ex);
      }
    }
  }
  
  

}
