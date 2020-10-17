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

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

public final class WikipediaExtractionThread extends Thread {

  private static final int N_EXTRACTION_THREADS = 5;
  private static Logger logger =
          LogManager.getLogger(WikipediaExtractionThread.class);
  private static Pattern sectionPattern = Pattern.compile("(=[=]+.*[=]+=)");

  private JsonStringEncoder escape = new JsonStringEncoder();
  private Semaphore semaphore;
  private PrintWriter printWriter;
  private String pathPatterns;

  // meta informaton
  private String pageId;
  private String title;
  private String revisionId;
  private String timestamp;

  // wikipedia markup text
  private String text;

  public WikipediaExtractionThread(final String pathPatterns,
                                   final String pageId,
                                   final String title,
                                   final String revisionId,
                                   final String timestamp,
                                   final String text,
                                   final PrintWriter printWriter,
                                   final Semaphore semaphore) {
    this.pathPatterns = pathPatterns;
    this.pageId = pageId;
    this.title = title;
    this.revisionId = revisionId;
    this.timestamp = timestamp;
    this.text = text;
    this.printWriter = printWriter;
    this.semaphore = semaphore;
  }

  public StringBuilder meta() {
    StringBuilder result = new StringBuilder();
    result.append(escape(pageId)).append("\t");
    result.append(escape(title)).append("\t");
    result.append(escape(revisionId)).append("\t");
    result.append(escape(timestamp)).append("\t");
    return result;
  }

  @Override
  public void run() {
    Section article = splitArticle();

    if (article.canSkip()) {
      return;
    }

    savePage();

    saveInfoboxes(article);
    saveLists(article);

    extractCausalSentences(article);
  }

  public LinkedList<WikipediaSentence> getSentences(final Section section) {
    LinkedList<WikipediaSentence> sentences = new LinkedList<>();
    for (String sentence : section.getSentences()) {
      sentences.add(new WikipediaSentence(
              section.getTitle(),
              section.getLevel(),
              sentence
      ));
    }

    for (Section child : section.getChildren()) {
      sentences.addAll(getSentences(child));
    }

    return sentences;
  }

  private void extractCausalSentences(final Section section) {
    LinkedList<WikipediaSentence> sentences = getSentences(section);

    MainExtractor extractor = new MainExtractor(
            pathPatterns, N_EXTRACTION_THREADS);
    extractor.parse(sentences);

    for (GeneralSentence sentence : extractor.getAllSentences()) {
      StringBuilder result = new StringBuilder();
      result.append("wikipedia_sentence\t");
      result.append(meta());
      result.append(sentence.printSentence());
      result.append("\n");
      store(result);
    }
  }

  private void saveLists(final Section section) {
    for (StringBuilder payload : section.printLists()) {
      StringBuilder result = new StringBuilder();
      result.append("wikipedia_list\t");
      result.append(meta());
      result.append(payload);
      result.append("\n");
      store(result);
    }

    for (Section child : section.getChildren()) {
      saveLists(child);
    }
  }

  private void saveInfoboxes(final Section section) {
    for (StringBuilder payload : section.printInfoboxes()) {
      StringBuilder result = new StringBuilder();
      result.append("wikipedia_infobox\t");
      result.append(meta());
      result.append(payload);
      result.append("\n");
      store(result);
    }

    for (Section child : section.getChildren()) {
      saveInfoboxes(child);
    }
  }

  private Section splitArticle() {

    Matcher m = sectionPattern.matcher(text);

    Section root = new Section();
    root.setTitle(title);

    int lastEnd = 0;
    String lastHeading = title;

    while (m.find()) {
      Section section;

      if (lastEnd == 0) {
        section = root;
        root.setLevel(1);
      } else {
        section = new Section();
        section.setLevel(StringUtils.countMatches(lastHeading, "=") / 2);
      }

      section.setTitle(extractTitle(title, lastHeading));
      String wikiText = text.substring(lastEnd, m.start(1));
      SwebleResult content = extractFromWikiText(title, wikiText);

      if (content != null) {
        section.setContent(content.getExtractedText());
        section.setStructuredData(content.getStructuredData());
        section.setSemiStructuredData(content.getSemiStructuredData());
      }

      root.addChild(section);

      lastEnd = m.end(1);
      lastHeading = m.group(1);
    }

    Section lastSection = new Section();
    lastSection.setLevel(StringUtils.countMatches(lastHeading, "=") / 2);
    lastSection.setTitle(extractTitle(title, lastHeading));

    return root;
  }

  private synchronized void store(final StringBuilder line) {
    try {
      semaphore.acquire();
      printWriter.write(line.toString());
      semaphore.release();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private String extractTitle(final String title,
                              final String wikiTextTitle) {
    return Objects.requireNonNull(sweble(title, wikiTextTitle
            .replace("=", "").trim())).getExtractedText();
  }

  private SwebleResult extractFromWikiText(final String title,
                                           final String wikiText) {
    return sweble(title, wikiText);
  }

  private SwebleResult sweble(final String title,
                              final String wikiText) {
    WikiConfig config = DefaultConfigEnWp.generate();
    WtEngineImpl engine = new WtEngineImpl(config);

    EngProcessedPage cp = null;
    try {
      PageTitle pageTitle = PageTitle.make(config, title);
      PageId pageId = new PageId(pageTitle, -1);
      cp = engine.postprocess(pageId, wikiText, null);
    } catch (EngineException | LinkTargetException ignored) {
      logger.info("Ignoring problem with: " + title);
    }
    SwebleVisitor p = new SwebleVisitor(config);

    if (cp == null) {
      return null;
    }
    return (SwebleResult) p.go(cp.getPage());
  }

  private void savePage() {
    StringBuilder line = new StringBuilder();
    line.append("wikipedia_page\t");
    line.append(escape(title));
    line.append("\n");
    store(line);
  }


  private String escape(final String toEscape) {
    if (toEscape == null) {
      return "";
    }
    return new String(escape.quoteAsString(toEscape));
  }
}
