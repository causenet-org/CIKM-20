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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CausalityExtractor extends Thread {

  private static final int TIMEOUT = 5;
  private static Logger logger = LogManager.getLogger(CausalityExtractor.class);

  private LinkedList<PathPattern> pathPatterns;
  private final LinkedList<GeneralSentence> causalSentences;
  private final LinkedList<GeneralSentence> samples;

  public CausalityExtractor(final String pathPatterns) {
    this.pathPatterns = PatternLoader.loadPathPatterns(pathPatterns);
    this.causalSentences = new LinkedList<>();
    this.samples = new LinkedList<>();
  }

  @Override
  public void run() {
    for (GeneralSentence sample : samples) {
      extractCausalityFromSentence(sample);

      if (!sample.hasMatches()) {
        continue;
      }

      causalSentences.add(sample);
    }
  }

  public void extractCausalityFromSentence(final GeneralSentence sample) {
    String sentenceSurface = sample.getSentence();
    if (sentenceSurface.contains("?")) {
      return;
    }

    if (containsNegativeWord(sentenceSurface)) {
      return;
    }

    if (!containsPathWords(sentenceSurface)) {
      return;
    }

    nlp(sample, sentenceSurface);

    try {
      extractCausality(sample);
    } catch (Exception | Error e) {
      logger.info("Skip one sentence.");
    }
  }

  private void nlp(final GeneralSentence sample,
                   final String sentenceSurface) {
    ExecutorService service = Executors.newSingleThreadExecutor();

    try {
      final Future<Boolean> f = service.submit(() -> {
        Document doc = new Document(sentenceSurface);
        Sentence sentence = doc.sentences().get(0);
        sample.setTokens(sentence.tokens());
        String dependencyGraph = sentence.dependencyGraph().toDotFormat();
        sample.setDependencyGraph(dependencyGraph);
        return true;
      });

      f.get(TIMEOUT, TimeUnit.MINUTES);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      service.shutdown();
    }
  }

  private boolean containsPathWords(final String text) {
    for (PathPattern p : this.pathPatterns) {
      if (text.contains(p.getIndicator())) {
        return true;
      }
    }
    return false;
  }

  private boolean containsNegativeWord(final String sentence) {
    return sentence.contains(" no ")
            || sentence.contains(" not ")
            || sentence.contains(" doesn't ")
            || sentence.contains(" didn't ");
  }

  private void extractCausality(final GeneralSentence sample) {
    DiGraph g = new DiGraph(sample.getDependencyGraph());

    for (PathPattern pattern : pathPatterns) {
      LinkedList<String[]> nounPairs = pattern.match(g);
      for (String[] nounPair : nounPairs) {
        Match match = new Match(nounPair, pattern.toString());
        if (!sample.hasMatchAlready(match)) {
          sample.addMatch(match);
        }
      }
    }
  }

  public void addSentence(final GeneralSentence line) {
    samples.add(line);
  }

  public LinkedList<GeneralSentence> getCausalSentences() {
    return causalSentences;
  }
}
