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

public final class PathPatternGeneralizer {

  private LinkedList<DepNode> path;
  private String generalizedPath;

  public PathPatternGeneralizer() {
    path = new LinkedList<>();
  }

  public void add(final DepNode depNode) {
    path.addFirst(depNode);
  }

  public void generalize(final Instance instance) {
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < path.size() - 1; i++) {
      DepNode curr = path.get(i);
      DepNode next = path.get(i + 1);

      result.append(curr.toString()).append("\t");
      result.append(curr.getLabel(next)).append("\t");
    }

    result.append(path.get(path.size() - 1).toString());
    generalizedPath = generalize(result.toString(), instance);
  }

  private String generalize(String path, final Instance instance) {
    path = path.replaceAll("(?i)" + instance.getCause() + "/(NNS|NNP|NN|NNPS)",
            "[[cause]]/N");
    return path.replaceAll("(?i)" + instance.getEffect() + "/(NNS|NNP|NN|NNPS)",
            "[[effect]]/N");
  }

  @Override
  public String toString() {
    return generalizedPath;
  }
}
