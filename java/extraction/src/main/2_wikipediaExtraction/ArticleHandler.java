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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public final class ArticleHandler extends DefaultHandler {

  private static final int N_EXTRACTION_THREADS = 10;
  private static final int ARTICLES_AFTER_PROGRESS_REPORT = 1000000;
  private static final int MAX_QUEUE_SIZE = 10000;
  private static final int THREAD_SLEEP_TIME = 1000;

  private static Logger logger = LogManager.getLogger(ArticleHandler.class);

  private ThreadPoolExecutor executor = (ThreadPoolExecutor)
          Executors.newFixedThreadPool(N_EXTRACTION_THREADS);
  private Semaphore semaphore = new Semaphore(1);
  private PrintWriter printWriter;

  private StringBuilder title = new StringBuilder();
  private StringBuilder pageid = new StringBuilder();
  private StringBuilder revisionid = new StringBuilder();
  private StringBuilder timestamp = new StringBuilder();
  private StringBuilder text = new StringBuilder();

  private boolean idDone = false;
  private boolean revisionDone = false;
  private int pageCount = 0;
  private String currentElement;
  private String pathPatterns;

  public ArticleHandler(final String pathPatterns,
                        final String saveResultPath) {
    this.pathPatterns = pathPatterns;
    try {
      printWriter = new PrintWriter(new FileOutputStream(saveResultPath));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void startElement(final String uri,
                           final String localName,
                           final String name,
                           final Attributes attributes) {
    if (name.equals("page")) {
      text.setLength(0);
      title.setLength(0);
      pageid.setLength(0);
      revisionid.setLength(0);
      timestamp.setLength(0);
      idDone = false;
      revisionDone = false;
    }

    if (name.equals("revision")) {
      revisionDone = true;
    }
    currentElement = name;
  }

  @Override
  public void characters(final char[] ch,
                         final int start,
                         final int length) {
    String token = String.valueOf(ch, start, length);

    if (currentElement.equals("title")) {
      this.title.append(token);
    }

    if (currentElement.equals("text")) {
      text.append(token);
    }

    if (currentElement.equals("timestamp")) {
      this.timestamp.append(token);
    }

    if (currentElement.equals("id") && !idDone && !revisionDone) {
      this.pageid.append(token);
    }

    if (currentElement.equals("id") && !idDone && revisionDone) {
      this.revisionid.append(token);
    }

  }

  @Override
  public void endElement(final String uri,
                         final String localName,
                         final String name) {
    if (name.equals("id") && !idDone && revisionDone) {
      idDone = true;
    }

    if (name.equals("page")) {
      pageCount++;

      WikipediaExtractionThread thread = new WikipediaExtractionThread(
              pathPatterns,
              pageid.toString(),
              title.toString(),
              revisionid.toString(),
              timestamp.toString(),
              text.toString(),
              printWriter,
              semaphore
      );

      executor.submit(thread);

      while (executor.getQueue().size() > MAX_QUEUE_SIZE) {
        try {
          Thread.sleep(THREAD_SLEEP_TIME);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      if (pageCount % ARTICLES_AFTER_PROGRESS_REPORT == 0) {
        logger.info("Already done: " + pageCount);
      }
    }

    currentElement = "";
  }

  public int getPageCount() {
    return pageCount;
  }

  public void finish() {
    executor.shutdown();

    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    printWriter.flush();
    printWriter.close();
  }
}
