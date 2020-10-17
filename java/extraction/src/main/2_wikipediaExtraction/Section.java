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
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

import java.util.LinkedList;

import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

public final class Section {

  private JsonStringEncoder escape = new JsonStringEncoder();

  private String title = null;
  private int level = -1;
  private LinkedList<String> content;
  private LinkedList<Section> children = new LinkedList<>();

  private LinkedList<LinkedList<String>> structuredData;
  private LinkedList<String> semiStructuredData;
  private String parentTitle;

  public LinkedList<StringBuilder> printLists() {
    LinkedList<StringBuilder> result = new LinkedList<>();

    if (semiStructuredData != null && semiStructuredData.size() > 0) {
      for (String semiStructuredInfo : semiStructuredData) {
        if (semiStructuredInfo.trim().isEmpty()) {
          continue;
        }

        StringBuilder list = new StringBuilder();
        list.append("\"").append(escape(parentTitle)).append("\"\t");
        list.append("\"").append(escape(title)).append("\"\t");
        list.append(level).append("\t");
        list.append("\"").append(escape(semiStructuredInfo)).append("\"");
        result.add(list);
      }
    }
    return result;
  }

  public LinkedList<StringBuilder> printInfoboxes() {
    LinkedList<StringBuilder> result = new LinkedList<>();

    if (structuredData != null) {
      for (LinkedList<String> structuredInfo : structuredData) {
        StringBuilder infobox = new StringBuilder();

        String templateName = escape(extractText(title, structuredInfo.get(0)));

        String infoboxTitle = "None";
        if (structuredInfo.get(1) != null
                && structuredInfo.get(1).length() > 0) {
          infoboxTitle = escape(extractText(title, structuredInfo.get(1)));
        }
        String infoboxArgument = escape(
                extractText(title, structuredInfo.get(2)));
        String infoboxValue = escape(
                extractText(title, structuredInfo.get(3)));

        infobox.append("\"").append(templateName).append("\"\t");
        infobox.append("\"").append(infoboxTitle).append("\"\t");
        infobox.append("\"").append(infoboxArgument).append("\"\t");
        infobox.append("\"").append(infoboxValue).append("\"");

        result.add(infobox);
      }
    }

    return result;
  }

  public void setTitle(final String title) {
    this.title = title;
    this.parentTitle = title;
  }

  private void setParentTitle(final String parentTitle) {
    this.parentTitle = parentTitle;
  }

  public void setContent(final String content) {
    this.content = new LinkedList<>();
    for (Sentence sentence : new Document(content).sentences()) {
      String sentenceString = sentence.toString();
      if (!sentenceString.equals(".")) {
        this.content.add(sentenceString);
      }
    }
  }

  private String escape(final String toEscape) {
    if (toEscape == null) {
      return "";
    }
    return new String(escape.quoteAsString(toEscape));
  }

  public void setLevel(final int level) {
    this.level = level;
  }

  public int getLevel() {
    return level;
  }

  public void addChild(final Section section) {
    if (section == this) {
      return;
    }

    if (section.getLevel() == level + 1) {
      section.setParentTitle(title);
      children.add(section);
    } else if (children.size() > 0) {
      children.get(children.size() - 1).addChild(section);
    }
  }

  public void setStructuredData(
          final LinkedList<LinkedList<String>> structuredData) {
    this.structuredData = structuredData;
  }

  private String extractText(final String title,
                             final String wikiText) {
    try {
      return sweble(title, wikiText).getExtractedText();
    } catch (LinkTargetException | EngineException ignored) {
      // ignore
    }
    return wikiText;
  }

  private SwebleResult sweble(final String title,
                              final String wikiText)
          throws LinkTargetException, EngineException {
    WikiConfig config = DefaultConfigEnWp.generate();
    WtEngineImpl engine = new WtEngineImpl(config);

    PageTitle pageTitle = PageTitle.make(config, title);
    PageId pageId = new PageId(pageTitle, -1);
    EngProcessedPage cp = engine.postprocess(pageId, wikiText, null);
    SwebleVisitor p = new SwebleVisitor(config);
    return (SwebleResult) p.go(cp.getPage());
  }

  public void setSemiStructuredData(
          final LinkedList<String> semiStructuredData) {
    this.semiStructuredData = semiStructuredData;
  }

  public LinkedList<Section> getChildren() {
    return children;
  }

  public LinkedList<String> getSentences() {
    return content;
  }

  public String getTitle() {
    return title;
  }

  public boolean canSkip() {
    for (String s : content) {
      if (s.contains("may refer to:")) {
        return true; // disambiguation
      }
    }

    return content.isEmpty() && children.isEmpty();
  }
}
