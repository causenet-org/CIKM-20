import java.util.List;
import java.util.Locale;


/**
 * A sentence extractor that uses a few heuristics to filter undesired
 * paragraphs and sentences. Based on an idea by Martin Potthast.
 *
 * <p>
 * The extractor discard too small paragraphs, sentences with too few function
 * words (also known as stop words), and sentences with too few proper words
 * (naively defined as tokens that only consist of alphabetic characters and
 * hyphens within).
 * </p><p>
 * The default settings are the ones used in
 * </p><pre>
 * Johannes Kiesel, Benno Stein, and Stefan Lucks.
 * A Large-scale Analysis of the Mnemonic Password Advice.
 * In Proceedings of the 24th Annual Network and Distributed System Security Symposium (NDSS 17),
 * February 2017.
 * </pre><p>
 * The settings were found to be the best for extracting complete main content
 * sentences while favoring precision over recall.
 * </p>
 *
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date$
 */
public class PotthastJerichoExtractor extends JerichoHtmlSentenceExtractor {

  public static final String DEFAULT_MATCHING_WORD_PATTERN =
          "^\\p{IsAlphabetic}[-\\p{IsAlphabetic}]*\\p{IsAlphabetic}*$";

  public static final int DEFAULT_MIN_PARAGRAPH_LENGTH = 400;

  public static final int DEFAULT_MIN_NUM_STOP_WORDS_IN_SENTENCE = 1;

  public static final double DEFAULT_MIN_MATCHING_WORD_RATIO = 0.5;

  //////////////////////////////////////////////////////////////////////////////
  //                                   MEMBERS                                //
  //////////////////////////////////////////////////////////////////////////////

  private int minParagraphLengthInCharacters;

  private StopWordFilter stopWordFilter;

  private WordMatchFilter wordMatchFilter;

  private final TextFilter stopWordTextFilter;

  private final TextFilter wordMatchTextFilter;

  //////////////////////////////////////////////////////////////////////////////
  //                                CONSTRUCTORS                              //
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new extractor with default settings. Note that the default
   * settings include to only extract English text.
   */
  public PotthastJerichoExtractor(String pathStopWords) {
    this.setMinParagraphLengthInCharacters(DEFAULT_MIN_PARAGRAPH_LENGTH);
    this.stopWordFilter = new StopWordFilter(pathStopWords, true);
    this.wordMatchFilter = new WordMatchFilter(DEFAULT_MATCHING_WORD_PATTERN);
    this.stopWordTextFilter = new TextFilter(this.stopWordFilter);
    this.setMinStopWordsInSentence(DEFAULT_MIN_NUM_STOP_WORDS_IN_SENTENCE);
    this.wordMatchTextFilter = new TextFilter(this.wordMatchFilter);
    this.setMinMatchingWordRatioInSentence(DEFAULT_MIN_MATCHING_WORD_RATIO);
  }

  public void setMinParagraphLengthInCharacters(
          final int minParagraphLengthInCharacters) {
    this.minParagraphLengthInCharacters = minParagraphLengthInCharacters;
  }

  public void setMinStopWordsInSentence(
          final int minStopWordsInSentence) {
    this.stopWordTextFilter.setMinAbsolute(minStopWordsInSentence);
  }

  public void setMinMatchingWordRatioInSentence(
          final double minMatchingWordRatioInSentence) {
    this.wordMatchTextFilter.setMinRatio(minMatchingWordRatioInSentence);
  }

  @Override
  protected boolean isValidParagraph(
          final String paragraph, final Locale paragraphLanguage) {
    return paragraph.length() >= this.minParagraphLengthInCharacters;
  }

  @Override
  protected boolean isValidSentence(
          final String sentence, final Locale paragraphLanguage) {
    final List<String> words = WordFilter.toWords(sentence, paragraphLanguage);

    return this.stopWordTextFilter.test(words, paragraphLanguage)
            && this.wordMatchTextFilter.test(words, paragraphLanguage);
  }

}
