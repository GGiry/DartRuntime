package type;

public class TypeVisitor<R, P> {
  protected R visitType(Type type, P parameter) {
    throw new AssertionError("no visit defined for type " + type.getClass().getName());
  }
  
  public R visitDynamicType(DynamicType type, P parameter) {
    return visitType(type, parameter);
  }
  
  public R visitVoidType(VoidType type, P parameter) {
    return visitType(type, parameter);
  }
  
  public R visitNullType(NullType type, P parameter) {
    return visitType(type, parameter);
  }
  
  public R visitUnionType(UnionType type, P parameter) {
    return visitType(type, parameter);
  }
  
  protected R visitOwnerType(OwnerType type, P parameter) {
    return visitType(type, parameter);
  }

  public R visitBoolType(BoolType type, P parameter) {
    return visitOwnerType(type, parameter);
  }

  public R visitIntType(IntType type, P parameter) {
    return visitOwnerType(type, parameter);
  }

  public R visitDoubleType(DoubleType type, P parameter) {
    return visitOwnerType(type, parameter);
  }

  public R visitInterfaceType(InterfaceType type, P parameter) {
    return visitOwnerType(type, parameter);
  }
  
  public R visitFunctionType(FunctionType type, P parameter) {
    return visitOwnerType(type, parameter);
  }
}
