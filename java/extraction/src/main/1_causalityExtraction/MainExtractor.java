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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import java.util.concurrent.TimeUnit;

public final class MainExtractor {

  private static final int TIMEOUT = 15;

  private LinkedList<GeneralSentence> causalSentences = new LinkedList<>();
  private String pathPatterns;
  private int numberOfThreads;

  private ThreadPoolExecutor executor;

  public MainExtractor(final String pathPatterns,
                       final int numberOfThreads) {
    this.pathPatterns = pathPatterns;
    this.numberOfThreads = numberOfThreads;
    executor = (ThreadPoolExecutor)
            Executors.newFixedThreadPool(numberOfThreads);
  }

  public void parse(final LinkedList<? extends GeneralSentence> sentences) {
    LinkedList<CausalityExtractor> extractors = new LinkedList<>();
    for (int i = 0; i < numberOfThreads; i++) {
      CausalityExtractor causalityExtractor =
              new CausalityExtractor(pathPatterns);
      extractors.add(causalityExtractor);
    }

    distributeSentences(sentences, extractors);

    for (CausalityExtractor extractor : extractors) {
      executor.submit(extractor);
    }

    executor.shutdown();

    try {
      executor.awaitTermination(TIMEOUT, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    for (CausalityExtractor extractor : extractors) {
      causalSentences.addAll(extractor.getCausalSentences());
    }
  }

  private void distributeSentences(
          final LinkedList<? extends GeneralSentence> sentences,
          final LinkedList<CausalityExtractor> extractors) {
    int i = 0;
    for (GeneralSentence sentence : sentences) {
      i++;
      extractors.get(i % numberOfThreads).addSentence(sentence);
    }
  }

  public LinkedList<GeneralSentence> getAllSentences() {
    return causalSentences;
  }
}
