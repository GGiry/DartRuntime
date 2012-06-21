/*
 * This class is loosely based on OpenJDK java.math.BigInteger.
 * All introduced bugs are mine. Remi
 */

/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * Portions Copyright (c) 1995  Colin Plumb.  All rights reserved.
 */

package jdart.runtime;

import java.util.Arrays;

public class BigInt implements Comparable<BigInt> {
  /**
   * The signum of this BigInteger: -1 for negative, 0 for zero, or 1 for
   * positive. Note that the BigInteger zero <i>must</i> have a signum of 0.
   * This is necessary to ensures that there is exactly one representation for
   * each BigInteger value.
   * 
   * @serial
   */
  final int signum;

  /**
   * The magnitude of this BigInteger, in <i>big-endian</i> order: the zeroth
   * element of this array is the most-significant int of the magnitude. The
   * magnitude must be "minimal" in that the most-significant int ({@code mag[0]}
   * ) must be non-zero. This is necessary to ensure that there is exactly one
   * representation for each BigInteger value. Note that this implies that the
   * BigInteger zero has a zero-length mag array.
   */
  final int[] mag;

  /**
   * This mask is used to obtain the value of an int as if it were unsigned.
   */
  final static long LONG_MASK = 0xffffffffL;

  // Constructors

  /**
   * This private constructor translates an int array containing the
   * two's-complement binary representation of a BigInteger into a BigInteger.
   * The input array is assumed to be in <i>big-endian</i> int-order: the most
   * significant int is in the zeroth element.
   */
  private BigInt(int[] val) {
    if (val[0] < 0) {
      mag = makePositive(val);
      signum = -1;
    } else {
      mag = trustedStripLeadingZeroInts(val);
      signum = (mag.length == 0 ? 0 : 1);
    }
  }

  /**
   * Translates the String representation of a BigInteger in the specified radix
   * into a BigInteger. The String representation consists of an optional minus
   * or plus sign followed by a sequence of one or more digits in the specified
   * radix. The character-to-digit mapping is provided by
   * {@code Character.digit}. The String may not contain any extraneous
   * characters (whitespace, for example).
   * 
   * @param val
   *          String representation of BigInteger.
   * @param radix
   *          radix to be used in interpreting {@code val}.
   * @throws NumberFormatException
   *           {@code val} is not a valid representation of a BigInteger in the
   *           specified radix, or {@code radix} is outside the range from
   *           {@link Character#MIN_RADIX} to {@link Character#MAX_RADIX},
   *           inclusive.
   * @see Character#digit
   */
  /*
   * private BigNum(String val, int radix) { int cursor = 0, numDigits; final
   * int len = val.length();
   * 
   * if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) throw new
   * NumberFormatException("Radix out of range"); if (len == 0) throw new
   * NumberFormatException("Zero length BigInteger");
   * 
   * // Check for at most one leading sign int sign = 1; int index1 =
   * val.lastIndexOf('-'); int index2 = val.lastIndexOf('+'); if ((index1 +
   * index2) <= -1) { // No leading sign character or at most one leading sign
   * character if (index1 == 0 || index2 == 0) { cursor = 1; if (len == 1) throw
   * new NumberFormatException("Zero length BigInteger"); } if (index1 == 0)
   * sign = -1; } else throw new
   * NumberFormatException("Illegal embedded sign character");
   * 
   * // Skip leading zeros and compute number of digits in magnitude while
   * (cursor < len && Character.digit(val.charAt(cursor), radix) == 0) cursor++;
   * if (cursor == len) { signum = 0; mag = ZERO.mag; dvalue = 0.0; return; }
   * 
   * numDigits = len - cursor; signum = sign;
   * 
   * // Pre-allocate array of expected size. May be too large but can // never
   * be too small. Typically exact. int numBits = (int)(((numDigits *
   * bitsPerDigit[radix]) >>> 10) + 1); int numWords = (numBits + 31) >>> 5;
   * int[] magnitude = new int[numWords];
   * 
   * // Process first (potentially short) digit group int firstGroupLen =
   * numDigits % digitsPerInt[radix]; if (firstGroupLen == 0) firstGroupLen =
   * digitsPerInt[radix]; String group = val.substring(cursor, cursor +=
   * firstGroupLen); magnitude[numWords - 1] = Integer.parseInt(group, radix);
   * if (magnitude[numWords - 1] < 0) throw new
   * NumberFormatException("Illegal digit");
   * 
   * // Process remaining digit groups int superRadix = intRadix[radix]; int
   * groupVal = 0; while (cursor < len) { group = val.substring(cursor, cursor
   * += digitsPerInt[radix]); groupVal = Integer.parseInt(group, radix); if
   * (groupVal < 0) throw new NumberFormatException("Illegal digit");
   * destructiveMulAdd(magnitude, superRadix, groupVal); } // Required for cases
   * where the array was overallocated. mag =
   * trustedStripLeadingZeroInts(magnitude); dvalue = 0.0; }
   * 
   * 
   * 
   * 
   * 
   * // bitsPerDigit in the given radix times 1024 // Rounded up to avoid
   * underallocation. private static long bitsPerDigit[] = { 0, 0, 1024, 1624,
   * 2048, 2378, 2648, 2875, 3072, 3247, 3402, 3543, 3672, 3790, 3899, 4001,
   * 4096, 4186, 4271, 4350, 4426, 4498, 4567, 4633, 4696, 4756, 4814, 4870,
   * 4923, 4975, 5025, 5074, 5120, 5166, 5210, 5253, 5295};
   * 
   * // Multiply x array times word y in place, and add word z private static
   * void destructiveMulAdd(int[] x, int y, int z) { // Perform the
   * multiplication word by word long ylong = y & LONG_MASK; long zlong = z &
   * LONG_MASK; int len = x.length;
   * 
   * long product = 0; long carry = 0; for (int i = len-1; i >= 0; i--) {
   * product = ylong * (x[i] & LONG_MASK) + carry; x[i] = (int)product; carry =
   * product >>> 32; }
   * 
   * // Perform the addition long sum = (x[len-1] & LONG_MASK) + zlong; x[len-1]
   * = (int)sum; carry = sum >>> 32; for (int i = len-2; i >= 0; i--) { sum =
   * (x[i] & LONG_MASK) + carry; x[i] = (int)sum; carry = sum >>> 32; } }
   */

  /**
   * Translates the decimal String representation of a BigInteger into a
   * BigInteger. The String representation consists of an optional minus sign
   * followed by a sequence of one or more decimal digits. The
   * character-to-digit mapping is provided by {@code Character.digit}. The
   * String may not contain any extraneous characters (whitespace, for example).
   * 
   * @param val
   *          decimal String representation of BigInteger.
   * @throws NumberFormatException
   *           {@code val} is not a valid representation of a BigInteger.
   * @see Character#digit
   */
  /*
   * public BigNum(String val) { this(val, 10); }
   */

  /**
   * This internal constructor differs from its public cousin with the arguments
   * reversed in two ways: it assumes that its arguments are correct, and it
   * doesn't copy the magnitude array.
   */
  BigInt(int[] magnitude, int signum) {
    this.signum = (magnitude.length == 0 ? 0 : signum);
    this.mag = magnitude;
  }

  // Static Factory Methods

  /**
   * Returns a BigInteger whose value is equal to that of the specified
   * {@code long}. This "static factory method" is provided in preference to a (
   * {@code long}) constructor because it allows for reuse of frequently used
   * BigIntegers.
   * 
   * @param val
   *          value of the BigInteger to return.
   * @return a BigInteger with the specified value.
   */
  public static BigInt valueOf(long val) {
    if (val == 0)
      return ZERO;
    return new BigInt(val);
  }

  /**
   * Constructs a BigInteger with the specified value, which may not be zero.
   */
  private BigInt(long val) {
    if (val < 0) {
      val = -val;
      signum = -1;
    } else {
      signum = 1;
    }

    int highWord = (int) (val >>> 32);
    if (highWord == 0) {
      mag = new int[1];
      mag[0] = (int) val;
    } else {
      mag = new int[2];
      mag[0] = highWord;
      mag[1] = (int) val;
    }
  }
  
  public static BigInt valueOf(int val) {
    if (val == 0)
      return ZERO;
    return new BigInt(val);
  }

  /**
   * Constructs a BigInteger with the specified value, which may not be zero.
   */
  private BigInt(int val) {
    if (val < 0) {
      val = -val;
      signum = -1;
    } else {
      signum = 1;
    }
    mag = new int[] {val};
  }

  /**
   * Returns a BigInteger with the given two's complement representation.
   * Assumes that the input array will not be modified (the returned BigInteger
   * will reference the input array if feasible).
   */
  private static BigInt valueOf(int val[]) {
    return (val[0] > 0 ? new BigInt(val, 1) : new BigInt(val));
  }
  
  /**
   * Null value.
   */
  private BigInt() {
    signum = 1;
    mag = null;
  }

  /**
   * The BigInteger constant zero.
   */
  public static final BigInt ZERO = new BigInt(new int[0], 0);

  
  /**
   * The null value, packed in a BigNum.
   * This value should be never used in calculation, it's only for test.
   */
  public static final BigInt NULL = new BigInt();

  // Arithmetic Operations

  /**
   * Returns a BigInteger whose value is {@code (this + val)}.
   * 
   * @param val
   *          value to be added to this BigInteger.
   * @return {@code this + val}
   */
  public BigInt add(BigInt val) {
    if (mag == null || val.mag == null) {
      throw new NullPointerException();
    }
    if (val.signum == 0)
      return this;
    if (signum == 0)
      return val;

    if (val.signum == signum)
      return new BigInt(add(mag, val.mag), signum);

    int cmp = compareMagnitude(val);
    if (cmp == 0)
      return ZERO;
    int[] resultMag = (cmp > 0 ? subtract(mag, val.mag)
        : subtract(val.mag, mag));
    resultMag = trustedStripLeadingZeroInts(resultMag);

    return new BigInt(resultMag, cmp == signum ? 1 : -1);
  }

  /**
   * Adds the contents of the int arrays x and y. This method allocates a new
   * int array to hold the answer and returns a reference to that array.
   */
  private static int[] add(int[] x, int[] y) {
    // If x is shorter, swap the two arrays
    if (x.length < y.length) {
      int[] tmp = x;
      x = y;
      y = tmp;
    }

    int xIndex = x.length;
    int yIndex = y.length;
    int result[] = new int[xIndex];
    long sum = 0;
    if (yIndex == 1) {
      sum = (x[--xIndex] & LONG_MASK) + (y[0] & LONG_MASK);
      result[xIndex] = (int) sum;
    } else {
      // Add common parts of both numbers
      while (yIndex > 0) {
        sum = (x[--xIndex] & LONG_MASK) + (y[--yIndex] & LONG_MASK)
            + (sum >>> 32);
        result[xIndex] = (int) sum;
      }
    }
    // Copy remainder of longer number while carry propagation is required
    boolean carry = (sum >>> 32 != 0);
    while (xIndex > 0 && carry)
      carry = ((result[--xIndex] = x[xIndex] + 1) == 0);

    // Copy remainder of longer number
    while (xIndex > 0)
      result[--xIndex] = x[xIndex];

    // Grow result if necessary
    if (carry) {
      int bigger[] = new int[result.length + 1];
      System.arraycopy(result, 0, bigger, 1, result.length);
      bigger[0] = 0x01;
      return bigger;
    }
    return result;
  }

  /**
   * Returns a BigInteger whose value is {@code (this - val)}.
   * 
   * @param val
   *          value to be subtracted from this BigInteger.
   * @return {@code this - val}
   */
  public BigInt subtract(BigInt val) {
    if (mag == null || val.mag == null) {
      throw new NullPointerException();
    }
    if (val.signum == 0)
      return this;
    if (signum == 0)
      return val.negate();
    if (val.signum != signum)
      return new BigInt(add(mag, val.mag), signum);

    int cmp = compareMagnitude(val);
    if (cmp == 0)
      return ZERO;
    int[] resultMag = (cmp > 0 ? subtract(mag, val.mag)
        : subtract(val.mag, mag));
    resultMag = trustedStripLeadingZeroInts(resultMag);
    return new BigInt(resultMag, cmp == signum ? 1 : -1);
  }

  /**
   * Subtracts the contents of the second int arrays (little) from the first
   * (big). The first int array (big) must represent a larger number than the
   * second. This method allocates the space necessary to hold the answer.
   */
  private static int[] subtract(int[] big, int[] little) {
    int bigIndex = big.length;
    int result[] = new int[bigIndex];
    int littleIndex = little.length;
    long difference = 0;

    // Subtract common parts of both numbers
    while (littleIndex > 0) {
      difference = (big[--bigIndex] & LONG_MASK)
          - (little[--littleIndex] & LONG_MASK) + (difference >> 32);
      result[bigIndex] = (int) difference;
    }

    // Subtract remainder of longer number while borrow propagates
    boolean borrow = (difference >> 32 != 0);
    while (bigIndex > 0 && borrow)
      borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1);

    // Copy remainder of longer number
    while (bigIndex > 0)
      result[--bigIndex] = big[bigIndex];

    return result;
  }

  /**
   * Returns a BigInteger whose value is {@code (this * val)}.
   * 
   * @param val
   *          value to be multiplied by this BigInteger.
   * @return {@code this * val}
   */
  public BigInt multiply(BigInt val) {
    if (mag == null || val.mag == null) {
      throw new NullPointerException();
    }
    if (val.signum == 0 || signum == 0)
      return ZERO;
    int resultSign = signum == val.signum ? 1 : -1;
    if (val.mag.length == 1) {
      return multiplyByInt(mag, val.mag[0], resultSign);
    }
    if (mag.length == 1) {
      return multiplyByInt(val.mag, mag[0], resultSign);
    }
    int[] result = multiplyToLen(mag, mag.length, val.mag, val.mag.length, null);
    result = trustedStripLeadingZeroInts(result);
    return new BigInt(result, resultSign);
  }

  private static BigInt multiplyByInt(int[] x, int y, int sign) {
    if (Integer.bitCount(y) == 1) {
      return new BigInt(shiftLeft(x, Integer.numberOfTrailingZeros(y)), sign);
    }
    int xlen = x.length;
    int[] rmag = new int[xlen + 1];
    long carry = 0;
    long yl = y & LONG_MASK;
    int rstart = rmag.length - 1;
    for (int i = xlen - 1; i >= 0; i--) {
      long product = (x[i] & LONG_MASK) * yl + carry;
      rmag[rstart--] = (int) product;
      carry = product >>> 32;
    }
    if (carry == 0L) {
      rmag = java.util.Arrays.copyOfRange(rmag, 1, rmag.length);
    } else {
      rmag[rstart] = (int) carry;
    }
    return new BigInt(rmag, sign);
  }

  /**
   * Multiplies int arrays x and y to the specified lengths and places the
   * result into z. There will be no leading zeros in the resultant array.
   */
  private static int[] multiplyToLen(int[] x, int xlen, int[] y, int ylen,
      int[] z) {
    int xstart = xlen - 1;
    int ystart = ylen - 1;

    if (z == null || z.length < (xlen + ylen))
      z = new int[xlen + ylen];

    long carry = 0;
    for (int j = ystart, k = ystart + 1 + xstart; j >= 0; j--, k--) {
      long product = (y[j] & LONG_MASK) * (x[xstart] & LONG_MASK) + carry;
      z[k] = (int) product;
      carry = product >>> 32;
    }
    z[xstart] = (int) carry;

    for (int i = xstart - 1; i >= 0; i--) {
      carry = 0;
      for (int j = ystart, k = ystart + 1 + i; j >= 0; j--, k--) {
        long product = (y[j] & LONG_MASK) * (x[i] & LONG_MASK)
            + (z[k] & LONG_MASK) + carry;
        z[k] = (int) product;
        carry = product >>> 32;
      }
      z[i] = (int) carry;
    }
    return z;
  }

  /**
   * Returns a BigInteger whose value is {@code (this / val)}.
   * 
   * @param val
   *          value by which this BigInteger is to be divided.
   * @return {@code this / val}
   * @throws ArithmeticException
   *           if {@code val} is zero.
   */
  public BigInt divide(BigInt val) {
    if (mag == null || val.mag == null) {
      throw new NullPointerException();
    }
    MutableBigInt q = new MutableBigInt(), a = new MutableBigInt(this.mag), b = new MutableBigInt(
        val.mag);

    a.divide(b, q, false);
    return q.toBigInteger(this.signum * val.signum);
  }

  /**
   * Returns an array of two BigIntegers containing {@code (this / val)}
   * followed by {@code (this % val)}.
   * 
   * @param val
   *          value by which this BigInteger is to be divided, and the remainder
   *          computed.
   * @return an array of two BigIntegers: the quotient {@code (this / val)} is
   *         the initial element, and the remainder {@code (this % val)} is the
   *         final element.
   * @throws ArithmeticException
   *           if {@code val} is zero.
   */
  /*
   * public BigNum[] divideAndRemainder(BigNum val) { BigNum[] result = new
   * BigNum[2]; MutableBigNum q = new MutableBigNum(), a = new
   * MutableBigNum(this.mag), b = new MutableBigNum(val.mag); MutableBigNum r =
   * a.divide(b, q); result[0] = q.toBigInteger(this.signum == val.signum ? 1 :
   * -1); result[1] = r.toBigInteger(this.signum); return result; }
   */

  /**
   * Returns a BigInteger whose value is {@code (this % val)}.
   * 
   * @param val
   *          value by which this BigInteger is to be divided, and the remainder
   *          computed.
   * @return {@code this % val}
   * @throws ArithmeticException
   *           if {@code val} is zero.
   */
  public BigInt remainder(BigInt val) {
    if (mag == null || val.mag == null) {
      throw new NullPointerException();
    }
    MutableBigInt q = new MutableBigInt(), a = new MutableBigInt(this.mag), b = new MutableBigInt(
        val.mag);

    return a.divide(b, q).toBigInteger(this.signum);
  }

  /**
   * Package private method to return bit length for an integer.
   */
  static int bitLengthForInt(int n) {
    return 32 - Integer.numberOfLeadingZeros(n);
  }

  /**
   * Returns a BigInteger whose value is the absolute value of this BigInteger.
   * 
   * @return {@code abs(this)}
   */
  public BigInt abs() {
    if (mag == null) {
      throw new NullPointerException();
    }
    return (signum >= 0 ? this : this.negate());
  }

  /**
   * Returns a BigInteger whose value is {@code (-this)}.
   * 
   * @return {@code -this}
   */
  public BigInt negate() {
    return new BigInt(this.mag, -this.signum);
  }

  /**
   * Returns the signum function of this BigInteger.
   * 
   * @return -1, 0 or 1 as the value of this BigInteger is negative, zero or
   *         positive.
   */
  /*public int signum() {
    return this.signum;
  }*/

  // Modular Arithmetic Operations

  /**
   * Returns a BigInteger whose value is {@code (this mod m}). This method
   * differs from {@code remainder} in that it always returns a
   * <i>non-negative</i> BigInteger.
   * 
   * @param m
   *          the modulus.
   * @return {@code this mod m}
   * @throws ArithmeticException
   *           {@code m} &le; 0
   * @see #remainder
   */
  public BigInt mod(BigInt m) {
    if (mag == null || m.mag == null) {
      throw new NullPointerException();
    }
    if (m.signum <= 0)
      throw new ArithmeticException("BigInteger: modulus not positive");

    BigInt result = this.remainder(m);
    return (result.signum >= 0 ? result : result.add(m));
  }

  // Shift Operations

  /**
   * Returns a BigInteger whose value is {@code (this << n)}. The shift
   * distance, {@code n}, may be negative, in which case this method performs a
   * right shift. (Computes <tt>floor(this * 2<sup>n</sup>)</tt>.)
   * 
   * @param n
   *          shift distance, in bits.
   * @return {@code this << n}
   * @throws ArithmeticException
   *           if the shift distance is {@code Integer.MIN_VALUE}.
   * @see #shiftRight
   */
  public BigInt shiftLeft(int n) {
    if (mag == null) {
      throw new ArithmeticException(
          "shifting a double is not supported");
    }
    if (signum == 0)
      return ZERO;
    if (n == 0)
      return this;
    if (n < 0) {
      if (n == Integer.MIN_VALUE) {
        throw new ArithmeticException(
            "Shift distance of Integer.MIN_VALUE not supported.");
      }
      return shiftRight(-n);
    }
    int[] newMag = shiftLeft(mag, n);

    return new BigInt(newMag, signum);
  }

  private static int[] shiftLeft(int[] mag, int n) {
    int nInts = n >>> 5;
    int nBits = n & 0x1f;
    int magLen = mag.length;
    int newMag[] = null;

    if (nBits == 0) {
      newMag = new int[magLen + nInts];
      System.arraycopy(mag, 0, newMag, 0, magLen);
    } else {
      int i = 0;
      int nBits2 = 32 - nBits;
      int highBits = mag[0] >>> nBits2;
      if (highBits != 0) {
        newMag = new int[magLen + nInts + 1];
        newMag[i++] = highBits;
      } else {
        newMag = new int[magLen + nInts];
      }
      int j = 0;
      while (j < magLen - 1)
        newMag[i++] = mag[j++] << nBits | mag[j] >>> nBits2;
      newMag[i] = mag[j] << nBits;
    }
    return newMag;
  }

  /**
   * Returns a BigInteger whose value is {@code (this >> n)}. Sign extension is
   * performed. The shift distance, {@code n}, may be negative, in which case
   * this method performs a left shift. (Computes
   * <tt>floor(this / 2<sup>n</sup>)</tt>.)
   * 
   * @param n
   *          shift distance, in bits.
   * @return {@code this >> n}
   * @throws ArithmeticException
   *           if the shift distance is {@code Integer.MIN_VALUE}.
   * @see #shiftLeft
   */
  public BigInt shiftRight(int n) {
    if (mag == null) {
      throw new ArithmeticException(
          "shifting a double is not supported");
    }
    if (n == 0)
      return this;
    if (n < 0) {
      if (n == Integer.MIN_VALUE) {
        throw new ArithmeticException(
            "Shift distance of Integer.MIN_VALUE not supported.");
      }
      return shiftLeft(-n);
    }

    int nInts = n >>> 5;
    int nBits = n & 0x1f;
    int magLen = mag.length;
    int newMag[] = null;

    // Special case: entire contents shifted off the end
    if (nInts >= magLen)
      return (signum >= 0 ? ZERO : new BigInt(-1));

    if (nBits == 0) {
      int newMagLen = magLen - nInts;
      newMag = Arrays.copyOf(mag, newMagLen);
    } else {
      int i = 0;
      int highBits = mag[0] >>> nBits;
      if (highBits != 0) {
        newMag = new int[magLen - nInts];
        newMag[i++] = highBits;
      } else {
        newMag = new int[magLen - nInts - 1];
      }

      int nBits2 = 32 - nBits;
      int j = 0;
      while (j < magLen - nInts - 1)
        newMag[i++] = (mag[j++] << nBits2) | (mag[j] >>> nBits);
    }

    if (signum < 0) {
      // Find out whether any one-bits were shifted off the end.
      boolean onesLost = false;
      for (int i = magLen - 1, j = magLen - nInts; i >= j && !onesLost; i--)
        onesLost = (mag[i] != 0);
      if (!onesLost && nBits != 0)
        onesLost = (mag[magLen - nInts - 1] << (32 - nBits) != 0);

      if (onesLost)
        newMag = javaIncrement(newMag);
    }

    return new BigInt(newMag, signum);
  }

  int[] javaIncrement(int[] val) {
    int lastSum = 0;
    for (int i = val.length - 1; i >= 0 && lastSum == 0; i--)
      lastSum = (val[i] += 1);
    if (lastSum == 0) {
      val = new int[val.length + 1];
      val[0] = 1;
    }
    return val;
  }

  // Bitwise Operations

  /**
   * Returns a BigInteger whose value is {@code (this & val)}. (This method
   * returns a negative BigInteger if and only if this and val are both
   * negative.)
   * 
   * @param val
   *          value to be AND'ed with this BigInteger.
   * @return {@code this & val}
   */
  public BigInt and(BigInt val) {
    if (mag == null) {
      throw new ArithmeticException("anding a double is not supported");
    }
    int[] result = new int[Math.max(intLength(), val.intLength())];
    for (int i = 0; i < result.length; i++)
      result[i] = (getInt(result.length - i - 1) & val.getInt(result.length - i
          - 1));

    return valueOf(result);
  }

  /**
   * Returns a BigInteger whose value is {@code (this | val)}. (This method
   * returns a negative BigInteger if and only if either this or val is
   * negative.)
   * 
   * @param val
   *          value to be OR'ed with this BigInteger.
   * @return {@code this | val}
   */
  public BigInt or(BigInt val) {
    if (mag == null) {
      throw new ArithmeticException("oring a double is not supported");
    }
    int[] result = new int[Math.max(intLength(), val.intLength())];
    for (int i = 0; i < result.length; i++)
      result[i] = (getInt(result.length - i - 1) | val.getInt(result.length - i
          - 1));

    return valueOf(result);
  }

  /**
   * Returns a BigInteger whose value is {@code (this ^ val)}. (This method
   * returns a negative BigInteger if and only if exactly one of this and val
   * are negative.)
   * 
   * @param val
   *          value to be XOR'ed with this BigInteger.
   * @return {@code this ^ val}
   */
  public BigInt xor(BigInt val) {
    if (mag == null) {
      throw new ArithmeticException("xoring a double is not supported");
    }
    int[] result = new int[Math.max(intLength(), val.intLength())];
    for (int i = 0; i < result.length; i++)
      result[i] = (getInt(result.length - i - 1) ^ val.getInt(result.length - i
          - 1));

    return valueOf(result);
  }

  /**
   * Returns a BigInteger whose value is {@code (~this)}. (This method returns a
   * negative value if and only if this BigInteger is non-negative.)
   * 
   * @return {@code ~this}
   */
  public BigInt not() {
    if (mag == null) {
      throw new ArithmeticException("operator ~ is not supported on a double");
    }
    int[] result = new int[intLength()];
    for (int i = 0; i < result.length; i++)
      result[i] = ~getInt(result.length - i - 1);

    return valueOf(result);
  }

  /**
   * Returns a BigInteger whose value is {@code (this & ~val)}. This method,
   * which is equivalent to {@code and(val.not())}, is provided as a convenience
   * for masking operations. (This method returns a negative BigInteger if and
   * only if {@code this} is negative and {@code val} is positive.)
   * 
   * @param val
   *          value to be complemented and AND'ed with this BigInteger.
   * @return {@code this & ~val}
   */
  /*public BigNum andNot(BigNum val) {
    int[] result = new int[Math.max(intLength(), val.intLength())];
    for (int i = 0; i < result.length; i++)
      result[i] = (getInt(result.length - i - 1) & ~val.getInt(result.length
          - i - 1));

    return valueOf(result);
  }*/

  // Single Bit Operations

  /**
   * Returns {@code true} if and only if the designated bit is set. (Computes
   * {@code ((this & (1<<n)) != 0)}.)
   * 
   * @param n
   *          index of bit to test.
   * @return {@code true} if and only if the designated bit is set.
   * @throws ArithmeticException
   *           {@code n} is negative.
   */
  /*
   * public boolean testBit(int n) { if (n<0) throw new
   * ArithmeticException("Negative bit address");
   * 
   * return (getInt(n >>> 5) & (1 << (n & 31))) != 0; }
   */

  /**
   * Returns a BigInteger whose value is equivalent to this BigInteger with the
   * designated bit set. (Computes {@code (this | (1<<n))}.)
   * 
   * @param n
   *          index of bit to set.
   * @return {@code this | (1<<n)}
   * @throws ArithmeticException
   *           {@code n} is negative.
   */
  /*
   * public BigInteger setBit(int n) { if (n<0) throw new
   * ArithmeticException("Negative bit address");
   * 
   * int intNum = n >>> 5; int[] result = new int[Math.max(intLength(),
   * intNum+2)];
   * 
   * for (int i=0; i<result.length; i++) result[result.length-i-1] = getInt(i);
   * 
   * result[result.length-intNum-1] |= (1 << (n & 31));
   * 
   * return valueOf(result); }
   */

  /**
   * Returns a BigInteger whose value is equivalent to this BigInteger with the
   * designated bit cleared. (Computes {@code (this & ~(1<<n))}.)
   * 
   * @param n
   *          index of bit to clear.
   * @return {@code this & ~(1<<n)}
   * @throws ArithmeticException
   *           {@code n} is negative.
   */
  /*
   * public BigInteger clearBit(int n) { if (n<0) throw new
   * ArithmeticException("Negative bit address");
   * 
   * int intNum = n >>> 5; int[] result = new int[Math.max(intLength(), ((n + 1)
   * >>> 5) + 1)];
   * 
   * for (int i=0; i<result.length; i++) result[result.length-i-1] = getInt(i);
   * 
   * result[result.length-intNum-1] &= ~(1 << (n & 31));
   * 
   * return valueOf(result); }
   */

  /**
   * Returns a BigInteger whose value is equivalent to this BigInteger with the
   * designated bit flipped. (Computes {@code (this ^ (1<<n))}.)
   * 
   * @param n
   *          index of bit to flip.
   * @return {@code this ^ (1<<n)}
   * @throws ArithmeticException
   *           {@code n} is negative.
   */
  /*
   * public BigInteger flipBit(int n) { if (n<0) throw new
   * ArithmeticException("Negative bit address");
   * 
   * int intNum = n >>> 5; int[] result = new int[Math.max(intLength(),
   * intNum+2)];
   * 
   * for (int i=0; i<result.length; i++) result[result.length-i-1] = getInt(i);
   * 
   * result[result.length-intNum-1] ^= (1 << (n & 31));
   * 
   * return valueOf(result); }
   */

  // Miscellaneous Bit Operations

  /**
   * Returns the number of bits in the minimal two's-complement representation
   * of this BigInteger, <i>excluding</i> a sign bit. For positive BigIntegers,
   * this is equivalent to the number of bits in the ordinary binary
   * representation. (Computes {@code (ceil(log2(this < 0 ? -this : this+1)))}.)
   * 
   * @return number of bits in the minimal two's-complement representation of
   *         this BigInteger, <i>excluding</i> a sign bit.
   */
  private int bitLength() {
    int[] m = mag;
    int len = m.length;
    if (len == 0) {
      return 1; // offset by one to initialize
    }
    // Calculate the bit length of the magnitude
    int magBitLength = ((len - 1) << 5) + bitLengthForInt(mag[0]);
    if (signum < 0) {
      // Check if magnitude is a power of two
      boolean pow2 = Integer.bitCount(mag[0]) == 1;
      for (int i = 1; i < len && pow2; i++)
        pow2 = (mag[i] == 0);

      return pow2 ? magBitLength : 1 + magBitLength;
    }
    return 1 + magBitLength;
  }

  // Comparison Operations

  /**
   * Compares this BigInteger with the specified BigInteger. This method is
   * provided in preference to individual methods for each of the six boolean
   * comparison operators ({@literal <}, ==, {@literal >}, {@literal >=}, !=,
   * {@literal <=}). The suggested idiom for performing these comparisons is:
   * {@code (x.compareTo(y)} &lt;<i>op</i>&gt; {@code 0)}, where
   * &lt;<i>op</i>&gt; is one of the six comparison operators.
   * 
   * @param val
   *          BigInteger to which this BigInteger is to be compared.
   * @return -1, 0 or 1 as this BigInteger is numerically less than, equal to,
   *         or greater than {@code val}.
   */
  @Override
  public int compareTo(BigInt val) {
    if (mag == null || val.mag == null) {
      return Double.compare(doubleValue(), val.doubleValue());
    }
    if (signum == val.signum) {
      switch (signum) {
      case 1:
        return compareMagnitude(val);
      case -1:
        return val.compareMagnitude(this);
      default:
        return 0;
      }
    }
    return signum > val.signum ? 1 : -1;
  }

  /**
   * Compares the magnitude array of this BigInteger with the specified
   * BigInteger's. This is the version of compareTo ignoring sign.
   * 
   * @param val
   *          BigInteger whose magnitude array to be compared.
   * @return -1, 0 or 1 as this magnitude array is less than, equal to or
   *         greater than the magnitude aray for the specified BigInteger's.
   */
  final int compareMagnitude(BigInt val) {
    int[] m1 = mag;
    int len1 = m1.length;
    int[] m2 = val.mag;
    int len2 = m2.length;
    if (len1 < len2)
      return -1;
    if (len1 > len2)
      return 1;
    for (int i = 0; i < len1; i++) {
      int a = m1[i];
      int b = m2[i];
      if (a != b)
        return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
    }
    return 0;
  }

  /**
   * Version of compareMagnitude that compares magnitude with long value. val
   * can't be Long.MIN_VALUE.
   */
  final int compareMagnitude(long val) {
    assert val != Long.MIN_VALUE;
    int[] m1 = mag;
    int len = m1.length;
    if (len > 2) {
      return 1;
    }
    if (val < 0) {
      val = -val;
    }
    int highWord = (int) (val >>> 32);
    if (highWord == 0) {
      if (len < 1)
        return -1;
      if (len > 1)
        return 1;
      int a = m1[0];
      int b = (int) val;
      if (a != b) {
        return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
      }
      return 0;
    }
    if (len < 2)
      return -1;
    int a = m1[0];
    int b = highWord;
    if (a != b) {
      return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
    }
    a = m1[1];
    b = (int) val;
    if (a != b) {
      return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
    }
    return 0;
  }

  /**
   * Compares this BigInteger with the specified Object for equality.
   * 
   * @param x
   *          Object to which this BigInteger is to be compared.
   * @return {@code true} if and only if the specified Object is a BigInteger
   *         whose value is numerically equal to this BigInteger.
   */
  @Override
  public boolean equals(Object x) {
    // This test is just an optimization, which may or may not help
    if (x == this)
      return true;

    if (!(x instanceof BigInt))
      return false;

    BigInt bigNum = (BigInt) x;
    
    int[] m = mag;
    int[] bm = bigNum.mag;
    if (m == bm)
      return true;
   
    if (bigNum.signum != signum)
      return false;

    
    int len = m.length;
    if (len != bm.length)
      return false;

    for (int i = 0; i < len; i++)
      if (bm[i] != m[i])
        return false;

    return true;
  }

  /**
   * Returns the minimum of this BigInteger and {@code val}.
   * 
   * @param val
   *          value with which the minimum is to be computed.
   * @return the BigInteger whose value is the lesser of this BigInteger and
   *         {@code val}. If they are equal, either may be returned.
   */
  public BigInt min(BigInt val) {
    return (compareTo(val) < 0 ? this : val);
  }

  /**
   * Returns the maximum of this BigInteger and {@code val}.
   * 
   * @param val
   *          value with which the maximum is to be computed.
   * @return the BigInteger whose value is the greater of this and {@code val}.
   *         If they are equal, either may be returned.
   */
  public BigInt max(BigInt val) {
    return (compareTo(val) > 0 ? this : val);
  }

  // Hash Function

  /**
   * Returns the hash code for this BigInteger.
   * 
   * @return hash code for this BigInteger.
   */
  @Override
  public int hashCode() {
    int[] mag = this.mag;
    int hashCode = 0;
    for (int i = 0; i < mag.length; i++)
      hashCode = (int) (31 * hashCode + (mag[i] & LONG_MASK));
    return hashCode * signum;
  }

  /**
   * Returns the String representation of this BigInteger in the given radix. If
   * the radix is outside the range from {@link Character#MIN_RADIX} to
   * {@link Character#MAX_RADIX} inclusive, it will default to 10 (as is the
   * case for {@code Integer.toString}). The digit-to-character mapping provided
   * by {@code Character.forDigit} is used, and a minus sign is prepended if
   * appropriate. (This representation is compatible with the
   * {@link #BigInt(String, int) (String, int)} constructor.)
   * 
   * @param radix
   *          radix of the String representation.
   * @return String representation of this BigInteger in the given radix.
   * @see Integer#toString
   * @see Character#forDigit
   * @see #BigInt(java.lang.String, int)
   */
  private String toString(int radix) {
    if (signum == 0)
      return "0";
    if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
      radix = 10;

    // Compute upper bound on number of digit groups and allocate space
    int maxNumDigitGroups = (4 * mag.length + 6) / 7;
    String digitGroup[] = new String[maxNumDigitGroups];

    // Translate number to string, a digit group at a time
    BigInt tmp = this.abs();
    int numGroups = 0;
    while (tmp.signum != 0) {
      BigInt d = longRadix[radix];

      MutableBigInt q = new MutableBigInt(), a = new MutableBigInt(tmp.mag), b = new MutableBigInt(
          d.mag);
      MutableBigInt r = a.divide(b, q);
      BigInt q2 = q.toBigInteger(tmp.signum * d.signum);
      BigInt r2 = r.toBigInteger(tmp.signum * d.signum);

      digitGroup[numGroups++] = Long.toString(r2.longValue(), radix);
      tmp = q2;
    }

    // Put sign (if any) and first digit group into result buffer
    StringBuilder buf = new StringBuilder(numGroups * digitsPerLong[radix] + 1);
    if (signum < 0)
      buf.append('-');
    buf.append(digitGroup[numGroups - 1]);

    // Append remaining digit groups padded with leading zeros
    for (int i = numGroups - 2; i >= 0; i--) {
      // Prepend (any) leading zeros for this digit group
      int numLeadingZeros = digitsPerLong[radix] - digitGroup[i].length();
      if (numLeadingZeros != 0)
        buf.append(zeros[numLeadingZeros]);
      buf.append(digitGroup[i]);
    }
    return buf.toString();
  }

  /* zero[i] is a string of i consecutive zeros. */
  private static final String zeros[] = new String[64];
  static {
    zeros[63] = "000000000000000000000000000000000000000000000000000000000000000";
    for (int i = 0; i < 63; i++)
      zeros[i] = zeros[63].substring(0, i);
  }

  /**
   * Returns the decimal String representation of this BigInteger. The
   * digit-to-character mapping provided by {@code Character.forDigit} is used,
   * and a minus sign is prepended if appropriate. (This representation is
   * compatible with the {@link #BigInteger(String) (String)} constructor, and
   * allows for String concatenation with Java's + operator.)
   * 
   * @return decimal String representation of this BigInteger.
   * @see Character#forDigit
   * @see #BigInteger(java.lang.String)
   */
  @Override
  public String toString() {
    if (mag == null) {
      throw new NullPointerException();
    }
    return toString(10);
  }

  /**
   * Converts this BigInteger to an {@code int}. This conversion is analogous to
   * a <i>narrowing primitive conversion</i> from {@code long} to {@code int} as
   * defined in section 5.1.3 of <cite>The Java&trade; Language
   * Specification</cite>: if this BigInteger is too big to fit in an
   * {@code int}, only the low-order 32 bits are returned. Note that this
   * conversion can lose information about the overall magnitude of the
   * BigInteger value as well as return a result with the opposite sign.
   * 
   * @return this BigInteger converted to an {@code int}.
   * @see #intValueExact()
   */
  /*public int intValue() {
    return getInt(0);
  }*/

  /**
   * Converts this BigInteger to a {@code long}. This conversion is analogous to
   * a <i>narrowing primitive conversion</i> from {@code long} to {@code int} as
   * defined in section 5.1.3 of <cite>The Java&trade; Language
   * Specification</cite>: if this BigInteger is too big to fit in a
   * {@code long}, only the low-order 64 bits are returned. Note that this
   * conversion can lose information about the overall magnitude of the
   * BigInteger value as well as return a result with the opposite sign.
   * 
   * @return this BigInteger converted to a {@code long}.
   */
  private long longValue() {
    long result = 0;
    for (int i = 1; i >= 0; i--)
      result = (result << 32) + (getInt(i) & LONG_MASK);
    return result;
  }

  /**
   * Converts this BigInteger to a {@code float}. This conversion is similar to
   * the <i>narrowing primitive conversion</i> from {@code double} to
   * {@code float} as defined in section 5.1.3 of <cite>The Java&trade; Language
   * Specification</cite>: if this BigInteger has too great a magnitude to
   * represent as a {@code float}, it will be converted to
   * {@link Float#NEGATIVE_INFINITY} or {@link Float#POSITIVE_INFINITY} as
   * appropriate. Note that even when the return value is finite, this
   * conversion can lose information about the precision of the BigInteger
   * value.
   * 
   * @return this BigInteger converted to a {@code float}.
   */
  /*public float floatValue() {
    if (mag == null) {
      return (float) dvalue;
    }
    if (signum == 0) {
      return 0.0f;
    }

    // Somewhat inefficient, but guaranteed to work.
    return Float.parseFloat(this.toString());
  }*/

  /**
   * Converts this BigInteger to a {@code double}. This conversion is similar to
   * the <i>narrowing primitive conversion</i> from {@code double} to
   * {@code float} as defined in section 5.1.3 of <cite>The Java&trade; Language
   * Specification</cite>: if this BigInteger has too great a magnitude to
   * represent as a {@code double}, it will be converted to
   * {@link Double#NEGATIVE_INFINITY} or {@link Double#POSITIVE_INFINITY} as
   * appropriate. Note that even when the return value is finite, this
   * conversion can lose information about the precision of the BigInteger
   * value.
   * 
   * @return this BigInteger converted to a {@code double}.
   */
  private double doubleValue() {
    if (mag == null) {
      throw new NullPointerException();
    }
    if (signum == 0) {
      return 0.0;
    }

    // Somewhat inefficient, but guaranteed to work.
    return Double.parseDouble(this.toString());
  }

  /**
   * Returns the input array stripped of any leading zero bytes. Since the
   * source is trusted the copying may be skipped.
   */
  private static int[] trustedStripLeadingZeroInts(int val[]) {
    int vlen = val.length;
    int keep;

    // Find first nonzero byte
    for (keep = 0; keep < vlen && val[keep] == 0; keep++)
      ;
    return keep == 0 ? val : java.util.Arrays.copyOfRange(val, keep, vlen);
  }

  /**
   * Takes an array a representing a negative 2's-complement number and returns
   * the minimal (no leading zero ints) unsigned whose value is -a.
   */
  private static int[] makePositive(int a[]) {
    int keep, j;

    // Find first non-sign (0xffffffff) int of input
    for (keep = 0; keep < a.length && a[keep] == -1; keep++)
      ;

    /*
     * Allocate output array. If all non-sign ints are 0x00, we must allocate
     * space for one extra output int.
     */
    for (j = keep; j < a.length && a[j] == 0; j++)
      ;
    int extraInt = (j == a.length ? 1 : 0);
    int result[] = new int[a.length - keep + extraInt];

    /*
     * Copy one's complement of input into output, leaving extra int (if it
     * exists) == 0x00
     */
    for (int i = keep; i < a.length; i++)
      result[i - keep + extraInt] = ~a[i];

    // Add one to one's complement to generate two's complement
    for (int i = result.length - 1; ++result[i] == 0; i--)
      ;

    return result;
  }

  /*
   * The following two arrays are used for fast String conversions. Both are
   * indexed by radix. The first is the number of digits of the given radix that
   * can fit in a Java long without "going negative", i.e., the highest integer
   * n such that radix**n < 2**63. The second is the "long radix" that tears
   * each number into "long digits", each of which consists of the number of
   * digits in the corresponding element in digitsPerLong (longRadix[i] =
   * i**digitPerLong[i]). Both arrays have nonsense values in their 0 and 1
   * elements, as radixes 0 and 1 are not used.
   */
  private static final int digitsPerLong[] = { 0, 0, 62, 39, 31, 27, 24, 22,
      20, 19, 18, 18, 17, 17, 16, 16, 15, 15, 15, 14, 14, 14, 14, 13, 13, 13,
      13, 13, 13, 12, 12, 12, 12, 12, 12, 12, 12 };

  private static final BigInt longRadix[] = { null, null,
      valueOf(0x4000000000000000L), valueOf(0x383d9170b85ff80bL),
      valueOf(0x4000000000000000L), valueOf(0x6765c793fa10079dL),
      valueOf(0x41c21cb8e1000000L), valueOf(0x3642798750226111L),
      valueOf(0x1000000000000000L), valueOf(0x12bf307ae81ffd59L),
      valueOf(0xde0b6b3a7640000L), valueOf(0x4d28cb56c33fa539L),
      valueOf(0x1eca170c00000000L), valueOf(0x780c7372621bd74dL),
      valueOf(0x1e39a5057d810000L), valueOf(0x5b27ac993df97701L),
      valueOf(0x1000000000000000L), valueOf(0x27b95e997e21d9f1L),
      valueOf(0x5da0e1e53c5c8000L), valueOf(0xb16a458ef403f19L),
      valueOf(0x16bcc41e90000000L), valueOf(0x2d04b7fdd9c0ef49L),
      valueOf(0x5658597bcaa24000L), valueOf(0x6feb266931a75b7L),
      valueOf(0xc29e98000000000L), valueOf(0x14adf4b7320334b9L),
      valueOf(0x226ed36478bfa000L), valueOf(0x383d9170b85ff80bL),
      valueOf(0x5a3c23e39c000000L), valueOf(0x4e900abb53e6b71L),
      valueOf(0x7600ec618141000L), valueOf(0xaee5720ee830681L),
      valueOf(0x1000000000000000L), valueOf(0x172588ad4f5f0981L),
      valueOf(0x211e44f7d02c1000L), valueOf(0x2ee56725f06e5c71L),
      valueOf(0x41c21cb8e1000000L) };

  /**
   * These routines provide access to the two's complement representation of
   * BigIntegers.
   */

  /**
   * Returns the length of the two's complement representation in ints,
   * including space for at least one sign bit.
   */
  private int intLength() {
    return (bitLength() >>> 5) + 1;
  }

  /* Returns an int of sign bits */
  private int signInt() {
    return signum < 0 ? -1 : 0;
  }

  /**
   * Returns the specified int of the little-endian two's complement
   * representation (int 0 is the least significant). The int number can be
   * arbitrarily high (values are logically preceded by infinitely many sign
   * ints).
   */
  private int getInt(int n) {
    if (n < 0)
      return 0;
    if (n >= mag.length)
      return signInt();

    int magInt = mag[mag.length - n - 1];

    return (signum >= 0 ? magInt : (n <= firstNonzeroIntNum() ? -magInt
        : ~magInt));
  }

  /**
   * Returns the index of the int that contains the first nonzero int in the
   * little-endian binary representation of the magnitude (int 0 is the least
   * significant). If the magnitude is zero, return value is undefined.
   */
  private int firstNonzeroIntNum() {
    // Search for the first nonzero int
    int i;
    int mlen = mag.length;
    for (i = mlen - 1; i >= 0 && mag[i] == 0; i--)
      ;
    return mlen - i - 1;
  }

}
