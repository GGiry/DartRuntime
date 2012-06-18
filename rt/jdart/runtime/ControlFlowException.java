package jdart.runtime;

@SuppressWarnings("serial")
public class ControlFlowException extends RuntimeException {
  // this field is used by the runtime
  public BigInt value;

  ControlFlowException() {
    super(null, null, false, false);
  }

  public static ControlFlowException value(BigInt value) {
    ControlFlowException e = CACHE.get();
    e.value =  value;
    return e;
  }

  private static final ThreadLocal<ControlFlowException> CACHE =
      new ThreadLocal<ControlFlowException>() {
    @Override
    protected ControlFlowException initialValue() {
      return new ControlFlowException();
    }
  };
}