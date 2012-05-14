package type;

import static type.CoreTypeRepository.DYNAMIC_NON_NULL_TYPE;
import static type.CoreTypeRepository.DYNAMIC_TYPE;

public class DynamicType extends AbstractType {

  public DynamicType(boolean nullable) {
    super(nullable);
  }

  @Override
  public String toString() {
    return "dynamic" + super.toString();
  }

  @Override
  public AbstractType asNullable() {
    if (isNullable()) {
      return this;
    }
    return DYNAMIC_TYPE;
  }

  @Override
  public AbstractType asNonNull() {
    if (!isNullable()) {
      return this;
    }
    return DYNAMIC_NON_NULL_TYPE;
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitDynamicType(this, parameter);
  }

  @Override
  public Object asConstant() {
    return null;
  }
}
