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

public final class PatternSelectionRules {

  private static final int VALID_NUMBER_OF_BRACKETS = 4;
  private static final int MIN_PATTERN_LENGTH = 5;

  private PatternSelectionRules() {

  }

  public static boolean canDelete(final PatternStatistic patternStatistic) {
    ExtractedPattern p = patternStatistic.getRepresentative();

    if (StringUtils.countMatches(p.getPattern(), "[")
            != VALID_NUMBER_OF_BRACKETS) {
      return true;
    }

    if (p.getPattern().contains(",/,")) {
      return true;
    }

    if (!longEnough(p.getPattern())) {
      return true;
    }

    if (p.getPattern().contains(" no ")
            || p.getPattern().contains(" not ")
            || p.getPattern().contains(" doesn't ")
            || p.getPattern().contains(" didn't ")) {
      return true;
    }

    if (p.getPattern().contains("(")) {
      return true;
    }

    if (p.getPattern().contains("\\")) {
      return true;
    }

    return !p.getPattern().replaceAll("\\d", "").equals(p.getPattern());

  }

  private static boolean longEnough(final String pattern) {
    return pattern.split("\t").length >= MIN_PATTERN_LENGTH;
  }
}
