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

public final class InstanceExtractor
        implements Callable<LinkedList<ExtractedInstance>> {

  private PathPattern pattern;
  private LinkedList<PreprocessedSentence> sentences;
  private LinkedList<ExtractedInstance> foundSeeds;

  public InstanceExtractor(final PathPattern pattern,
                           final LinkedList<PreprocessedSentence> sentences) {
    this.pattern = pattern;
    this.sentences = sentences;
  }

  @Override
  public LinkedList<ExtractedInstance> call() {
    foundSeeds = new LinkedList<>();

    for (PreprocessedSentence sentence : sentences) {
      extractFromSentencePath(sentence);
    }

    sentences.clear();

    return foundSeeds;
  }

  private void extractFromSentencePath(final PreprocessedSentence sentence) {
    DiGraph g = new DiGraph(sentence.getDependencies());
    LinkedList<String[]> matches = pattern.match(g);

    for (String[] match : matches) {
      if (match == null) {
        continue;
      }
      ExtractedInstance extractedInstance =
              new ExtractedInstance(sentence.toString(), this.pattern);
      extractedInstance.setCausality(match[0], match[1]);
      foundSeeds.add(extractedInstance);
    }
  }
}
