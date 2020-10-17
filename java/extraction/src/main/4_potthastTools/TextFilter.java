import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;

/**
 * A class that checks text based on absolute or relative counts done by a
 * {@link WordFilter}.
 *
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date$
 */
public class TextFilter implements BiPredicate<String, Locale> {

  private WordFilter wordFilter;

  private double minRatio;

  private int minAbsolute;

  /**
   * Creates a new filter using given word filter for counting.
   */
  public TextFilter(final WordFilter wordFilter) {
    this.setWordFilter(wordFilter);
    this.setMinRatio(0.0);
    this.setMinAbsolute(0);
  }

  /**
   * Gets the current word filter that is used in filtering texts.
   */
  public WordFilter getWordFilter() {
    return this.wordFilter;
  }

  /**
   * Gets the current relative number of words of a text (compared to all words
   * in the text) that the word filter  must match so that this filter accepts
   * the text.
   */
  public double getMinRatio() {
    return this.minRatio;
  }

  /**
   * Gets the current minimum number of words of a text that the word filter
   * must match so that this filter accepts the text.
   */
  public int getMinAbsolute() {
    return this.minAbsolute;
  }

  /**
   * Sets the word filter that is used in filtering texts.
   */
  public void setWordFilter(final WordFilter wordFilter) {
    if (wordFilter == null) {
      throw new NullPointerException();
    }
    this.wordFilter = wordFilter;
  }

  /**
   * Sets the relative number of words of a text (compared to all words
   * in the text) that the word filter  must match so that this filter accepts
   * the text.
   */
  public void setMinRatio(final double minRatio) {
    if (minRatio < 0.0) {
      throw new IllegalArgumentException("Negative ratio: " + minRatio);
    }
    this.minRatio = minRatio;
  }

  /**
   * Sets the minimum number of words of a text that the word filter
   * must match so that this filter accepts the text.
   */
  public void setMinAbsolute(final int minAbsolute) {
    if (minAbsolute < 0) {
      throw new IllegalArgumentException("Negative threshold: " + minAbsolute);
    }
    this.minAbsolute = minAbsolute;
  }

  /**
   * Tests whether the text (specified by its words) of given language fulfills
   * the minimum ratio and minimum absolute count requirements of this filter.
   *
   * @see #getWordFilter()
   * @see #getMinAbsolute()
   * @see #getMinRatio()
   */
  @Override
  public boolean test(final String text, final Locale language) {
    final List<String> words = WordFilter.toWords(text, language);
    return this.test(words, language);
  }

  /**
   * Tests whether the text (specified by its words) of given language fulfills
   * the minimum ratio and minimum absolute count requirements of this filter.
   *
   * @see #getWordFilter()
   * @see #getMinAbsolute()
   * @see #getMinRatio()
   */
  public boolean test(final List<String> words, final Locale language) {
    final int numWords = words.size();
    final int numRemaining = this.wordFilter.filterWords(words, language).size();

    final double ratio = ((double) numRemaining) / ((double) numWords);
    return numRemaining >= this.minAbsolute && ratio >= this.minRatio;
  }

}
