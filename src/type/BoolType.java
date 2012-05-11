package type;

import static type.CoreTypeRepository.BOOL_NON_NULL_TYPE;
import static type.CoreTypeRepository.BOOL_TYPE;
import static type.CoreTypeRepository.FALSE;
import static type.CoreTypeRepository.TRUE;

import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

public class BoolType extends PrimitiveType {
  private final Boolean constant;

  BoolType(boolean nullable, Boolean constant) {
    super(nullable);
    this.constant = constant;
  }

  public static BoolType constant(boolean constant) {
    return (constant) ? TRUE : FALSE;
  }

  @Override
  public int hashCode() {
    return (isNullable() ? 1 : 0) ^ Objects.hashCode(constant);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BoolType)) {
      return false;
    }
    BoolType boolType = (BoolType) obj;
    return isNullable() == boolType.isNullable() && Objects.equals(constant, boolType.constant);
  }

  @Override
  ClassElement getLazyElement() {
    return CoreTypeRepository.getCoreTypeRepository().getBoolClassElement();
  }

  @Override
  public String toString() {
    return "bool" + super.toString() + ' ' + ((constant != null) ? constant : "");
  }

  @Override
  public BoolType asNullable() {
    if (isNullable()) {
      return this;
    }
    if (constant != null) {
      return new BoolType(true, constant);
    }
    return BOOL_TYPE;
  }

  @Override
  public BoolType asNonNull() {
    if (!isNullable()) {
      return this;
    }
    if (constant == null) {
      return BOOL_NON_NULL_TYPE;
    }
    return constant(constant);
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitBoolType(this, parameter);
  }

  @Override
  public Boolean asConstant() {
    return constant;
  }

  @Override
  AbstractType merge(AbstractType type) {
    if (type == BOOL_TYPE) {
      return BOOL_TYPE;
    }
    if (type == BOOL_NON_NULL_TYPE) {
      return (isNullable()) ? BOOL_TYPE : BOOL_NON_NULL_TYPE;
    }
    if (!(type instanceof BoolType)) {
      return super.merge(type);
    }
    if (this == BOOL_TYPE) {
      return BOOL_TYPE;
    }
    if (this == BOOL_NON_NULL_TYPE) {
      return (type.isNullable()) ? BOOL_TYPE : BOOL_NON_NULL_TYPE;
    }
    BoolType boolType = (BoolType) type;
    assert constant != null && boolType.constant != null;
    if (isNullable() || boolType.isNullable()) {
      if (constant.equals(boolType.constant)) {
        return asNullable();
      }
      // constants are different, so true, false and null are accepted
      return BOOL_TYPE;
    }
    if (constant.equals(boolType.constant)) {
      return this;
    }
    // only true and false are accepted
    return BOOL_NON_NULL_TYPE;
  }
}
