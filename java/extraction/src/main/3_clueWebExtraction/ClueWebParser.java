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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

public final class ClueWebParser {

  private static final int N_EXTRACTION_THREADS = 16;
  private static Logger logger = LogManager.getLogger(Main.class);
  private PrintWriter printWriter;
  private PotthastJerichoExtractor textExtractor;
  private String pathPatterns;

  public ClueWebParser(final String pathPatterns,
                       final String pathStopWordList,
                       final String pathOutput) {
    this.pathPatterns = pathPatterns;
    textExtractor = new PotthastJerichoExtractor(pathStopWordList);
    try {
      printWriter = new PrintWriter(new FileOutputStream(pathOutput));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void parse(final String path) {
    try {
      read(path);
    } catch (IOException e) {
      e.printStackTrace();
    }

    printWriter.flush();
    printWriter.close();
  }

  private void extractText(final String warcRecordIdUri,
                           final String warcTargetUriStr,
                           final String warcDate,
                           final String html) {
    List<String> sentences = textExtractor.extract(html);

    if (sentences == null || sentences.isEmpty()) {
      return;
    }

    String text = StringUtils.join(sentences, "");
    if (text.trim().equals("")) {
      return;
    }

    LinkedList<ClueWebSentence> clueWebSentences = new LinkedList<>();
    for (String sentenceSurface : sentences) {
      clueWebSentences.add(new ClueWebSentence(
              warcRecordIdUri,
              warcTargetUriStr,
              warcDate,
              sentenceSurface
      ));
    }

    MainExtractor extractor = new MainExtractor(
            pathPatterns, N_EXTRACTION_THREADS);
    extractor.parse(clueWebSentences);

    for (GeneralSentence sentence : extractor.getAllSentences()) {
      StringBuilder result = new StringBuilder();
      result.append("clueweb12_sentence\t");
      result.append(sentence.printSentence());
      result.append("\n");
      printWriter.write(result.toString());
    }
  }

  private void read(final String path) throws IOException {
    InputStream fileStream = new FileInputStream(path);
    if (path.contains(".gz")) {
      fileStream = new GZIPInputStream(fileStream);
    }

    WarcReader reader = WarcReaderFactory.getReader(fileStream);
    WarcRecord record;

    try {
      while ((record = reader.getNextRecord()) != null) {
        String warcRecordIdUri = record.header.warcRecordIdUri.toString();
        String warcTargetUriStr = record.header.warcTargetUriStr;
        String warcDate = record.header.warcDateStr;

        if (record.hasPayload()
                && record.header.contentType.mediaType.equals("http")) {
          InputStream inputStream = record.getPayload().getInputStream();
          StringWriter writer = new StringWriter();
          IOUtils.copy(inputStream, writer, "UTF-8");
          String html = writer.toString();
          extractText(warcRecordIdUri, warcTargetUriStr, warcDate, html);
        }
      }
    } catch (ZipException e) {
      logger.info("Zip Exception, skip file " + path);
    }

    logger.info("Finished " + path);
  }
}
