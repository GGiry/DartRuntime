package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;

import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

public class BoolType extends PrimitiveType {
  private final Boolean constant;

  BoolType(boolean nullable, Boolean constant) {
    super(nullable);
    this.constant = constant;
  }

  public static BoolType constant(boolean constant) {
    return (constant) ? TRUE_TYPE : FALSE_TYPE;
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
  NullableType merge(NullableType type) {
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

  @Override
  public Type commonValuesWith(Type type) {
    if (type instanceof BoolType) {
      if (constant.equals(((BoolType) type).constant)) {
        return this;
      }

      if (this == TRUE_TYPE) {
        if (type == FALSE_TYPE) {
          return null;
        }
        return this;
      }

      if (this == FALSE_TYPE) {
        if (type == TRUE_TYPE) {
          return null;
        }
        return this;
      }

      if (type == TRUE_TYPE || type == FALSE_TYPE) {
        return type;
      }

      return BOOL_NON_NULL_TYPE;
    }

    if (type instanceof UnionType) {
      return ((UnionType) type).commonValuesWith(this);
    }

    return null;
  }

  @Override
  public Type invert() {
    if (this == TRUE_TYPE) {
      return FALSE_TYPE;
    }
    if (this == FALSE_TYPE) {
      return TRUE_TYPE;
    }
    return this;
  }

  @Override
  public Type lessThanOrEqualsValues(Type other, boolean inLoop) {
    return null;
  }

  @Override
  public Type lessThanValues(Type other, boolean inLoop) {
    return null;
  }
  
  @Override
  public Type greaterThanOrEqualsValues(Type other, boolean inLoop) {
    return null;
  }
  
  @Override
  public Type greaterThanValues(Type other, boolean inLoop) {
    return null;
  }

  @Override
  public Type exclude(Type other) {
    if (other instanceof BoolType) {
      if (other.equals(this)) {
        return null;
      }

      if (this == TRUE_TYPE || this == FALSE_TYPE) {
        if (other != BOOL_NON_NULL_TYPE && other != BOOL_TYPE) {
          return this;
        }
        return null;
      }
    }

    if (other instanceof UnionType) {
      return other.exclude(this);
    }

    return null;
  }

  @Override
  public Type add(Type other) {
    return null;
  }

  @Override
  public Type mod(Type other) {
    return null;
  }

  @Override
  public Type sub(Type other) {
    return null;
  }
}
