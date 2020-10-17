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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class WikipediaParser {

  private ArticleHandler handler;
  private static Logger logger = LogManager.getLogger(ArticleHandler.class);

  public void parseDump(final String wikipediaDump,
                        final String pathPatterns,
                        final String saveResult) {
    handler = new ArticleHandler(pathPatterns, saveResult);
    try {
      parse(wikipediaDump);
    } catch (Exception e) {
      e.printStackTrace();
    }
    handler.finish();
    logger.info("Total processed: " + handler.getPageCount());
  }

  private void parse(final String file)
          throws IOException, ParserConfigurationException, SAXException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    InputSource br = getInputStreamFromCompressedFile(file);
    saxParser.parse(br, handler);
  }

  private InputSource getInputStreamFromCompressedFile(final String fileIn)
          throws FileNotFoundException {
    InputStream fis = new FileInputStream(new File(fileIn));
    Reader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);
    InputSource is = new InputSource(reader);
    is.setEncoding("UTF-8");
    return is;
  }

}
