package type;

import java.util.Objects;

public class BoolType extends AbstractType {
  private final Boolean constant;
  
	BoolType(boolean nullable, Boolean constant) {
    super(nullable);
    this.constant = constant;
  }
	
	@Override
	public int hashCode() {
	  return (isNullable()?1 : 0) ^ Objects.hashCode(constant); 
	}
	
	@Override
	public boolean equals(Object obj) {
	  if (this == obj) {
	    return true;
	  }
	  if (!(obj instanceof BoolType)) {
	    return false;
	  }
	  BoolType boolType = (BoolType)obj;
	  return isNullable() == boolType.isNullable() &&
	         Objects.equals(constant, boolType.constant);
	}
	
	@Override
	public String toString() {
	  return "bool" + (isNullable()? "?": "") + ((constant != null)? constant: "");
	}
	
	@Override
	public Type asNullable() {
	  if (isNullable()) {
	    return this;
	  }
	  if (constant != null) {
	    return new BoolType(true, constant);
	  }
	  return Types.BOOL_TYPE;
	}
	
	@Override
	public Type asNonNull() {
	  if (!isNullable()) {
	    return this;
	  }
	  if (constant == null) {
	    return Types.BOOL_NON_NULL_TYPE;
	  }
	  return (constant)? Types.TRUE: Types.FALSE;
	}
	
	@Override
	public Boolean asConstant() {
	  return constant;
	}
}
