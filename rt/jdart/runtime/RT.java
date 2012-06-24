package jdart.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

/*
 * This class use methods java.Math.*exact from OpenJDK.
 * All introduced bugs are mine. Remi
 */

/**
 *  JDart runtime support class
 *
 */
public class RT {
  // load a BigInt as a constant from a String
  public CallSite ldcBSM(Lookup lookup, String name, MethodType methodType, String bigIntAsString) {
    return new ConstantCallSite(MethodHandles.constant(BigInt.class, BigInt.valueOf(bigIntAsString)));
  }
  
//load a Double as a constant from a double
 public CallSite ldcBSM(Lookup lookup, String name, MethodType methodType, double value) {
   return new ConstantCallSite(MethodHandles.constant(double.class, Double.valueOf(value)));
 }
  
  public static void throwArithmeticException() {
    throw new ArithmeticException();
  }
  
  public static BigInt addOverflowed(int r1, int r2) {
    long result = (long)r1 + (long)r2;
    return BigInt.valueOf(result);
  }

  public static BigInt addBig(int r1, BigInt _r1, int r2, BigInt _r2) {
    if (_r1 == null) {
      _r1 = BigInt.valueOf(r1);
    }
    if (_r2 == null) {
      _r2 = BigInt.valueOf(r2);
    }
    return _r1.add(_r2);
  }
  
  // version used for jdk7, jdk8 uses Math.addExact()
  public static int addExact(int r1, int r2) {
    int result = r1 + r2;
    // HD 2-12 Overflow iff both arguments have the opposite sign of the result
    if (((r1 ^ result) & (r2 ^ result)) < 0) {
        throw new ArithmeticException("integer overflow");
    }
    return result;
  }
}
