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

import java.util.HashMap;
import java.util.Set;

public final class DepNode {

  private int nodeId;
  private String name;
  private String POS;

  private HashMap<String, String> edges;

  public DepNode(final String nodeId, final String label) {
    String[] x = label.split("/");
    this.nodeId = Integer.parseInt(nodeId.replace("N_", "")) - 1;
    name = x[0];
    POS = x[1].split("-")[0];
    edges = new HashMap<>();
  }

  public void addOutgoingEdge(final String to,
                              final String label) {
    edges.put(to, "+" + label);
  }

  public void addIncomingEdge(final String from,
                              final String label) {
    edges.put(from, "-" + label);
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name + "/" + POS;
  }

  public String getIncident(final String node) {
    return edges.get(node);
  }

  public String getPOS() {
    return POS;
  }

  public Set<String> getAdjacency() {
    return edges.keySet();
  }

  public int getNodeId() {
    return nodeId;
  }

}
