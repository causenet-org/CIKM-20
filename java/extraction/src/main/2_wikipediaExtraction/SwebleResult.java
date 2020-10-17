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

public final class SwebleResult {
  private final String extractedText;
  private final LinkedList<LinkedList<String>> structuredData;
  private LinkedList<String> semiStructuredData;

  public SwebleResult(final String extractedText,
                      final LinkedList<LinkedList<String>> structuredData,
                      final LinkedList<String> semiStructuredData) {
    this.extractedText = extractedText;
    this.structuredData = structuredData;
    this.semiStructuredData = semiStructuredData;
  }

  public String getExtractedText() {
    return extractedText;
  }

  public LinkedList<String> getSemiStructuredData() {
    return semiStructuredData;
  }

  public LinkedList<LinkedList<String>> getStructuredData() {
    return structuredData;
  }
}
