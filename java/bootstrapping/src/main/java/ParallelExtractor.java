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

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class ParallelExtractor<T> {

  private LinkedList<Future<LinkedList<T>>> tasks = new LinkedList<>();
  private ThreadPoolExecutor executor;

  public ParallelExtractor() {
    executor = (ThreadPoolExecutor)
            Executors.newFixedThreadPool(Main.MAX_THREADS);
  }

  protected abstract void addNew(LinkedList<T> foundT);

  protected final void submit(final Callable<LinkedList<T>> callable) {
    tasks.add(executor.submit(callable));
  }

  protected final void joinResults() {
    for (Future<LinkedList<T>> future : tasks) {
      LinkedList<T> foundT = null;
      try {
        foundT = future.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
      if (foundT == null || foundT.isEmpty()) {
        continue;
      }
      addNew(foundT);
    }
    tasks.clear();
  }

  protected final void finish() {
    executor.shutdown();
  }
}
