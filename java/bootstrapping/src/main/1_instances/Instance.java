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

public final class Instance {

  private String cause;
  private String effect;

  public Instance(final String cause,
                  final String effect) {
    this.cause = cause;
    this.effect = effect;
  }

  public String getCause() {
    return cause;
  }

  public String getEffect() {
    return effect;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Instance)) {
      return false;
    }
    Instance cmp = (Instance) obj;
    return cause.equals(cmp.cause) && effect.equals(cmp.effect);
  }

  @Override
  public int hashCode() {
    return cause.hashCode() + effect.hashCode();
  }

  @Override
  public String toString() {
    return "[" + cause + "," + effect + "]";
  }

}
