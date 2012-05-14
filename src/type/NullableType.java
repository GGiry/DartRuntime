package type;

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

}