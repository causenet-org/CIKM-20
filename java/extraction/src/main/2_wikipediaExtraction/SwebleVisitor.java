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

import de.fau.cs.osr.ptk.common.AstVisitor;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngPage;
import org.sweble.wikitext.parser.nodes.WtBold;
import org.sweble.wikitext.parser.nodes.WtExternalLink;
import org.sweble.wikitext.parser.nodes.WtHorizontalRule;
import org.sweble.wikitext.parser.nodes.WtIllegalCodePoint;
import org.sweble.wikitext.parser.nodes.WtImageLink;
import org.sweble.wikitext.parser.nodes.WtInternalLink;
import org.sweble.wikitext.parser.nodes.WtItalics;
import org.sweble.wikitext.parser.nodes.WtListItem;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtNodeList;
import org.sweble.wikitext.parser.nodes.WtOrderedList;
import org.sweble.wikitext.parser.nodes.WtPageSwitch;
import org.sweble.wikitext.parser.nodes.WtParagraph;
import org.sweble.wikitext.parser.nodes.WtSection;
import org.sweble.wikitext.parser.nodes.WtTagExtension;
import org.sweble.wikitext.parser.nodes.WtTemplate;
import org.sweble.wikitext.parser.nodes.WtTemplateArgument;
import org.sweble.wikitext.parser.nodes.WtTemplateParameter;
import org.sweble.wikitext.parser.nodes.WtText;
import org.sweble.wikitext.parser.nodes.WtUnorderedList;
import org.sweble.wikitext.parser.nodes.WtUrl;
import org.sweble.wikitext.parser.nodes.WtWhitespace;
import org.sweble.wikitext.parser.nodes.WtXmlCharRef;
import org.sweble.wikitext.parser.nodes.WtXmlComment;
import org.sweble.wikitext.parser.nodes.WtXmlElement;
import org.sweble.wikitext.parser.nodes.WtXmlEntityRef;
import org.sweble.wikitext.parser.parser.LinkTargetException;

public final class SwebleVisitor extends AstVisitor<WtNode> {

  private final WikiConfig config;
  private StringBuilder stringBuilder;

  // for infoboxes
  private LinkedList<LinkedList<String>> structuredData = new LinkedList<>();
  private String currentTemplateName = "";
  private String currentInfoboxName = "";

  // for lists
  private LinkedList<String> semiStructuredData = new LinkedList<>();

  public SwebleVisitor(final WikiConfig config) {
    this.config = config;
  }

  @Override
  protected WtNode before(final WtNode node) {
    stringBuilder = new StringBuilder();
    return super.before(node);
  }

  @Override
  protected Object after(final WtNode node, final Object result) {
    return new SwebleResult(stringBuilder.toString(),
            structuredData, semiStructuredData);
  }

  public void visit(final WtSection s) {
    iterate(s.getHeading());
    iterate(s.getBody());
  }

  public void visit(final WtParagraph p) {
    iterate(p);
  }

  public void visit(final WtText text) {
    stringBuilder.append(text.getContent());
  }

  public void visit(final WtWhitespace w) {
    stringBuilder.append(" ");
  }

  public void visit(final WtNode n) {
    stringBuilder.append(" ");
  }

  public void visit(final WtNodeList n) {
    handleList(n);
  }

  public void visit(final WtUnorderedList e) {
    handleList(e);
  }

  public void visit(final WtOrderedList e) {
    handleList(e);
  }

  private void handleList(final WtNode node) {
    StringBuilder tmp = stringBuilder;
    stringBuilder = new StringBuilder();
    iterate(node);
    if (stringBuilder.length() > 0) {
      semiStructuredData.add(stringBuilder.toString());
    }
    tmp.append(stringBuilder);
    stringBuilder = tmp;
  }

  public void visit(final WtListItem item) {
    stringBuilder.append("\n");
    iterate(item);
  }

  public void visit(final EngPage p) {
    iterate(p);
  }

  public void visit(final WtBold b) {
    iterate(b);
  }

  public void visit(final WtItalics i) {
    iterate(i);
  }

  public void visit(final WtXmlCharRef cr) {
    try {
      stringBuilder.append(Character.toChars(cr.getCodePoint()));
    } catch (IllegalArgumentException ignored) {
      // ignore
    }
  }

  public void visit(final WtInternalLink link) {
    try {
      if (link.getTarget().isResolved()) {
        PageTitle page = PageTitle.make(config, link.getTarget().getAsString());
        if (page.getNamespace().equals(config.getNamespace("Category"))) {
          return;
        }
      }
    } catch (LinkTargetException e) {
      e.printStackTrace();
    }

    stringBuilder.append(link.getPrefix());
    if (!link.hasTitle()) {
      iterate(link.getTarget());
    } else {
      iterate(link.getTitle());
    }
    stringBuilder.append(link.getPostfix());
  }

  public void visit(final WtXmlElement e) {
    if (!e.getName().equalsIgnoreCase("br")) {
      iterate(e.getBody());
    } else {
      stringBuilder.append("\n");
    }
  }

  public void visit(final WtExternalLink link) {
    if (!link.hasTitle()) {
      iterate(link.getTarget());
    } else {
      iterate(link.getTitle());
    }
  }

  public void visit(final WtXmlEntityRef er) {
    iterate(er);
    String ch = er.getResolved();
    if (ch == null) {
      stringBuilder.append('&');
      stringBuilder.append(er.getName());
      stringBuilder.append(';');
    } else {
      stringBuilder.append(ch);
    }
  }

  public void visit(final WtUrl wtUrl) {
    if (!wtUrl.getProtocol().isEmpty()) {
      stringBuilder.append(wtUrl.getProtocol());
      stringBuilder.append(':');
    }
    stringBuilder.append(wtUrl.getPath());
  }

  public void visit(final WtTemplate node) {
    if (!node.getName().isResolved()) {
      return;
    }

    currentTemplateName = node.getName().getAsString().trim();
    iterate(node.getArgs());
  }

  public void visit(final WtTemplateArgument node) {
    if (!node.getName().isResolved()) {
      return;
    }

    String argument = node.getName().getAsString().trim().toLowerCase();
    try {
      if (argument.equals("name") || argument.equals("title")) {
        StringBuilder tmp = stringBuilder;
        stringBuilder = new StringBuilder();
        iterate(node.getValue());
        currentInfoboxName = stringBuilder.toString().trim();
        stringBuilder = tmp;
      }
    } catch (UnsupportedOperationException e) {
      currentInfoboxName = "";
    }

    try {
      String[] causalArguments = {"cause", "causes", "symptom",
              "symptoms", "risks", "result"};

      for (String potentialArgument : causalArguments) {
        if (argument.toLowerCase().equals(potentialArgument)) {
          StringBuilder tmp = stringBuilder;
          stringBuilder = new StringBuilder();
          iterate(node.getValue());
          String value = stringBuilder.toString().trim();
          if (!value.equals("")) {
            LinkedList<String> structuredInfo = new LinkedList<>();
            structuredInfo.add(currentTemplateName);
            structuredInfo.add(currentInfoboxName);
            structuredInfo.add(argument);
            structuredInfo.add(value);
            structuredData.add(structuredInfo);
          }
          stringBuilder = tmp;
        }
      }
    } catch (UnsupportedOperationException e) {
      e.printStackTrace();
    }
  }

  public void visit(final WtHorizontalRule hr) {
  }

  public void visit(final WtImageLink n) {
  }

  public void visit(final WtIllegalCodePoint n) {
  }

  public void visit(final WtXmlComment n) {
  }

  public void visit(final WtTemplateParameter n) {
  }

  public void visit(final WtTagExtension n) {
  }

  public void visit(final WtPageSwitch n) {
  }
}
