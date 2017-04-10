/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.ranker;

import it.units.malelab.ege.core.Individual;
import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
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
public class ParetoRankerTest {
  
  public ParetoRankerTest() {
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
   * Test of rank method, of class ParetoRanker.
   */
  @Test
  public void testRank() {
    ParetoRanker instance = new ParetoRanker();
    Individual<?, MultiObjectiveFitness> i0 = new Individual<>(null, new MultiObjectiveFitness(2, 1), 0, null, null);
    Individual<?, MultiObjectiveFitness> i1 = new Individual<>(null, new MultiObjectiveFitness(1, 2), 0, null, null);
    Individual<?, MultiObjectiveFitness> i2 = new Individual<>(null, new MultiObjectiveFitness(1, 3), 0, null, null);
    Individual<?, MultiObjectiveFitness> i3 = new Individual<>(null, new MultiObjectiveFitness(3, 1), 0, null, null);
    Individual<?, MultiObjectiveFitness> i4 = new Individual<>(null, new MultiObjectiveFitness(4, 4), 0, null, null);
    Individual<?, MultiObjectiveFitness> i5 = new Individual<>(null, new MultiObjectiveFitness(5, 5), 0, null, null);
    List<Individual<?, MultiObjectiveFitness>> pop = Arrays.asList(i0, i1, i2, i3, i4, i5);
    for (int i = 0; i<5; i++) {
      List<List<Individual<?, MultiObjectiveFitness>>> ranked = instance.rank(pop);
      assertEquals("i0 rank should be in list 0", true, ranked.get(0).contains(i0));
      assertEquals("i1 rank should be in list 0", true, ranked.get(0).contains(i1));
      assertEquals("i2 rank should be in list 1", true, ranked.get(1).contains(i2));
      assertEquals("i3 rank should be in list 1", true, ranked.get(1).contains(i3));
      assertEquals("i4 rank should be in list 2", true, ranked.get(2).contains(i4));
      assertEquals("i5 rank should be in list 3", true, ranked.get(3).contains(i5));
      Collections.shuffle(pop, new Random(1l));
    }
  }

  /**
   * Test of compare method, of class ParetoRanker.
   */
  @Test
  public void testCompare() {
    MultiObjectiveFitness f0 = new MultiObjectiveFitness(2, 1);
    MultiObjectiveFitness f1 = new MultiObjectiveFitness(1, 2);
    MultiObjectiveFitness f2 = new MultiObjectiveFitness(2, 2);
    ParetoRanker instance = new ParetoRanker();
    assertEquals("f0 vs f1 should be 0", 0, instance.compare(f0, f1));
    assertEquals("f1 vs f0 should be 0", 0, instance.compare(f1, f0));
    assertEquals("f1 vs f2 should be -1", -1, instance.compare(f1, f2));
    assertEquals("f2 vs f0 should be 1", 1, instance.compare(f2, f0));
  }
  
}
