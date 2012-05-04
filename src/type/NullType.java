package type;

public class NullType implements Type {
  NullType() {
    // enforce singleton
  }
  
  @Override
  public String toString() {
    return "null";
  }
  
	@Override
	public boolean isNullable() {
		return true;
	}
	
	@Override
	public Type asNullable() {
	  return this;
	}
	
	@Override
	public Object asConstant() {
	  return NULL_VALUE;
	}
}
