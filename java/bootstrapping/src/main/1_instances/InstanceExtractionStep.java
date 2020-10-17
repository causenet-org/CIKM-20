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

import java.util.HashSet;
import java.util.LinkedList;

public final class InstanceExtractionStep
        extends ParallelExtractor<ExtractedInstance> {

  private LinkedList<ExtractedInstance> instances;
  private HashSet<String> queriedPattern;
  private LuceneQueryFramework queryFramework;

  public InstanceExtractionStep(final LuceneQueryFramework queryFramework) {
    this.queryFramework = queryFramework;
    instances = new LinkedList<>();
    queriedPattern = new HashSet<>();
  }

  public void extract(final LinkedList<PathPattern> pattern) {
    startThreads(pattern);
    joinResults();
  }

  private void startThreads(final LinkedList<PathPattern> pattern) {
    for (PathPattern p : pattern) {
      if (queriedPattern.contains(p.getPattern())) {
        continue;
      }

      InstanceExtractor s = new InstanceExtractor(p, getSentences(p));
      submit(s);
      queriedPattern.add(p.getPattern());
    }
  }

  private LinkedList<PreprocessedSentence> getSentences(final PathPattern p) {
    return queryFramework.patternQuery(p);
  }

  @Override
  protected void addNew(final LinkedList<ExtractedInstance> foundSeeds) {
    instances.addAll(foundSeeds);
  }

  public LinkedList<ExtractedInstance> getInstances() {
    return instances;
  }
}
