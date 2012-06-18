

public class NumBigFibo {
  private static int fibo(Double nBig) {
    double n = nBig;
    if (n < 2) {
      return 1;
    }
    return fibo(n - 1) + fibo(n - 2);
  }
  
  public static void main(String[] args) {
    System.out.println(fibo(35.0));
  }
}
