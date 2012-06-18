import jdart.runtime.BigInt;
import jdart.runtime.ControlFlowException;
import jdart.runtime.RT;

public class FiboAsDart {
  private static int fibo(int n) throws ControlFlowException {
    if (n < 2) {
      return 1;
    }
    int r1;
    BigInt _r1;
    try {
      r1 = fibo(n -1);
      _r1 = null;
    } catch(ControlFlowException e) {
      r1 = 0;
      _r1 = e.value;
    }
    int r2;
    BigInt _r2;
    try {
      r2 = fibo(n -2);
      _r2 = null;
    } catch(ControlFlowException e) {
      r2 = 0;
      _r2 = e.value;
    }
    int r3;
    BigInt _r3;
    if (_r1 == null && _r2 == null) {
      try {
        r3 = RT.addExact(r1, r2);
        _r3 = null;
      } catch(ArithmeticException e) {
        _r3 = RT.addOverflowed(r1, r2);
        r3 = 0;
      }
    } else {
      _r3 = RT.addBig(r1, _r1, r2, _r2);
      r3 = 0;
    }
    if (_r3 == null) {
      return r3;
    }
    throw ControlFlowException.value(_r3); 
  }

  public static void main(String[] args) {
    int r1;
    BigInt _r1;
    try {
      r1 = fibo(40);
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
