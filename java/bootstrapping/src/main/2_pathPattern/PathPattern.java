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

public final class PathPattern {

  private String pathString;
  private String[] parts;

  public PathPattern(final String path) {
    pathString = path;
    parts = path.split("\t");
  }

  /*
    Tries to match path pattern to digraph
   */
  public LinkedList<String[]> match(final DiGraph diGraph) {
    LinkedList<String[]> matches = new LinkedList<>();

    for (DepNode n : diGraph.nodes()) {
      matches.addAll(tryToMatch(diGraph, n));
    }

    return matches;
  }

  private LinkedList<String[]> tryToMatch(final DiGraph diGraph,
                                          final DepNode start) {
    int position = 0;

    return recursiveMatching(diGraph, start, position, start);
  }

  private LinkedList<String[]> recursiveMatching(final DiGraph diGraph,
                                                 final DepNode start,
                                                 int position,
                                                 final DepNode end) {
    LinkedList<String[]> result = new LinkedList<>();

    if (position >= parts.length) {
      return result;
    }

    if (!matches(parts[position], end)) {
      return result;
    }

    position++;

    if (position == parts.length) {
      // match!!
      result.add(extractCauseAndEffectFromGraph(start, end));
      return result;
    }

    for (String adjacentNode : end.getAdjacency()) {
      if (end.getIncident(adjacentNode).equals(parts[position])) {
        result.addAll(recursiveMatching(
                diGraph,
                start,
                position + 1,
                diGraph.getNode(adjacentNode)));
      }
    }

    return result;
  }

  private String[] extractCauseAndEffectFromGraph(final DepNode start,
                                                  final DepNode end) {
    if (start == end) {
      return null;
    }

    boolean causeFirst = parts[0].contains("[[cause]]");

    String[] i;

    if (causeFirst) {
      i = new String[]{start.getName(), end.getName()};
    } else {
      i = new String[]{end.getName(), start.getName()};
    }

    return i;
  }

  private boolean matches(final String pathNode,
                          final DepNode node) {
    String[] nodeParts = pathNode.split("/");
    String pathNodeName = nodeParts[0];
    String pathNodePOS = nodeParts[1];

    String nodeName = node.getName();
    String nodePOS = node.getPOS();

    if (pathNodeName.contains("[[cause]]")
            || pathNodeName.contains("[[effect]]")) {
      return isNoun(nodePOS);
    }

    return pathNodePOS.equals(nodePOS) && pathNodeName.equals(nodeName);
  }

  private boolean isNoun(final String string) {
    return string.equals("NNS")
            || string.equals("NNP")
            || string.equals("NN")
            || string.equals("NNPS");
  }

  public String getPattern() {
    return pathString;
  }

  public String[] getParts() {
    return parts;
  }

  @Override
  public String toString() {
    return pathString;
  }

  public boolean isReverseOf(final PathPattern pathPattern) {
    if (parts.length != pathPattern.parts.length) {
      return false;
    }

    int i = pathPattern.parts.length - 1;
    for (String part : parts) {
      String compareTo = pathPattern.parts[i];

      if (part.contains("[[") && compareTo.contains("[[")) {
        i--;
        continue;
      }

      if (part.contains("/")) {
        if (!part.equals(compareTo)) {
          return false;
        }
      } else if (!part.substring(1).equals(compareTo.substring(1))) {
        return false;
      }

      i--;
    }
    return true;
  }
}
