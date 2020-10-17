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

import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

public final class DiGraph {

  private static final int LABEL_START = 7;
  private static final int LABEL_END = 3;

  private HashMap<String, DepNode> nodesById;

  /*
    Class for representing dependency graphs based on a digraph representation.
    Example sentence: "An earthquake in 1882 caused a regional tsunami."
    Example dependency graph:

      digraph  {
        N_1 [label="An/DT-1"];
        N_2 [label="earthquake/NN-2"];
        N_3 [label="in/IN-3"];
        N_4 [label="1882/CD-4"];
        N_5 [label="caused/VBD-5"];
        N_6 [label="a/DT-6"];
        N_7 [label="regional/JJ-7"];
        N_8 [label="tsunami/NN-8"];
        N_9 [label="./.-9"];
        N_2 -> N_1 [label="det"];
        N_2 -> N_4 [label="nmod:in"];
        N_4 -> N_3 [label="case"];
        N_5 -> N_2 [label="nsubj"];
        N_5 -> N_8 [label="dobj"];
        N_5 -> N_9 [label="punct"];
        N_8 -> N_6 [label="det"];
        N_8 -> N_7 [label="amod"];
      }
   */
  public DiGraph(final String diGraphString) {
    nodesById = new HashMap<>();
    createGraph(diGraphString);
  }

  private void createGraph(final String diGraphString) {
    String[] graph = diGraphString.split("\\n");

    for (int i = 1; i < graph.length - 1; i++) {
      if (!graph[i].contains("->")) {
        createNode(graph[i]);
      } else {
        try {
          createEdge(graph[i]);
        } catch (Exception ignored) {
          // ignore edge
        }
      }
    }
  }

  /*
    Parsing example:
        N_2 [label="earthquake/NN-2"];
   */
  private void createNode(final String node) {
    String[] x = node.split("\\[", 2);
    String label = x[1].substring(LABEL_START, x[1].length() - LABEL_END);
    DepNode depNode = new DepNode(x[0].trim(), label);
    nodesById.put(x[0].trim(), depNode);
  }

  /*
    Parsing example:
        N_2 -> N_4 [label="nmod:in"];
   */
  private void createEdge(final String edge) {
    String[] x = edge.split("\\[", 2);
    String[] edgePointer = x[0].split(" -> ");
    String label = x[1].substring(LABEL_START, x[1].length() - LABEL_END);
    nodesById.get(edgePointer[0].trim())
            .addOutgoingEdge(edgePointer[1].trim(), label);
    nodesById.get(edgePointer[1].trim())
            .addIncomingEdge(edgePointer[0].trim(), label);
  }

  public DepNode getNodeMatching(final String string) {
    for (DepNode node : nodesById.values()) {
      if (node.getName().equals(StringUtils.lowerCase(string))) {
        return node;
      }
    }
    return null;
  }

  public int size() {
    return nodesById.keySet().size();
  }

  public Collection<DepNode> nodes() {
    return nodesById.values();
  }

  public DepNode getNode(final String nodeId) {
    return nodesById.get(nodeId);
  }
}
