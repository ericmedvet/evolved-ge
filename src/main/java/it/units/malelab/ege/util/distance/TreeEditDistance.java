package it.units.malelab.ege.util.distance;

import distance.APTED;
import it.units.malelab.ege.core.Node;
import util.LblTree;

/**
 *
 * @author eric
 */
// from https://github.com/unnonouno/tree-edit-distance/blob/master/tree-edit-distance/src/treedist/TreeEditDistance.java
public class TreeEditDistance<T> implements Distance<Node<T>> {

  @Override
  public double d(Node<T> t1, Node<T> t2) {
    APTED ted = new APTED((float) 1.0, (float) 1.0, (float) 1.0);
    return ted.nonNormalizedTreeDist(
            LblTree.fromString(treeToString(t1)),
            LblTree.fromString(treeToString(t2))
    );
  }

  private String treeToString(Node<T> root) {
    StringBuilder sb = new StringBuilder();
    sb.append("{").append(root.getContent());
    if (!root.getChildren().isEmpty()) {
      for (Node<T> child : root.getChildren()) {
        sb.append(treeToString(child));
      }
    }
    sb.append("}");
    return sb.toString();
  }

}
