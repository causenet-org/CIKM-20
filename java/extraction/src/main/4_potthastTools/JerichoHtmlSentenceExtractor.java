import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.ibm.icu.text.BreakIterator;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date$
 */
public abstract class JerichoHtmlSentenceExtractor {

  private String paragraphSeparator;

  private boolean separateParagraphs;

  private boolean extractAltTexts = true;

  /**
   * Creates a new extractor that only extracts English paragraphs and does not
   * separate the output sentences by the paragraphs they came from.
   */
  public JerichoHtmlSentenceExtractor() {
    Logger.getLogger("net.htmlparser.jericho").setLevel(Level.OFF);
    this.setDoNotSeparateParagraphs();
  }

  public final void setDoNotSeparateParagraphs() {
    this.setParagraphSeparator(null);
    this.separateParagraphs = false;
  }

  public void setParagraphSeparator(final String paragraphSeparator) {
    this.paragraphSeparator = paragraphSeparator;
    this.separateParagraphs = true;
  }


  //////////////////////////////////////////////////////////////////////////////
  //                               FUNCTIONALITY                              //
  //////////////////////////////////////////////////////////////////////////////

  protected List<String> extract(final String htmlInput) {
    try {
      if (htmlInput == null) {
        return null;
      }

      final List<String> paragraphs = this.extractParagraphs(htmlInput);
      if (paragraphs == null) {
        return null;
      }
      final List<String> sentences = new ArrayList<>();
      boolean firstParagraph = true;
      for (final String paragraph : paragraphs) {
        final List<String> paragraphSentences =
                this.extractSentencesFromParagraph(paragraph);
        if (!paragraphSentences.isEmpty()) {
          if (firstParagraph) {
            firstParagraph = false;
          } else if (this.separateParagraphs) {
            sentences.add(this.paragraphSeparator);
          }
          sentences.addAll(paragraphSentences);
        }
      }
      return sentences;
    } catch (NullPointerException | IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Renders the HTML page with Jericho, normalizes sequences
   * of whitespace characters to a single whitespace, and returns the list of
   * paragraphs. Return <tt>null</tt> on a fatal rendering error.
   */
  protected List<String> extractParagraphs(final String htmlInput) {
    try {
      final Source source = new Source(htmlInput);
      final Segment segment = new Segment(source, 0, htmlInput.length());
      final Renderer renderer = new Renderer(segment);
      renderer.setMaxLineLength(0);
      renderer.setIncludeHyperlinkURLs(false);
      renderer.setIncludeAlternateText(this.extractAltTexts);
      final String[] paragraphsArray = renderer.toString().split("\n");
      final List<String> paragraphs = new ArrayList<>(paragraphsArray.length);
      for (final String paragraph : paragraphsArray) {
        paragraphs.add(this.normalizeWhitespace(paragraph));
      }
      return paragraphs;
    } catch (final Error error) {
      return null;
    }
  }

  /**
   * Detects the language of the paragraph, checks whether it is a target
   * language and it {@link #isValidParagraph(String, Locale)}, and returns the
   * sentences from it. Returns an empty list when the paragraph is empty, from
   * a non-target language, or not valid.
   */
  protected List<String> extractSentencesFromParagraph(final String paragraph) {
    final Locale paragraphLanguage = Locale.ENGLISH;
    if (paragraphLanguage == null
            || !this.isValidParagraph(paragraph, paragraphLanguage)) {
      return Collections.emptyList();
    }
    return this.extractSentencesFromParagraph(paragraph, paragraphLanguage);
  }

  /**
   * Extract sentence from the given paragraph of given language. This is
   * called after it was checked that the paragraph is in a target language and
   * valid.
   */
  protected List<String> extractSentencesFromParagraph(
          final String paragraph, final Locale paragraphLanguage) {
    // they are not thread-safe, so we create a new one each time
    final BreakIterator segmenter =
            BreakIterator.getSentenceInstance(paragraphLanguage);

    final List<String> sentences = new ArrayList<String>();
    for (final String sentence : this.getSegments(paragraph, segmenter)) {
      if (!sentence.isEmpty()) {
        if (this.isValidSentence(sentence, paragraphLanguage)) {
          sentences.add(sentence);
        }
      }
    }
    return sentences;
  }

  /**
   * Checks whether given paragraph of given language should be extracted.
   * <p>
   * The default implementation of this method always return true.
   * </p>
   */
  protected boolean isValidParagraph(
          final String paragraph, final Locale paragraphLanguage) {
    return true;
  }

  /**
   * Checks whether given sentence of given language should be extracted.
   * <p>
   * The sentence is from a paragraph of given language that is valid according
   * to {@link #isValidParagraph(String, Locale)}.
   * </p><p>
   * The default implementation of this method always return true.
   * </p>
   */
  protected boolean isValidSentence(
          final String sentence, final Locale paragraphLanguage) {
    return true;
  }

  /**
   * Normalizes sequences of whitespace characters and trims the text.
   */
  protected String normalizeWhitespace(final String text) {
    return text.replaceAll("\\s+", " ").trim();
  }

  /**
   * Uses given break iterator to segment the text.
   */
  protected List<String> getSegments(
          final String text, final BreakIterator segmenter) {
    segmenter.setText(text);

    final List<String> segments = new ArrayList<>();
    int begin = segmenter.first();
    int end = segmenter.next();
    while (end != BreakIterator.DONE) {
      segments.add(text.substring(begin, end).trim());
      begin = end;
      end = segmenter.next();
    }

    return segments;
  }

}
