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
import edu.stanford.nlp.simple.Token;

import java.util.LinkedList;
import java.util.List;

public abstract class GeneralSentence {

  private List<Token> tokens;
  private JsonStringEncoder escape = new JsonStringEncoder();
  private LinkedList<Match> matches = new LinkedList<>();

  protected String sentence;
  protected String dependencyGraph;

  public GeneralSentence(final String sentence) {
    this.sentence = sentence;
  }

  public abstract StringBuilder printSentenceMeta();

  protected final StringBuilder printSentence() {
    StringBuilder result = new StringBuilder();
    result.append(printSentenceMeta());
    result.append("\"").append(escape(sentence)).append("\"\t");
    result.append(printTokens()).append("\t");
    result.append("\"").append(escape(dependencyGraph)).append("\"\t");
    result.append(printMatches());
    return result;
  }

  private StringBuilder printMatches() {
    StringBuilder result = new StringBuilder();
    result.append("[");
    for (Match match : matches) {
      result.append(match.printMatch());
      result.append(",");
    }

    result.setLength(result.length() - 1);
    result.append("]");
    return result;
  }

  private StringBuilder printTokens() {
    StringBuilder result = new StringBuilder();
    result.append("[");
    for (Token token : tokens) {
      result.append("\"");
      result.append(escape(token.originalText()));
      result.append("\"");
      result.append(",");
    }
    result.setLength(result.length() - 1);
    result.append("]");
    return result;
  }

  public final String getDependencyGraph() {
    return dependencyGraph;
  }

  public final void setTokens(final List<Token> tokens) {
    this.tokens = tokens;
  }

  protected final String escape(final String toEscape) {
    if (toEscape == null) {
      return "";
    }
    return new String(escape.quoteAsString(toEscape));
  }

  public final void addMatch(final Match match) {
    matches.add(match);
  }

  public final boolean hasMatches() {
    return matches.size() > 0;
  }

  public final void setDependencyGraph(final String dependencyGraph) {
    this.dependencyGraph = dependencyGraph;
  }

  public final String getSentence() {
    return sentence;
  }

  public final boolean hasMatchAlready(final Match match) {
    for (Match m : matches) {
      if (m.getCause().equals(match.getCause())
              && m.getEffect().equals(match.getEffect())) {
        return true;
      }
    }
    return false;
  }
}
