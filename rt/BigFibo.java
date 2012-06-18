import java.math.BigInteger;


public class BigFibo {
  private static final BigInteger TWO = BigInteger.valueOf(2);
  
  private static BigInteger fibo(BigInteger n) {
    if (n.compareTo(TWO) < 0) {
      return BigInteger.ONE;
    }
    return fibo(n.subtract(BigInteger.ONE)).add(fibo(n.subtract(TWO)));
  }
  
  public static void main(String[] args) {
    System.out.println(fibo(BigInteger.valueOf(40)));
  }
}
