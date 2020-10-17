/*
 * MIT License
 *
 * Copyright (c) 2020 Stefan Heindorf, Yan Scholten, Henning Wachsmuth,
 * Axel-Cyrille Ngonga Ngomo, Martin Potthast
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

import java.util.LinkedList;
import java.util.Set;

public final class PathExtractor {

  private PathExtractor() {

  }

  public static String extract(final DiGraph diGraph,
                               final Instance instance) {
    DepNode causeNode = diGraph.getNodeMatching(instance.getCause());
    DepNode effectNode = diGraph.getNodeMatching(instance.getEffect());

    if (causeNode == null || effectNode == null) {
      return null;
    }

    PathPatternGeneralizer result =
            shortestPath(diGraph, causeNode, effectNode);

    if (result == null) {
      return null;
    }

    result.generalize(instance);

    return result.toString();
  }

  private static PathPatternGeneralizer shortestPath(
          final DiGraph diGraph,
          final DepNode start,
          final DepNode end) {

    LinkedList<DepNode> queue = new LinkedList<>();

    queue.addLast(start);

    DepNode node;
    while ((node = queue.removeFirst()) != null) {
      visit(diGraph, queue, node.getAdjacency(), node);

      if (node == end) {
        break;
      }

      if (queue.isEmpty()) {
        return null;
      }
    }

    if (node != end) {
      return null;
    }

    return constructPath(start, end);
  }

  private static void visit(final DiGraph diGraph,
                            final LinkedList<DepNode> queue,
                            final Set<String> adjacentNodes,
                            final DepNode node) {
    for (String adjId : adjacentNodes) {
      DepNode adjNode = diGraph.getNode(adjId);

      if (adjNode == null || adjNode.visited()) {
        continue;
      }

      adjNode.setPredecessor(node);
      queue.addLast(adjNode);
    }
  }

  private static PathPatternGeneralizer constructPath(
          final DepNode start,
          final DepNode end) {
    PathPatternGeneralizer p = new PathPatternGeneralizer();

    DepNode pred = end;
    while (pred != null && pred != start) {
      p.add(pred);
      pred = pred.getPredecessor();
    }

    if (pred == start) {
      p.add(start);
    }

    return p;
  }

}
