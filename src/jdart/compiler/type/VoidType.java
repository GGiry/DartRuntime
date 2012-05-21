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
}
