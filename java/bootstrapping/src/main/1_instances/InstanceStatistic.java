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
import java.util.LinkedList;

public final class InstanceStatistic {

  private HashMap<PathPattern, Integer> patternFrequency;
  private HashMap<PathPattern, String> sampleSentences;
  private LinkedList<String> sentences;

  private String cause;
  private String effect;

  private int frequency = 0;

  public InstanceStatistic(final ExtractedInstance extractedInstance) {
    this.cause = extractedInstance.getCause();
    this.effect = extractedInstance.getEffect();

    patternFrequency = new HashMap<>();
    sentences = new LinkedList<>();

    sampleSentences = new HashMap<>();

    update(extractedInstance);
  }

  public int getSupport() {
    return patternFrequency.keySet().size();
  }

  public void update(final ExtractedInstance i) {
    if (patternFrequency.containsKey(i.getPattern())) {
      patternFrequency.put(i.getPattern(),
              patternFrequency.get(i.getPattern()) + 1);
    } else {
      patternFrequency.put(i.getPattern(), 1);
      sampleSentences.put(i.getPattern(), i.getSentence());
    }

    sentences.add(i.getSentence());
    frequency++;
  }

  public static int compareFrequency(final InstanceStatistic i1,
                                     final InstanceStatistic i2) {
    return -Integer.compare(i1.frequency, i2.frequency);
  }

  public int getFrequency() {
    return frequency;
  }

  public String getCause() {
    return cause;
  }

  public String getEffect() {
    return effect;
  }

  public static int compareSupport(
          final InstanceStatistic instanceStatistic,
          final InstanceStatistic instanceStatistic1) {
    return -Integer.compare(instanceStatistic.getSupport(),
            instanceStatistic1.getSupport());
  }
}
