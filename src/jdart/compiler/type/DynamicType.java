package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;

public class DynamicType extends NullableType {

  public DynamicType(boolean nullable) {
    super(nullable);
  }

  @Override
  public String toString() {
    return "dynamic" + super.toString();
  }

  @Override
  public NullableType asNullable() {
    if (isNullable()) {
      return this;
    }
    return DYNAMIC_TYPE;
  }

  @Override
  public NullableType asNonNull() {
    if (!isNullable()) {
      return this;
    }
    return DYNAMIC_NON_NULL_TYPE;
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitDynamicType(this, parameter);
  }

  @Override
  public Object asConstant() {
    return null;
  }

  @Override
  public Type commonValuesWith(Type type) {
    if (type instanceof DynamicType) {
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
}
