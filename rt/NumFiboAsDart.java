import java.math.BigInteger;


public class NumFiboAsDart {
  @SuppressWarnings("serial")
  static class ControlFlowException extends RuntimeException {
    BigInteger value;

    ControlFlowException() {
      super(null, null, false, false);
    }
    
    public static ControlFlowException value(BigInteger value) {
      ControlFlowException e = cache.get();
      e.value =  value;
      return e;
    }
    
    private static final ThreadLocal<ControlFlowException> cache =
        new ThreadLocal<ControlFlowException>() {
          @Override
          protected ControlFlowException initialValue() {
            return new ControlFlowException();
          }
        };
  }
  
  private static int fibo(double n) throws ControlFlowException {
    if (n < 2) {
      return 1;
    }
    int r1;
    BigInteger _r1;
    try {
      r1 = fibo(n -1);
      _r1 = null;
    } catch(ControlFlowException e) {
      r1 = 0;
      _r1 = e.value;
    }
    int r2;
    BigInteger _r2;
    try {
      r2 = fibo(n -2);
      _r2 = null;
    } catch(ControlFlowException e) {
      r2 = 0;
      _r2 = e.value;
    }
    int r3;
    BigInteger _r3;
    if (_r1 == null && _r2 == null) {
      try {
        r3 = /*Math.*/addExact(r1, r2);
        _r3 = null;
      } catch(ArithmeticException e) {
        _r3 = overflowedAdd(r1, r2);
        r3 = 0;
      }
    } else {
      _r3 = addBig(r1, _r1, r2, _r2);
      r3 = 0;
    }
    if (_r3 == null) {
      return r3;
    }
    throw ControlFlowException.value(_r3); 
  }
  
  private static BigInteger overflowedAdd(int r1, int r2) {
    return BigInteger.valueOf(r1).add(BigInteger.valueOf(r2));
  }

  private static BigInteger addBig(int r1, BigInteger _r1, int r2, BigInteger _r2) {
    if (_r1 == null) {
      _r1 = BigInteger.valueOf(r1);
    }
    if (_r2 == null) {
      _r2 = BigInteger.valueOf(r2);
    }
    return _r1.add(_r2);
  }
  
  // temporary hack, to compile with jdk7
  private static int addExact(int x, int y) {
    int r = x + y;
    // HD 2-12 Overflow iff both arguments have the opposite sign of the result
    if (((x ^ r) & (y ^ r)) < 0) {
        throw new ArithmeticException("integer overflow");
    }
    return r;
  }

  public static void main(String[] args) {
    int r1;
    BigInteger _r1;
    try {
      r1 = fibo(35.0);
      _r1 = null;
    } catch(ControlFlowException e) {
      _r1 = e.value;
      r1 = 0;
    }
    if (_r1 == null) {
      System.out.println(r1);
    } else {
      System.out.println(_r1);
    }
  }
}
