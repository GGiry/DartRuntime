package type;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class FunctionType extends AbstractType {
  private final Type returnType;
  private final List<Type> parameterTypes;
  private final Map<String, Type> namedParameterTypes;

  FunctionType(boolean nullable, Type returnType, List<Type> parameterTypes, Map<String, Type> namedParameterTypes) {
    super(nullable);
    this.returnType = Objects.requireNonNull(returnType);
    this.parameterTypes = Objects.requireNonNull(parameterTypes);
    this.namedParameterTypes = Objects.requireNonNull(namedParameterTypes);
  }
  
  @Override
  public int hashCode() {
    return returnType.hashCode() ^
        Integer.rotateLeft(parameterTypes.hashCode(), 8) ^
        Integer.rotateLeft(namedParameterTypes.hashCode(), 24);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FunctionType)) {
      return false;
    }
    FunctionType functionType = (FunctionType) obj;
    return isNullable() == functionType.isNullable() &&
        returnType.equals(functionType.returnType) &&
        parameterTypes.equals(functionType.parameterTypes) &&
        namedParameterTypes.equals(functionType.namedParameterTypes);
  }

  public Type getReturnType() {
    return returnType;
  }

  public List<Type> getParameterTypes() {
    return parameterTypes;
  }

  public Map<String, Type> getNamedParameterTypes() {
    return namedParameterTypes;
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    for(Type type: parameterTypes) {
      builder.append(type).append(", ");
    }
    if (!namedParameterTypes.isEmpty()) {
      builder.append('[');
      for(Entry<String, Type> entry: namedParameterTypes.entrySet()) {
        builder.append(entry.getKey()).append(' ').append(entry.getValue()).append(", ");
      }
      if (!namedParameterTypes.isEmpty()) {
        builder.setLength(builder.length() - 2);
      }
      builder.append(']');
    } else {
      if (!parameterTypes.isEmpty()) {
        builder.setLength(builder.length() - 2);
      }
    }
    builder.append(") -> ").append(returnType);
    return builder.toString();
  }

  @Override
  public boolean isNullable() {
    throw new IllegalStateException("function type");
  }

  @Override
  public FunctionType asNullable() {
    throw new IllegalStateException("function type");
  }

  @Override
  public FunctionType asNonNull() {
    throw new IllegalStateException("function type");
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitFunctionType(this, parameter);
  }

  @Override
  public Object asConstant() {
    throw new IllegalStateException("function type");
  }
}
