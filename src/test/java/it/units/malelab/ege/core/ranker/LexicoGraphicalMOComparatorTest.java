/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.core.ranker;

import it.units.malelab.ege.core.fitness.MultiObjectiveFitness;
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
public class LexicoGraphicalMOComparatorTest {

  public LexicoGraphicalMOComparatorTest() {
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
   * Test of compare method, of class LexicoGraphicalMOComparator.
   */
  @Test
  public void testCompare() {
    System.out.println("compare");
    MultiObjectiveFitness f1 = new MultiObjectiveFitness(2, 4, 5);
    MultiObjectiveFitness f2 = new MultiObjectiveFitness(2, 3, 6);
    MultiObjectiveFitness f3 = new MultiObjectiveFitness(2, 3, 6);
    LexicoGraphicalMOComparator c = new LexicoGraphicalMOComparator(0, 1, 2);
    assertTrue("f1>f2", c.compare(f1, f2) > 0);
    assertTrue("f2<f1", c.compare(f2, f1) < 0);
    assertTrue("f2<>f3", c.compare(f2, f3) == 0);
    assertTrue("f3<>f2", c.compare(f3, f2) == 0);
    c = new LexicoGraphicalMOComparator(2, 1, 0);
    assertTrue("f1<f2", c.compare(f1, f2) < 0);
    assertTrue("f2>f1", c.compare(f2, f1) > 0);
  }

}
