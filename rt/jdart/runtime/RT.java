package jdart.runtime;

import java.io.PrintStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
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
  public static CallSite ldcBSM(Lookup lookup, String name, MethodType methodType, String bigIntAsString) {
    return new ConstantCallSite(MethodHandles.constant(BigInt.class, BigInt.valueOf(bigIntAsString)));
  }
  
  public static CallSite operatorBSM(Lookup lookup, String name, MethodType methodType) {
    String methodName;
    switch(name) {
    case "ADD":
      methodName = "add";
      break;
    default:
      throw new BootstrapMethodError("operation "+name+" not implemented yet");
    }
    
    MethodHandle mh;
    try {
      mh = MethodHandles.lookup().findVirtual(BigInt.class, methodName, MethodType.methodType(BigInt.class, BigInt.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new BootstrapMethodError(e);
    }
    
    return new ConstantCallSite(mh);
  }
  
  public static CallSite operatorOverflowBSM(Lookup lookup, String name, MethodType methodType) {
    String methodName;
    switch(name) {
    case "ADD":
      methodName = "addOverflowed";
      break;
    default:
      throw new BootstrapMethodError("operation "+name+" not implemented yet");
    }
    
    MethodHandle mh;
    try {
      mh = MethodHandles.lookup().findStatic(RT.class, methodName, methodType);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new BootstrapMethodError(e);
    }
    
    return new ConstantCallSite(mh);
  }
  
  public static CallSite operatorBigBSM(Lookup lookup, String name, MethodType methodType) {
    String methodName;
    switch(name) {
    case "ADD":
      methodName = "addBig";
      break;
    default:
      throw new BootstrapMethodError("operation "+name+" not implemented yet");
    }
    
    MethodHandle mh;
    try {
      mh = MethodHandles.lookup().findStatic(RT.class, methodName, methodType);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new BootstrapMethodError(e);
    }
    
    return new ConstantCallSite(mh);
  }
  
  public static CallSite functionCallBSM(Lookup lookup, String name, MethodType methodType, Class<?> unitType) {
    //FIXME do callsite adaptations
    
    // temporary hack, trap print()
    if (name.equals("print") && unitType.getName().equals("core_runtime")) {
      MethodType lookupMethodType = methodType;
      if (!methodType.parameterType(0).isPrimitive()) {
        lookupMethodType = methodType.changeParameterType(0, Object.class);
      }
      MethodHandle mh;
      try {
        mh = MethodHandles.lookup().findVirtual(PrintStream.class, "println", lookupMethodType);
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new BootstrapMethodError(e);
      }
      mh = mh.bindTo(System.out);
      return new ConstantCallSite(mh.asType(methodType));
    }
    
    // function call
    MethodHandle mh;
    try {
      mh = MethodHandles.lookup().findStatic(unitType, name, methodType);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new BootstrapMethodError(e);
    }
    
    return new ConstantCallSite(mh);
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
  
  public static BigInt shl(int r1, int r2) {
    BigInt _r1 = BigInt.valueOf(r1);
    return _r1.shiftLeft(r2);
  }
  
  public static int shlExact(int r1, int r2) {
    return r1 << r2;
  }
  
  public static void assignExact(int r1, int r2) {
    r1 = r2;
  }
}
