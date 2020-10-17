import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.ibm.icu.text.BreakIterator;

/**
 * Filters words based on their language.
 *
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date$
 */
public abstract class WordFilter implements BiPredicate<String, Locale> {

  /**
   * Gets the test for a specific language.
   */
  public abstract Predicate<String> getPredicate(final Locale language);

  @Override
  public boolean test(final String word, final Locale language) {
    final Predicate<String> predicate = this.getPredicate(language);
    return predicate.test(word);
  }

  /**
   * Segments the text into words and returns those words that passes the
   * {@link #test(String, Locale)}.
   */
  public List<String> filterWords(final String text, final Locale language) {
    final List<String> words = WordFilter.toWords(text, language);
    return this.filterWords(words, language);
  }

  /**
   * Returns those words that passes the {@link #test(String, Locale)} in a new
   * list.
   */
  public List<String> filterWords(
          final List<String> words, final Locale language) {
    if (language == null) {
      throw new NullPointerException();
    }
    final Predicate<String> predicate = this.getPredicate(language);
    final List<String> remaining = new ArrayList<>(words.size());
    for (final String word : words) {
      if (predicate.test(word)) {
        remaining.add(word);
      }
    }
    return remaining;

  }

  /**
   * Segments the text into words, using a segmenter for the given language.
   */
  public static List<String> toWords(final String text, final Locale language) {
    final BreakIterator segmenter = BreakIterator.getWordInstance(language);
    segmenter.setText(text);

    final List<String> segments = new ArrayList<>();
    int begin = segmenter.first();
    int end = segmenter.next();
    while (end != BreakIterator.DONE) {
      final String segment = text.substring(begin, end).trim();
      if (!segment.isEmpty()) {
        segments.add(segment);
      }
      begin = end;
      end = segmenter.next();
    }

    return segments;
  }

}
