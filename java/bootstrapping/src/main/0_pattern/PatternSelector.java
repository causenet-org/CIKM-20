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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PatternSelector {

  private static Logger logger = LogManager.getLogger(PatternSelector.class);

  private static final int SELECTED_PATTERNS_INCREMENTATION = 25;

  private HashMap<String, PatternStatistic> fastSearch;
  private LinkedList<PatternStatistic> statistics;

  private LinkedList<PathPattern> selected;
  private int numSelectedPatternLastIteration = 0;

  public PatternSelector() {
    fastSearch = new HashMap<>();
    statistics = new LinkedList<>();
    selected = new LinkedList<>();
  }

  public void select(final LinkedList<ExtractedPattern> positivePattern) {
    clear();

    positivePattern.sort(ExtractedPattern::compareLength);
    addPattern(positivePattern);

    statistics.removeIf(PatternSelectionRules::canDelete);
    statistics.removeIf(s -> s.getSupport() < 2);
    statistics.sort(PatternStatistic::compareSupport);

    logger.info("Total patterns found: " + statistics.size());
    selectPattern();
    logger.info("Selected patterns: " + selected.size());

    numSelectedPatternLastIteration = selected.size();
  }

  private void selectPattern() {
    for (PatternStatistic selectedPattern : statistics) {
      if (selected.size() == numSelectedPatternLastIteration
              + SELECTED_PATTERNS_INCREMENTATION) {
        break;
      }

      PathPattern newPattern = new PathPattern(
              selectedPattern.getRepresentative().getPattern());

      if (isPalindrome(newPattern)) {
        continue;
      }

      if (!containsReversed(newPattern)) {
        selected.add(newPattern);
      }
    }
  }

  private boolean isPalindrome(final PathPattern newPattern) {
    return newPattern.isReverseOf(newPattern);
  }

  private boolean containsReversed(final PathPattern pattern) {
    for (PathPattern p : selected) {
      if (pattern.isReverseOf(p)) {
        return true;
      }
    }
    return false;
  }

  public void clear() {
    fastSearch.clear();
    statistics.clear();
    selected.clear();
  }

  private void addPattern(final LinkedList<ExtractedPattern> pattern) {
    for (ExtractedPattern p : pattern) {
      if (fastSearch.containsKey(p.getPattern())) {
        fastSearch.get(p.getPattern()).updateSeeds(p);
        continue;
      }

      PatternStatistic stat = new PatternStatistic(p);
      fastSearch.put(p.getPattern(), stat);
      statistics.add(stat);
    }
  }

  public LinkedList<PathPattern> getSelectedPatterns() {
    return selected;
  }

}
