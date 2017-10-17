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
  @Test
  public void testBuild() throws IOException {
    Random random = new Random(1);
    int maxDepth = 12;
    String[] grammars = new String[]{"grammars/max-grammar.bnf", "grammars/text.bnf", "grammars/mapper.bnf", "grammars/symbolic-regression.bnf"};
    for (String grammar : grammars) {
      Grammar<String> g = Utils.parseFromFile(new File(grammar));
      FullTreeFactory<String> f = new FullTreeFactory<>(maxDepth, g);
      for (int i = 0; i < 100; i++) {
        Node<String> tree = f.build(random);
        assertEquals("tree root should be the starting symbol", g.getStartingSymbol(), tree.getContent());
        assertTrue("tree depth should be <=" + maxDepth, tree.depth() <= maxDepth);
      }
    }
  }

}
