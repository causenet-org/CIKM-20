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

import com.fasterxml.jackson.core.io.JsonStringEncoder;

public final class Match {

  private final String[] nounPair;
  private final String patternSurface;
  private JsonStringEncoder escape = new JsonStringEncoder();

  public Match(final String[] nounPair,
               final String patternSurface) {
    this.nounPair = nounPair;
    this.patternSurface = patternSurface;
  }

  public StringBuilder printMatch() {
    StringBuilder result = new StringBuilder();
    result.append("{");
    result.append(embrace("Cause")).append(": ");
    result.append(nounPair[0]).append(",");
    result.append(embrace("Effect")).append(": ");
    result.append(nounPair[1]).append(",");
    result.append(embrace("Pattern")).append(": ");
    result.append(embrace(escape(patternSurface))).append("}");
    return result;
  }

  private String embrace(final String string) {
    return "\"" + string + "\"";
  }

  private String escape(final String toEscape) {
    if (toEscape == null) {
      return "";
    }
    return new String(escape.quoteAsString(toEscape));
  }

  public String getCause() {
    return nounPair[0];
  }

  public String getEffect() {
    return nounPair[1];
  }
}
