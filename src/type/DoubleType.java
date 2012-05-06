package type;

import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

public class DoubleType extends PrimitiveType {
	private final Double constant;

  DoubleType(boolean isNullable, Double constant) {
		super(isNullable);
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
    if (!(obj instanceof DoubleType)) {
      return false;
    }
    DoubleType doubleType = (DoubleType)obj;
    return isNullable() == doubleType.isNullable() &&
           Objects.equals(constant, doubleType.constant);
  }
  
  @Override
  public String getName() {
    return "double";
  }
  
  @Override
  public String toString() {
    return super.toString() + ((constant != null)? constant: "");
  }
  
  @Override
  ClassElement getLazyElement() {
    return CoreTypeRepository.getCoreTypeRepository().getDoubleClassElement();
  }
  
  @Override
  public DoubleType asNullable() {
    if (isNullable()) {
      return this;
    }
    if (constant == null) {
      return Types.DOUBLE_TYPE;
    }
    return new DoubleType(true, constant);
  }
  
  @Override
  public DoubleType asNonNull() {
    if (!isNullable()) {
      return this;
    }
    if (constant == null) {
      return Types.DOUBLE_NON_NULL_TYPE;
    }
    return new DoubleType(false, constant);
  }
  
  @Override
  public <R,P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitDoubleType(this, parameter);
  }
  
  @Override
  public Double asConstant() {
    return constant;
  }
  
  public static DoubleType constant(double constant) {
    return new DoubleType(false, constant);
  }
}
