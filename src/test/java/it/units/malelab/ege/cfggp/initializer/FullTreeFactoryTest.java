/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.cfggp.initializer;

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
public class FullTreeFactoryTest {

  public FullTreeFactoryTest() {
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
   * Test of build method, of class FullTreeFactory.
   */
  @Ignore("re-enable")
  @Test
  public void testBuild() throws IOException {
    Random random = new Random(1);
    Grammar<String> g = Utils.parseFromFile(new File("grammars/max-grammar.bnf"));
    int maxDepth = 8;
    int currentDepth = 2;
    GrowTreeFactory<String> f = new GrowTreeFactory<>(maxDepth, g);
    List<String> nonTerminals = new ArrayList<>(g.getRules().keySet());
    for (int i = 0; i < 100; i++) {
      String symbol = nonTerminals.get(random.nextInt(nonTerminals.size()));
      Node<String> tree = f.build(random, symbol, maxDepth, currentDepth);

      if (tree != null) {
        System.out.printf("%d %d<%d%n", i, tree.depth() + currentDepth, maxDepth);
      } else {
        System.out.printf("%d null%n", i);
      }

      if (tree != null) {
        assertEquals("tree root should be the arg symbol", symbol, tree.getContent());
        assertTrue("tree depth+" + currentDepth + " should be <=" + maxDepth, tree.depth() + currentDepth <= maxDepth);
      }
    }
  }

}
