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
import java.util.concurrent.Callable;

public final class PatternExtractor
        implements Callable<LinkedList<ExtractedPattern>> {

  private LinkedList<ExtractedPattern> foundPattern;
  private LinkedList<PreprocessedSentence> sentences;
  private Instance instance;

  public PatternExtractor(final Instance instance,
                          final LinkedList<PreprocessedSentence> sentences) {
    this.sentences = sentences;
    this.instance = instance;
  }

  @Override
  public LinkedList<ExtractedPattern> call() {
    foundPattern = new LinkedList<>();

    for (PreprocessedSentence sentence : sentences) {
      extractTreePattern(sentence.getDependencies());
    }
    sentences.clear();
    return foundPattern;
  }

  private void extractTreePattern(final String dependencies) {
    DiGraph tree = new DiGraph(dependencies);
    String path = PathExtractor.extract(tree, instance);

    if (path == null) {
      return;
    }

    ExtractedPattern p = new ExtractedPattern(path, instance);
    foundPattern.add(p);
  }
}
