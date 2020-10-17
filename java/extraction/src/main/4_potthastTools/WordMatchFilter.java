
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Filters words that do not match a specific pattern.
 *
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date$
 */
public class WordMatchFilter extends WordFilter {

  private PatternPredicate predicate;

  /**
   * Creates a new filter that discards all words that do not match the regular
   * pattern.
   */
  public WordMatchFilter(final String pattern) {
    this.setPattern(pattern);
  }

  /**
   * Creates a new filter that discards all words that do not match the regular
   * pattern.
   */
  public WordMatchFilter(final Pattern pattern) {
    this.setPattern(pattern);
  }

  /**
   * Gets the current pattern.
   */
  public Pattern getPattern() {
    return this.predicate.pattern;
  }

  /**
   * Sets the regular expression pattern.
   * <p>
   * This filter will discard all words that do <b>not</b> match this pattern.
   * </p>
   */
  public void setPattern(final String pattern) {
    this.setPattern(Pattern.compile(pattern));
  }

  /**
   * Sets the regular expression pattern.
   * <p>
   * This filter will discard all words that do <b>not</b> match this pattern.
   * </p>
   */
  public void setPattern(final Pattern pattern) {
    this.predicate = new PatternPredicate(pattern);
  }

  @Override
  public Predicate<String> getPredicate(final Locale language) {
    return this.predicate;
  }

  protected class PatternPredicate implements Predicate<String> {

    private final Pattern pattern;

    protected PatternPredicate(final Pattern pattern) {
      if (pattern == null) {
        throw new NullPointerException();
      }
      this.pattern = pattern;
    }

    @Override
    public boolean test(final String word) {
      return this.pattern.matcher(word).matches();
    }

  }

}
