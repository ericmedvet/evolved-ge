/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.cfggp.operator;

import it.units.malelab.ege.cfggp.initializer.GrowTreeFactory;
import it.units.malelab.ege.core.Grammar;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.util.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
public class StandardTreeCrossoverTest {
  
  public StandardTreeCrossoverTest() {
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
   * Test of apply method, of class StandardCrossover.
   */
  @Test
  public void testApply() throws IOException {
    Random random = new Random(1);
    Grammar<String> g = Utils.parseFromFile(new File("grammars/max-grammar.bnf"));
    int maxDepth = 10;
    GrowTreeFactory<String> f = new GrowTreeFactory<>(maxDepth, g);
    Node<String> t1 = f.build(random);
    Node<String> t2 = f.build(random);
    StandardTreeCrossover<String> op = new StandardTreeCrossover<>(maxDepth);
    List<Node<String>> parents = new ArrayList<>();
    parents.add(t1);
    parents.add(t2);
    for (int i = 0; i<50; i++) {
      Node<String> parent1 = parents.get(0);
      Node<String> parent2 = parents.get(1);
      List<Node<String>> children = op.apply(parents, random);
      assertTrue("parent1 should remain unchanged", parent1==parents.get(0));
      assertTrue("parent2 should remain unchanged", parent2==parents.get(1));
      if (children!=null) {
        assertEquals("there should be 2 children", 2, children.size());
        assertTrue("child1 depth should be <="+maxDepth, children.get(0).depth()<=maxDepth);
        assertTrue("child2 depth should be <="+maxDepth, children.get(1).depth()<=maxDepth);
        assertTrue("child1 should valid", Utils.validate(children.get(0), g));
        assertTrue("child2 should valid", Utils.validate(children.get(1), g));
        if (children.get(0).depth()>parent1.depth()) {
          parents.set(0, children.get(0));
        }
        if (children.get(1).depth()>parent2.depth()) {
          parents.set(1, children.get(1));
        }
      }
    }
  }
 
}
