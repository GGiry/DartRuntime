package type;

abstract class AbstractType implements Type {
	private final boolean isNullable;

	public AbstractType(boolean isNullable) {
		this.isNullable = isNullable;
	}
	
	@Override
  public boolean isNullable() {
		return isNullable;
	}
	
	Type merge(AbstractType type) {
	  Object constant = asConstant();
    if (constant != null && constant.equals(type.asConstant())) {
      return this;
    }
    if (type instanceof UnionType) {
      return ((UnionType) type).merge(this);
    }
	  return UnionType.createUnionType(this, type);
	}
}
