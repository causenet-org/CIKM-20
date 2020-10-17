/*
 * Copyright (C) 2008 www.webis.de
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;


/**
 * This class provides the functionality to check if a word is within a predefined stop word list.
 * <p>
 * Stop word list are available for the following languages: Dutch, English, Finnish, French, German, Italian,
 * Norwegian, Polish, Portuguese, Spanish, Swedish.
 *
 * @author maik.anderka@medien.uni-weimar.de
 * @version aitools 2.0
 * <p>
 * Created on 11.08.2008
 * <p>
 * $Id$
 */
public class StopWordList {
  private static final int DEFAULT_INITIAL_HASHSET_CAPACITY = 1 << 10;           // 1024.
  private final HashSet<String> stopWords;

  /**
   * Constructs a <tt>StopWordList</tt> for the specified language.
   *
   * @param language Language.
   *                 If no stop word list exists for the specified language.
   * @throws IllegalArgumentException If <code>language == null</code>.
   */
  public StopWordList(String pathStopwordlist, Locale language) {
    try {
      this.stopWords = loadStopWordList(pathStopwordlist);
    } catch (FileNotFoundException e) {
      throw new Error(e);
    }
  }

  public String[] getStopWordList() {
    String[] stopWords = new String[this.stopWords.size()];
    this.stopWords.toArray(stopWords);
    return stopWords;
  }

  private HashSet<String> loadStopWordList(String pathStopwordlist) throws FileNotFoundException {
    File stopWordListFile = new File(pathStopwordlist);
    FileReader fr = new FileReader(stopWordListFile);
    BufferedReader bufferedReader = new BufferedReader(fr);

    HashSet<String> stopWords = new HashSet<>(StopWordList.DEFAULT_INITIAL_HASHSET_CAPACITY);
    try {
      for (String word = bufferedReader.readLine(); word != null; word = bufferedReader.readLine()) {
        stopWords.add(word);
      }
      bufferedReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return stopWords;
  }
}
