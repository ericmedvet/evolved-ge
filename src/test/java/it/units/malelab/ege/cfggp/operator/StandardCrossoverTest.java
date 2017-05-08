/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.cfggp.operator;

import it.units.malelab.ege.core.Node;
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
public class StandardCrossoverTest {
  
  public StandardCrossoverTest() {
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
  public void testApply() {
    Node<Character> t1 = new Node<>('R');
    t1.getChildren().add(new Node<>('a'));
    t1.getChildren().add(new Node<>('A'));
    t1.getChildren().add(new Node<>('A'));
    t1.getChildren().add(new Node<>('B'));
    t1.getChildren().get(1).getChildren().add(new Node<>('b'));
    t1.getChildren().get(1).getChildren().add(new Node<>('c'));
    t1.getChildren().get(2).getChildren().add(new Node<>('d'));
    t1.getChildren().get(2).getChildren().add(new Node<>('e'));
    t1.getChildren().get(2).getChildren().add(new Node<>('f'));
    t1.getChildren().get(3).getChildren().add(new Node<>('g'));
    t1.getChildren().get(3).getChildren().add(new Node<>('h'));
    t1.getChildren().get(3).getChildren().add(new Node<>('B'));
    t1.getChildren().get(3).getChildren().add(new Node<>('A'));
    t1.getChildren().get(3).getChildren().get(2).getChildren().add(new Node<>('i'));
    t1.getChildren().get(3).getChildren().get(2).getChildren().add(new Node<>('l'));
    t1.getChildren().get(3).getChildren().get(3).getChildren().add(new Node<>('m'));
    int maxDepth = 10;
    StandardCrossover<Character> op = new StandardCrossover<>(maxDepth, new Random(1l));
    List<Node<Character>> parents = new ArrayList<>();
    parents.add(t1);
    parents.add(t1);
    for (int i = 0; i<50; i++) {
      Node<Character> parent1 = parents.get(0);
      Node<Character> parent2 = parents.get(1);
      List<Node<Character>> children = op.apply(parents);
      assertTrue("parent1 should remain unchanged", parent1==parents.get(0));
      assertTrue("parent2 should remain unchanged", parent2==parents.get(1));
      if (children!=null) {
        assertEquals("there should be 2 children", 2, children.size());
        assertTrue("child1 depth should be <="+maxDepth, children.get(0).depth()<=maxDepth);
        assertTrue("child2 depth should be <="+maxDepth, children.get(0).depth()<=maxDepth);
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
