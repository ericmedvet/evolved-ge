/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.ranker;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
import it.units.malelab.ege.core.fitness.NumericFitness;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eric
 */
public class ComparableFitnessRankerTest {
  
  public ComparableFitnessRankerTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of rank method, of class ComparableFitnessRanker.
   */
  @Test
  public void testRank() {
    ComparableFitnessRanker instance = new ComparableFitnessRanker();
    Individual<?, NumericFitness> i0 = new Individual<>(null, new NumericFitness(0d), 0, null, null);
    Individual<?, NumericFitness> i1 = new Individual<>(null, new NumericFitness(1d), 0, null, null);
    Individual<?, NumericFitness> i2 = new Individual<>(null, new NumericFitness(2d), 0, null, null);
    Individual<?, NumericFitness> i3 = new Individual<>(null, new NumericFitness(2d), 0, null, null);
    List<Individual<?, NumericFitness>> pop = Arrays.asList(i0, i1, i2, i3);
    for (int i = 0; i<5; i++) {
      instance.rank(pop);
      assertEquals("i0 rank should be 0", 0, i0.getRank());
      assertEquals("i1 rank should be 1", 1, i1.getRank());
      assertEquals("i2 rank should be 2", 2, i2.getRank());
      assertEquals("i3 rank should be 2", 2, i3.getRank());
      Collections.shuffle(pop, new Random(1l));
    }
  }
  
}
