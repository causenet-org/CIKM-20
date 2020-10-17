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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.MMapDirectory;

public final class LuceneQueryFramework {

  private IndexSearcher searcher;
  private IndexReader reader;

  public LuceneQueryFramework(final String pathLuceneIndex) {
    try {
      MMapDirectory dir = (MMapDirectory) MMapDirectory.open(
              Paths.get(pathLuceneIndex));
      dir.setPreload(true);
      reader = DirectoryReader.open(dir);
      searcher = new IndexSearcher(reader);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public LinkedList<PreprocessedSentence> instanceQuery(
          final String causeSeed, final String effectSeed) {
    LinkedList<PreprocessedSentence> sentences = new LinkedList<>();
    String queryString = "(sentence:\"" + causeSeed;
    queryString += "\") AND (sentence:\"" + effectSeed + "\")";
    try {
      Analyzer analyzer = new StandardAnalyzer();
      QueryParser parser = new QueryParser("sentence", analyzer);
      Query query = parser.parse(queryString);
      TopDocs docs = searcher.search(query, reader.numDocs());

      for (ScoreDoc d : docs.scoreDocs) {
        Document document = searcher.doc(d.doc);
        PreprocessedSentence s = new PreprocessedSentence(
                document.get("depTree")
        );
        sentences.add(s);
      }
    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }

    return sentences;
  }

  public LinkedList<PreprocessedSentence> patternQuery(
          final PathPattern pattern) {
    LinkedList<PreprocessedSentence> sentences = new LinkedList<>();
    String[] parts = pattern.getParts();

    StringBuilder treeQuery = new StringBuilder();

    for (int i = 1; i < parts.length - 1; i++) {
      String part = parts[i];

      if (i % 2 == 1) {
        // remove direction from edges
        part = part.substring(1);
      }

      treeQuery.append("depTree:\"").append(part).append("\"");

      if (i != parts.length - 2) {
        treeQuery.append(" AND ");
      }
    }

    try {
      Analyzer analyzer = new StandardAnalyzer();
      QueryParser parser = new QueryParser("sentence", analyzer);
      Query query = parser.parse(treeQuery.toString());
      TopDocs docs = searcher.search(query, searcher.count(query));

      for (ScoreDoc d : docs.scoreDocs) {
        Document document = searcher.doc(d.doc);
        sentences.add(new PreprocessedSentence(
                document.get("depTree")
        ));
      }
    } catch (ParseException | IOException e) {
      e.printStackTrace();
    }
    return sentences;
  }
}
