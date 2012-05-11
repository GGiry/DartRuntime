package type;

import java.util.List;
import java.util.Map;

public class FunctionType implements Type {
  private final com.google.dart.compiler.type.Type returnType;
  private final List<com.google.dart.compiler.type.Type> parameterTypes;
  private final Map<String, com.google.dart.compiler.type.Type> namedParameterTypes;

  FunctionType(com.google.dart.compiler.type.Type returnType, List<com.google.dart.compiler.type.Type> parameterTypes,
      Map<String, com.google.dart.compiler.type.Type> namedParameterTypes) {
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
    this.namedParameterTypes = namedParameterTypes;
  }

  public com.google.dart.compiler.type.Type getReturnType() {
    return returnType;
  }

  public List<com.google.dart.compiler.type.Type> getParameterTypes() {
    return parameterTypes;
  }

  public Map<String, com.google.dart.compiler.type.Type> getNamedParameterTypes() {
    return namedParameterTypes;
  }

  @Override
  public boolean isNullable() {
    return false;
  }

  @Override
  public AbstractType asNullable() {
    // TODO
    return null;
  }

  @Override
  public AbstractType asNonNull() {
    // TODO
    return null;
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitFunctionType(this, parameter);
  }

  @Override
  public Object asConstant() {
    return null;
  }
}
