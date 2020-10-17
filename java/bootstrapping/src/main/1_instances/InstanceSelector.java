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

public final class InstanceSelector {

  private static Logger logger = LogManager.getLogger(InstanceSelector.class);

  private static final int NUMBER_OF_INITIAL_SEEDS = 8;
  private static final int SELECTED_SEEDS_INCREMENTATION = 10;

  private int selectedPreviousIteration = NUMBER_OF_INITIAL_SEEDS;

  private HashMap<String, InstanceStatistic> causeEffectMatch;
  private LinkedList<InstanceStatistic> statistics;
  private LinkedList<Instance> selected;

  public InstanceSelector() {
    statistics = new LinkedList<>();
    selected = new LinkedList<>();
    causeEffectMatch = new HashMap<>();
  }

  public void select(final LinkedList<ExtractedInstance> instances) {
    clear();
    createStatistics(instances);
    logger.info("Total instances found: " + statistics.size());

    statistics.sort(InstanceStatistic::compareFrequency);
    statistics.removeIf(i -> i.getFrequency() < 2);
    statistics.removeIf(i -> i.getSupport() < 2);
    statistics.sort(InstanceStatistic::compareSupport);

    for (InstanceStatistic s : statistics) {
      if (s.getCause().contains("/")
              || s.getEffect().contains("/")
              || s.getCause().contains("%")
              || s.getEffect().contains("%")
              || s.getCause().contains("\\")
              || s.getEffect().contains("\\")) {
        continue;
      }

      if (s.getCause().equals(s.getEffect())) {
        continue;
      }

      selected.add(new Instance(s.getCause(), s.getEffect()));

      if (selected.size() == selectedPreviousIteration
              + SELECTED_SEEDS_INCREMENTATION) {
        break;
      }
    }

    logger.info("Selected instances: " + selected.size());
    selectedPreviousIteration = selected.size();
  }

  private void createStatistics(final LinkedList<ExtractedInstance> instances) {
    for (ExtractedInstance i : instances) {
      updateStatistics(i);
    }
  }

  private void updateStatistics(final ExtractedInstance extractedInstance) {
    String matcherCE = "[" + extractedInstance.getCause()
            + "," + extractedInstance.getEffect() + "]";

    if (causeEffectMatch.containsKey(matcherCE)) {
      InstanceStatistic match = causeEffectMatch.get(matcherCE);
      match.update(extractedInstance);
      return;
    }

    InstanceStatistic s = new InstanceStatistic(extractedInstance);
    statistics.add(s);
    causeEffectMatch.put(matcherCE, s);
  }

  public LinkedList<Instance> getSelectedInstances() {
    return selected;
  }

  public void clear() {
    statistics.clear();
    selected.clear();
    causeEffectMatch.clear();
  }
}
