import jdart.runtime.BigInt;

public class BigFibo {
  private static final BigInt ONE = BigInt.valueOf(1);
  private static final BigInt TWO = BigInt.valueOf(2);
  
  private static BigInt fibo(BigInt n) {
    if (n.compareTo(TWO) < 0) {
      return ONE;
    }
    return fibo(n.subtract(ONE)).add(fibo(n.subtract(TWO)));
  }
  
  public static void main(String[] args) {
    System.out.println(fibo(BigInt.valueOf(40)));
  }
}
