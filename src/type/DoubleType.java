package type;

import java.util.Objects;

public class DoubleType extends AbstractType {
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
  public String toString() {
    return "double" + (isNullable()? "?": "") + ((constant != null)? constant: "");
  }
  
  @Override
  public Type asNullable() {
    if (isNullable()) {
      return this;
    }
    return Types.DOUBLE_TYPE;
  }
  
  @Override
  public Double asConstant() {
    return constant;
  }
  
  public static DoubleType constant(double constant) {
    return new DoubleType(false, constant);
  }
}
