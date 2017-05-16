/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark;

import it.units.malelab.ege.core.Node;
import it.units.malelab.ege.util.Pair;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eric
 */
public class KLandscapesTest {

  private final Map<String, Double> v = new LinkedHashMap<>();
  private final Map<Pair<String, String>, Double> w = new LinkedHashMap<>();

  private final Node<String> tree1;
  private final Node<String> tree2;
  private final Node<String> tree3;

  public KLandscapesTest() {
    v.put("n0", 0.1);
    v.put("n1", 0.2);
    v.put("t0", 0.3);
    v.put("t1", 0.4);
    w.put(new Pair<>("n0", "n0"), 0.1);
    w.put(new Pair<>("n0", "n1"), 0.2);
    w.put(new Pair<>("n0", "t0"), 0.3);
    w.put(new Pair<>("n0", "t1"), 0.4);
    w.put(new Pair<>("n1", "n0"), 0.5);
    w.put(new Pair<>("n1", "n1"), 0.6);
    w.put(new Pair<>("n1", "t0"), 0.7);
    w.put(new Pair<>("n1", "t1"), 0.8);
    tree1 = new Node<>("t0");
    tree2 = new Node<>("n0");
    tree2.getChildren().add(new Node<>("t0"));
    tree2.getChildren().add(new Node<>("t1"));
    tree3 = new Node<>("n0");
    tree3.getChildren().add(new Node<>("t0"));
    tree3.getChildren().add(new Node<>("n1"));
    tree3.getChildren().get(1).getChildren().add(new Node<>("t0"));
    tree3.getChildren().get(1).getChildren().add(new Node<>("t1"));
  }

  @Test
  public void testF() {
    assertEquals("f(" + tree1 + ",1) should be 0.3", 0.3, KLandscapes.f(tree1, 1, v, w), 0.0001);
    assertEquals("f(" + tree2 + ",1) should be 1.0/2.0*1.05", 1.0 / 2.0 * 1.05, KLandscapes.f(tree2, 1, v, w), 0.0001);
    assertEquals("f(" + tree2 + ",2) should be 1.05", 1.05, KLandscapes.f(tree2, 2, v, w), 0.0001);
    assertEquals("f(" + tree3 + ",1) should be 1.0/3.0*1.43", 1.0 / 3.0 * 1.43, KLandscapes.f(tree3, 1, v, w), 0.0001);
    assertEquals("f(" + tree3 + ",2) should be 1.0/2.0*1.774", 1.0 / 2.0 * 2.206, KLandscapes.f(tree3, 2, v, w), 0.0001);
  }

  @Test
  public void testFK() {
    assertEquals("fK(" + tree1 + ",1) should be 0.3", 0.3, KLandscapes.fK(tree1, 1, v, w), 0.0001);
    assertEquals("fK(" + tree2 + ",1) should be 0.1+1.3*0.3+1.4*0.4", 0.1 + 1.3 * 0.3 + 1.4 * 0.4, KLandscapes.fK(tree2, 1, v, w), 0.0001);
    assertEquals("fK(" + tree3 + ",1) should be 0.1+1.3*0.3+1.2*0.2", 0.1 + 1.3 * 0.3 + 1.2 * 0.2, KLandscapes.fK(tree3, 1, v, w), 0.0001);
    assertEquals("fK(" + tree1 + ",2) should be 0.3", 0.3, KLandscapes.fK(tree1, 2, v, w), 0.0001);
    assertEquals("fK(" + tree2 + ",2) should be 0.1+1.3*0.3+1.4*0.4", 0.1 + 1.3 * 0.3 + 1.4 * 0.4, KLandscapes.fK(tree2, 2, v, w), 0.0001);
    assertEquals("fK(" + tree3 + ",2) should be 0.1+1.3*0.3+1.2*(0.2+1.7*0.3+1.8*0.4)", 0.1 + 1.3 * 0.3 + 1.2 * (0.2 + 1.7 * 0.3 + 1.8 * 0.4), KLandscapes.fK(tree3, 2, v, w), 0.0001);
  }

  @Test
  public void testOptimum() {
    for (int k = 0; k < 4; k++) {
      List<Node<String>> all = new ArrayList<>();
      for (int l = 1; l<=k+1; l++) {
        all.addAll(buildAllTrees(l, 2, 2));
      }
      double bestF = Double.NEGATIVE_INFINITY;
      for (Node<String> tree : all) {
        double f = KLandscapes.f(tree, k, v, w);
        if (f > bestF) {
          bestF = f;
        }
      }
      assertEquals("f(., "+k+") of optimum should be the largest among all trees of level "+(k+1),
              bestF,
              KLandscapes.f(KLandscapes.optimum(k, 2, 2, 2, v, w), k, v, w),
              0.001);
    }
  }

  private List<Node<String>> buildAllTrees(int depth, int nTerminals, int nNonTerminals) {
    //assume arity==2 for simplicity
    List<Node<String>> all = new ArrayList<>();
    if (depth == 1) {
      for (int i = 0; i < nTerminals; i++) {
        all.add(new Node<>("t" + i));
      }
    } else {
      List<Node<String>> subtrees = buildAllTrees(depth - 1, nTerminals, nNonTerminals);
      for (int i = 0; i < nNonTerminals; i++) {
        for (Node<String> leftChild : subtrees) {
          for (Node<String> rigthChild : subtrees) {
            Node<String> node = new Node<>("n" + i);
            node.getChildren().add(leftChild);
            node.getChildren().add(rigthChild);
            all.add(node);
          }
        }
      }
    }
    return all;
  }

  @Test
  public void testLevelEqualTree() {
    int[] indexes = new int[]{0, 1, 2};
    int arity = 2;
    Node<String> t = new Node<>("n0");
    t.getChildren().add(new Node<>("n1"));
    t.getChildren().add(new Node<>("n1"));
    t.getChildren().get(0).getChildren().add(new Node<>("t2"));
    t.getChildren().get(0).getChildren().add(new Node<>("t2"));
    t.getChildren().get(1).getChildren().add(new Node<>("t2"));
    t.getChildren().get(1).getChildren().add(new Node<>("t2"));
    Node<String> result = KLandscapes.levelEqualTree(indexes, arity);
    assertEquals("Tree should be " + t, t, result);
  }

  @Test
  public void testMaxFK() {
    List<Node<String>> subtrees = new ArrayList<>();
    subtrees.add(tree3);
    subtrees.add(tree3.getChildren().get(0));
    subtrees.add(tree3.getChildren().get(1));
    subtrees.add(tree3.getChildren().get(1).getChildren().get(0));
    subtrees.add(tree3.getChildren().get(1).getChildren().get(1));
    for (int k = 0; k < 5; k++) {
      double maxFk = Double.NEGATIVE_INFINITY;
      for (Node<String> subtree : subtrees) {
        double fK = KLandscapes.fK(subtree, k, v, w);
        if (fK > maxFk) {
          maxFk = fK;
        }
      }
      assertEquals("maxFK(" + tree3 + "," + k + ") should be " + maxFk, maxFk, KLandscapes.maxFK(tree3, k, v, w), 0.0001);
    }
  }

}
