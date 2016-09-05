/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.mapper.BreathFirstMapper;
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
    Grammar grammar = Utils.parseFromFile(new File("/home/eric/Scrivania/max_grammar.bnf"));
    System.out.println(grammar);
    StandardGEMapper geMapper = new StandardGEMapper(4, 10, grammar);
    BreathFirstMapper bfMapper = new BreathFirstMapper(4, 10, grammar);
    Random random = new Random(1);
    for (int i = 0; i < 5; i++) {
      Genotype g = Utils.randomGenotype(128, random);
      System.out.println(Utils.bitSetToString(g, 128));
      try {
        System.out.println(geMapper.map(g));
      } catch (MappingException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "GE ex!", ex);
      }
      try {
        System.out.println(bfMapper.map(g));
      } catch (MappingException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "BF ex!", ex);
      }
    }
  }

}
