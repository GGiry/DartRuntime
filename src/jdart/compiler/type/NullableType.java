package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.DYNAMIC_NON_NULL_TYPE;

abstract class NullableType implements Type {
  private final boolean isNullable;

  NullableType(boolean isNullable) {
    this.isNullable = isNullable;
  }

  @Override
  public boolean isNullable() {
    return isNullable;
  }

  @Override
  public String toString() {
    return (isNullable() ? "?" : "");
  }

  @Override
  public abstract NullableType asNullable();

  @Override
  public abstract NullableType asNonNull();

  @Override
  public Type asNullable(boolean nullable) {
    return (nullable)?asNullable(): asNonNull();
  }
  
  NullableType merge(NullableType type) {
    Object constant = asConstant();
    
    if (constant != null && constant.equals(type.asConstant())) {
      return (type.isNullable) ? asNullable() : this;
    }
    if (type instanceof UnionType) {
      return ((UnionType) type).merge(this);
    }
    return UnionType.createUnionType(this, type);
  }

  @Override
  public Type map(TypeMapper typeMapper) {
    Type resultType = typeMapper.transform(this);
    return (resultType == null)? DYNAMIC_NON_NULL_TYPE: resultType;
  }
}