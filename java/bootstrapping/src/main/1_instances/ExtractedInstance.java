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

import org.apache.commons.lang3.StringUtils;

public final class ExtractedInstance {

  private String sentence;
  private PathPattern pattern;

  private String cause;
  private String effect;

  public ExtractedInstance(final String sentence,
                           final PathPattern pattern) {
    this.sentence = sentence;
    this.pattern = pattern;
  }

  public void setCausality(final String cause,
                           final String effect) {
    this.cause = StringUtils.lowerCase(cause.trim());
    this.effect = StringUtils.lowerCase(effect.trim());
  }

  public String getCause() {
    return cause;
  }

  public String getEffect() {
    return effect;
  }

  public PathPattern getPattern() {
    return pattern;
  }

  public String getSentence() {
    return sentence;
  }

  @Override
  public String toString() {
    return "[" + getCause() + "," + getEffect() + "]";
  }
}
