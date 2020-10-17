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

public final class PatternExtractionStep
        extends ParallelExtractor<ExtractedPattern> {

  private LinkedList<ExtractedPattern> patterns = new LinkedList<>();
  private LinkedList<Instance> seeds;
  private LuceneQueryFramework queryFramework;

  private HashSet<String> queriedInstances = new HashSet<>();

  public PatternExtractionStep(final LinkedList<Instance> seeds,
                               final LuceneQueryFramework queryFramework) {
    this.seeds = seeds;
    this.queryFramework = queryFramework;
  }

  public void extract() {
    startThreads();
    joinResults();
  }

  private void startThreads() {
    for (Instance instance : seeds) {
      if (queriedInstances.contains(instance.toString())) {
        continue;
      }
      PatternExtractor p = new PatternExtractor(
              instance,
              getSentences(instance)
      );
      submit(p);
      queriedInstances.add(instance.toString());
    }
  }

  private LinkedList<PreprocessedSentence> getSentences(
          final Instance instance) {
    return queryFramework.instanceQuery(
            instance.getCause(),
            instance.getEffect()
    );
  }

  @Override
  protected void addNew(final LinkedList<ExtractedPattern> foundPattern) {
    patterns.addAll(foundPattern);
  }

  public LinkedList<ExtractedPattern> getPatterns() {
    return patterns;
  }
}
