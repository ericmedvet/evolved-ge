/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.mapper;

import it.units.malelab.ege.MapperGenerationExperimenter;
import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.util.Utils;
import static it.units.malelab.ege.util.Utils.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
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
public class RecursiveMapperTest {

  private Node<String> standardGERawTree;

  public RecursiveMapperTest() {
    standardGERawTree = MapperGenerationExperimenter.getGERawTree();
  }

  private static BitsGenotype bg8(int... values) {
    StringBuilder sb = new StringBuilder();
    for (int value : values) {
      StringBuilder valueSB = new StringBuilder();
      valueSB.append(Integer.toBinaryString(value));
      while (valueSB.length() < 8) {
        valueSB.insert(0, "0");
      }
      sb.append(valueSB.reverse().toString());
    }
    BitsGenotype g = new BitsGenotype(sb.toString());
    return g;
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
   * Test of map method, of class RecursiveMapper.
   */
  @Test
  public void testMap() throws Exception {
    //test mapping of hand-crafted geno with standard GE
    BitsGenotype genotype = bg8(0, 2, 1, 3, 2, 0, 2, 1);
    Map<String, Object> report = new HashMap<>();
    RecursiveMapper instance = new RecursiveMapper<>(standardGERawTree, 10, 3, Utils.parseFromFile(new File("grammars/symbolic-regression-classic4.bnf")));;
    Node<String> result = instance.map(genotype, report);
    Node<String> expected = node("<expr>",
            node("<op>",
                    node("*")),
            node("<expr>",
                    node("<pre-op>",
                            node("log")),
                    node("<expr>",
                            node("<var>",
                                    node("x")))),
            node("<expr>",
                    node("<var>",
                            node("1.0"))));
    assertEquals("Result with standard GE should be log(x)*1", expected, result);
  }

}
