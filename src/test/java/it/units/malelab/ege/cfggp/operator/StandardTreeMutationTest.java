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
import org.junit.Ignore;

/**
 *
 * @author eric
 */
public class StandardTreeMutationTest {

  public StandardTreeMutationTest() {
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
   * Test of apply method, of class StandardMutation.
   */
  @Ignore("re-enable")
  @Test
  public void testApply() throws IOException {
    Random random = new Random(1);
    Grammar<String> g = Utils.parseFromFile(new File("grammars/max-grammar.bnf"));
    int maxDepth = 10;
    GrowTreeFactory<String> f = new GrowTreeFactory<>(maxDepth, g);
    StandardTreeMutation<String> op = new StandardTreeMutation<>(maxDepth, g);
    List<Node<String>> parents = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
      parents.clear();
      parents.add(f.build(random));
      Node<String> parent1 = parents.get(0);
      List<Node<String>> children = op.apply(parents, random);
      assertTrue("parent1 should remain unchanged", parent1 == parents.get(0));
      if (children != null) {
        assertEquals("there should be 1 child", 1, children.size());
        assertTrue("child depth should be <=" + maxDepth, children.get(0).depth() <= maxDepth);
        assertTrue("child should valid", Utils.validate(children.get(0), g));
      }
    }
  }

}
