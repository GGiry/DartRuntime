package type;

public class TypeVisitor<R, P> {
  protected R visitType(Type type, P parameter) {
    throw new AssertionError("no visit defined for type " + type.getClass().getName());
  }

  public R visitNullType(NullType type, P parameter) {
    return visitType(type, parameter);
  }

  public R visitBoolType(BoolType type, P parameter) {
    return visitType(type, parameter);
  }

  public R visitIntType(IntType type, P parameter) {
    return visitType(type, parameter);
  }

  public R visitDoubleType(DoubleType type, P parameter) {
    return visitType(type, parameter);
  }

  public R visitInterfaceType(InterfaceType type, P parameter) {
    return visitType(type, parameter);
  }

  public R visitUnionType(UnionType type, P parameter) {
    return visitType(type, parameter);
  }
}
