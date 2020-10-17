import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Filters words that are not stop words.
 *
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date$
 */
public class StopWordFilter extends WordFilter {

  private final Map<Locale, StopWordPredicate> stopWordLists;

  private final boolean ignoreCase;
  private String pathStopWords;

  /**
   * Create a new filter that discards all words that do not match a stop word
   * in the stop word list of the respective language.
   *
   * @param pathStopWords
   * @param ignoreCase Whether stop words should be checked ignoring case
   */
  public StopWordFilter(String pathStopWords, final boolean ignoreCase) {
    this.pathStopWords = pathStopWords;
    this.stopWordLists = new HashMap<>();
    this.ignoreCase = ignoreCase;
  }

  /**
   * Adds given stop words to the list for the given language.
   */
  public void addStopWords(
          final Locale language, final String[] words) {
    if (language == null) {
      throw new NullPointerException();
    }
    if (words == null) {
      throw new NullPointerException();
    }
    StopWordPredicate predicate = this.stopWordLists.get(language);
    if (predicate == null) {
      predicate = new StopWordPredicate(language);
      this.stopWordLists.put(language, predicate);
    }
    predicate.addStopWords(words);
  }

  @Override
  public Predicate<String> getPredicate(final Locale language) {
    final Predicate<String> predicate = this.stopWordLists.get(language);
    if (predicate != null) {
      return predicate;
    } else if (this.stopWordLists.containsKey(language)) {
      throw new IllegalArgumentException("Language not supported: " + language);
    } else {
      synchronized (this.stopWordLists) {
        if (!this.stopWordLists.containsKey(language)) {
          try {
            final String[] stopWords =
                    new StopWordList(pathStopWords, language).getStopWordList();
            this.addStopWords(language, stopWords);
          } catch (final Error e) {
            e.printStackTrace();
            this.stopWordLists.put(language, null);
          }
        }
      }
      return this.getPredicate(language);
    }
  }

  protected class StopWordPredicate implements Predicate<String> {

    private final Set<String> stopWords;

    private final Locale language;

    public StopWordPredicate(final Locale language) {
      if (language == null) {
        throw new NullPointerException();
      }
      this.stopWords = new HashSet<>();
      this.language = language;
    }

    @Override
    public boolean test(final String word) {
      return this.stopWords.contains(this.normalize(word));
    }

    protected void addStopWords(final String[] words) {
      for (final String word : words) {
        this.stopWords.add(this.normalize(word));
      }
    }

    protected String normalize(final String word) {
      if (StopWordFilter.this.ignoreCase) {
        return word.toLowerCase(this.language);
      } else {
        return word;
      }
    }
  }
}
