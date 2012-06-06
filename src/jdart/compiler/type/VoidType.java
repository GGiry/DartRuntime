package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.DYNAMIC_NON_NULL_TYPE;

public class VoidType implements Type {

  VoidType() {
    // enforce singleton
  }

  @Override
  public String toString() {
    return "void";
  }

  @Override
  public boolean isNullable() {
    throw new IllegalStateException("void type");
  }

  @Override
  public NullableType asNullable() {
    throw new IllegalStateException("void type");
  }

  @Override
  public NullableType asNonNull() {
    throw new IllegalStateException("void type");
  }

  @Override
  public Type asNullable(boolean nullable) {
    throw new IllegalStateException("void type");
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitVoidType(this, parameter);
  }

  @Override
  public Object asConstant() {
    throw new IllegalStateException("void type");
  }

  @Override
  public Type map(TypeMapper typeMapper) {
    Type resultType = typeMapper.transform(this);
    return (resultType == null)? DYNAMIC_NON_NULL_TYPE: resultType;
  }

  @Override
  public Type commonValuesWith(Type type) {
    throw new IllegalStateException("void type");
  }

  @Override
  public Type invert() {
    throw new IllegalStateException("void type");
  }

  @Override
  public Type lessThanOrEqualsValues(Type other, boolean inLoop) {
    return null;
  }

  @Override
  public Type lessThanValues(Type other, boolean inLoop) {
    return null;
  }

  @Override
  public Type exclude(Type other) {
    return null;
  }

  @Override
  public Type add(Type other) {
    return null;
  }

  @Override
  public Type mod(Type other) {
    return null;
  }

  @Override
  public Type sub(Type other) {
    return null;
  }
}
