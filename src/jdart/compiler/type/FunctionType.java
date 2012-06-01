package jdart.compiler.type;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.RandomAccess;

import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.MethodElement;

public class FunctionType extends OwnerType {
  private final Type returnType;
  private final List<Type> parameterTypes;
  private final Map<String, Type> namedParameterTypes;
  private final/* maybenull */MethodElement constant;
  private transient FunctionType dualType; // almost final see postInitDualType

  FunctionType(boolean nullable, Type returnType, List<Type> parameterTypes, Map<String, Type> namedParameterTypes, /* maybenull */MethodElement constant) {
    super(nullable);
    this.returnType = Objects.requireNonNull(returnType);
    assert parameterTypes instanceof RandomAccess;
    this.parameterTypes = Objects.requireNonNull(parameterTypes);
    this.namedParameterTypes = Objects.requireNonNull(namedParameterTypes);
    this.constant = constant;
  }

  void postInitDualType(FunctionType dualType) {
    this.dualType = Objects.requireNonNull(dualType);
  }

  @Override
  public int hashCode() {
    return (isNullable() ? 1 : 0) ^ returnType.hashCode() ^ Integer.rotateLeft(parameterTypes.hashCode(), 8)
        ^ Integer.rotateLeft(namedParameterTypes.hashCode(), 16) ^ Integer.rotateLeft(Objects.hashCode(constant), 24);
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
    return isNullable() == functionType.isNullable() && returnType.equals(functionType.returnType) && parameterTypes.equals(functionType.parameterTypes)
        && namedParameterTypes.equals(functionType.namedParameterTypes) && Objects.equals(constant, functionType.constant);
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
    for (Type type : parameterTypes) {
      builder.append(type).append(", ");
    }
    if (!namedParameterTypes.isEmpty()) {
      builder.append('[');
      for (Entry<String, Type> entry : namedParameterTypes.entrySet()) {
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
    builder.append(')').append(super.toString()).append(" -> ").append(returnType);
    return builder.toString();
  }

  @Override
  public InterfaceType getSuperType() {
    return null;
  }

  @Override
  public List<InterfaceType> getInterfaces() {
    return Collections.singletonList(CoreTypeRepository.getCoreTypeRepository().getFunctionType());
  }

  @Override
  public Element localLookupMember(String name) {
    return null;
  }

  @Override
  public FunctionType asNullable() {
    return (isNullable()) ? this : dualType;
  }

  @Override
  public FunctionType asNonNull() {
    return (!isNullable()) ? this : dualType;
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitFunctionType(this, parameter);
  }

  @Override
  public MethodElement asConstant() {
    return constant;
  }

  @Override
  public Type commonValuesWith(Type type) {
    if (type instanceof FunctionType) {
      return equals(type) ? this : null;
    }

    if (type instanceof UnionType) {
      return ((UnionType) type).commonValuesWith(this);
    }
    return null;
  }

  @Override
  public Type invert() {
    return null;
  }

  @Override
  public Type LTEValues(Type other) {
    return null;
  }

  @Override
  public Type LTValues(Type other) {
    return null;
  }
  
  @Override
  public Type exclude(Type other) {
    return null;
  }
}
