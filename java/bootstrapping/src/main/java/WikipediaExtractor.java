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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class WikipediaExtractor {

  private static Logger logger = LogManager.getLogger(WikipediaExtractor.class);

  private String pathOutput;

  private PatternExtractionStep patternExtractionStep;
  private InstanceExtractionStep instanceExtractionStep;
  private PatternSelector patternSelector;
  private InstanceSelector instanceSelector;

  private LinkedList<Instance> instances;
  private LinkedList<PathPattern> patterns;

  private int previousSizeInstances = 0;
  private int previousSizePattern = 0;

  public WikipediaExtractor(final String pathLuceneIndex,
                            final String pathSeeds,
                            final String pathOutput) {
    this.pathOutput = pathOutput;
    instances = SeedLoader.load(pathSeeds);

    logger.info("Loading Lucene index for fast search");
    LuceneQueryFramework queryFramework =
            new LuceneQueryFramework(pathLuceneIndex);
    logger.info("Loading Lucene index done");

    patternExtractionStep = new PatternExtractionStep(
            instances, queryFramework);
    patternSelector = new PatternSelector();

    instanceExtractionStep = new InstanceExtractionStep(queryFramework);
    instanceSelector = new InstanceSelector();

    patterns = new LinkedList<>();
  }

  public void bootstrapping() {
    save(0);
    for (int i = 1; true; i++) {
      logger.info("----- Iteration: " + i + " -----");
      previousSizeInstances = instances.size();
      previousSizePattern = patterns.size();

      LinkedList<PathPattern> iterationPattern = patternExtraction();
      mergePatterns(patterns, iterationPattern);
      patternSelector.clear();

      if (i == Main.NUM_ITERATIONS) {
        break;
      }

      mergeInstances(instanceExtraction(patterns));
      instanceSelector.clear();

      save(i);
    }
    patternExtractionStep.finish();
    instanceExtractionStep.finish();
    logger.info("Done");
  }

  private void save(final int iteration) {
    store(instances, iteration + "-instances", previousSizeInstances);
    store(patterns, iteration + "-patterns", previousSizePattern);
  }

  public LinkedList<PathPattern> patternExtraction() {
    patternExtractionStep.extract();
    patternSelector.select(patternExtractionStep.getPatterns());
    return patternSelector.getSelectedPatterns();
  }

  private LinkedList<Instance> instanceExtraction(
          final LinkedList<PathPattern> pattern) {
    instanceExtractionStep.extract(pattern);
    instanceSelector.select(instanceExtractionStep.getInstances());
    return instanceSelector.getSelectedInstances();
  }

  private void mergePatterns(final LinkedList<PathPattern> pattern,
                             final LinkedList<PathPattern> iterationPattern) {
    for (PathPattern i : iterationPattern) {
      boolean duplicate = false;
      for (PathPattern p : pattern) {
        if (p.getPattern().equals(i.getPattern())) {
          duplicate = true;
        }
        if (i.isReverseOf(p)) {
          duplicate = true;
        }
      }

      if (!duplicate) {
        pattern.add(i);
      }
    }
  }

  private void mergeInstances(final LinkedList<Instance> newInstances) {
    for (Instance i : newInstances) {
      boolean duplicate = false;

      for (Instance p : instances) {
        if (i.equals(p)) {
          duplicate = true;
        }
      }

      if (!duplicate) {
        instances.add(i);
      }
    }
  }

  private void store(final LinkedList<?> list,
                     final String filename,
                     int previousSize) {
    PrintWriter pw = null;
    try {
      pw = new PrintWriter(new FileOutputStream(this.pathOutput + filename));
      for (Object o : list) {
        pw.println(o);

        previousSize--;
        if (previousSize == 0) {
          pw.println();
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    if (pw != null) {
      pw.close();
    }
  }
}
