/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.util;

import it.units.malelab.ege.core.Grammar;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eric
 */
public class GrammarUtilsTest {
  
  public GrammarUtilsTest() {
  }

  @Test
  public void testComputeSymbolsMinMaxDepths() throws IOException {
    String[] grammars = new String[]{"grammars/max-grammar.bnf", "grammars/text.bnf", "grammars/mapper.bnf", "grammars/symbolic-regression.bnf"};
    for (String grammar : grammars) {
      Grammar<String> g = Utils.parseFromFile(new File(grammar));
      Map<String, Pair<Double, Double>> depths = GrammarUtils.computeSymbolsMinMaxDepths(g);
      assertTrue("Should contain all non-terminal symbols.", depths.keySet().containsAll(g.getRules().keySet()));
      for (String s : depths.keySet()) {
        if (!g.getRules().containsKey(s)) {
          assertTrue("Terminal should have min=1", depths.get(s).getFirst()==1);
          assertTrue("Terminal should have max=1", depths.get(s).getSecond()==1);
        } else {
          assertTrue("Should have min<=max", depths.get(s).getFirst()<=depths.get(s).getSecond());
        }
      }
    }
  }
  
}
