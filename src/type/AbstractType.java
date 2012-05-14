package type;

abstract class AbstractType implements Type {
  private final boolean isNullable;

  AbstractType(boolean isNullable) {
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
  public abstract AbstractType asNullable();

  @Override
  public abstract AbstractType asNonNull();

  AbstractType merge(AbstractType type) {
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