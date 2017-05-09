/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util.booleanfunction;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.core.PhenotypePrinter;
import java.util.Arrays;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eric
 */
public class BooleanUtilsTest {
  
  public BooleanUtilsTest() {
  }
  @Test
  public void testBuildCompleteCases() {
    BooleanUtils instance = new BooleanUtils();
    Map<String, boolean[]> result = instance.buildCompleteCases("a", "b", "c");
    assertTrue("'a' should be [f,t,f,t,f,t,f,t]", Arrays.equals(new boolean[]{false, true, false, true, false, true, false, true}, result.get("a")));
    assertTrue("'b' should be [f,f,t,t,f,f,t,t]", Arrays.equals(new boolean[]{false, false, true, true, false, false, true, true}, result.get("b")));
    assertTrue("'c' should be [f,f,f,f,t,t,t,t]", Arrays.equals(new boolean[]{false, false, false, false, true, true, true, true}, result.get("c")));
  }
  
}
