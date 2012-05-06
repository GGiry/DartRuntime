package type;

import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

public class BoolType extends PrimitiveType {
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
	public String getName() {
	  return "bool";
	}
	
	@Override
	public String toString() {
	  return super.toString() + ((constant != null)? constant: "");
	}
	
	@Override
	ClassElement getLazyElement() {
	  return CoreTypeRepository.getCoreTypeRepository().getBoolClassElement();
	}
	
	@Override
	public BoolType asNullable() {
	  if (isNullable()) {
	    return this;
	  }
	  if (constant != null) {
	    return new BoolType(true, constant);
	  }
	  return Types.BOOL_TYPE;
	}
	
	@Override
	public BoolType asNonNull() {
	  if (!isNullable()) {
	    return this;
	  }
	  if (constant == null) {
	    return Types.BOOL_NON_NULL_TYPE;
	  }
	  return (constant)? Types.TRUE: Types.FALSE;
	}
	
	@Override
  public <R,P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
	  return visitor.visitBoolType(this, parameter);
	}
	
	@Override
	public Boolean asConstant() {
	  return constant;
	}
}
