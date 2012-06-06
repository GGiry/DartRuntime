package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;

import java.math.BigInteger;
import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

public class DoubleType extends PrimitiveType {
  private final Double constant;

  DoubleType(boolean isNullable, Double constant) {
    super(isNullable);
    this.constant = constant;
  }

  public static DoubleType constant(double constant) {
    return new DoubleType(false, constant);
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
    if (!(obj instanceof DoubleType)) {
      return false;
    }
    DoubleType doubleType = (DoubleType) obj;
    return isNullable() == doubleType.isNullable() && Objects.equals(constant, doubleType.constant);
  }

  @Override
  ClassElement getLazyElement() {
    return CoreTypeRepository.getCoreTypeRepository().getDoubleClassElement();
  }

  @Override
  public String toString() {
    return "double" + super.toString() + ' ' + ((constant != null) ? constant : "");
  }

  @Override
  public DoubleType asNullable() {
    if (isNullable()) {
      return this;
    }
    if (constant == null) {
      return DOUBLE_TYPE;
    }
    return new DoubleType(true, constant);
  }

  @Override
  public DoubleType asNonNull() {
    if (!isNullable()) {
      return this;
    }
    if (constant == null) {
      return DOUBLE_NON_NULL_TYPE;
    }
    return new DoubleType(false, constant);
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitDoubleType(this, parameter);
  }

  @Override
  public Double asConstant() {
    return constant;
  }

  @Override
  NullableType merge(NullableType type) {
    if (type == DOUBLE_TYPE) {
      return DOUBLE_TYPE;
    }
    if (type == DOUBLE_NON_NULL_TYPE) {
      return (isNullable()) ? DOUBLE_TYPE : DOUBLE_NON_NULL_TYPE;
    }
    if (!(type instanceof DoubleType)) {
      return super.merge(type);
    }
    if (this == DOUBLE_TYPE) {
      return DOUBLE_TYPE;
    }
    if (this == DOUBLE_NON_NULL_TYPE) {
      return (isNullable()) ? DOUBLE_TYPE : DOUBLE_NON_NULL_TYPE;
    }
    return super.merge(type);
  }

  public DoubleType add(DoubleType type) {
    if (constant != null && type.constant != null) {
      return constant(constant + type.constant);
    }
    return DOUBLE_NON_NULL_TYPE;
  }

  public DoubleType sub(DoubleType type) {
    if (constant != null && type.constant != null) {
      return constant(constant - type.constant);
    }
    return DOUBLE_NON_NULL_TYPE;
  }

  @Override
  public Type commonValuesWith(Type type) {
    if (type instanceof DoubleType) {
      return (constant.equals(((DoubleType) type).constant)) ? this : null;
    }

    if (type instanceof IntType) {
      return ((IntType) type).commonValuesWith(this);
    }

    if (type instanceof UnionType) {
      return ((UnionType) type).commonValuesWith(this);
    }

    return null;
  }

  @Override
  public Type invert() {
    return DOUBLE_TYPE;
  }

  @Override
  public Type lessThanOrEqualsValues(Type other, boolean inLoop) {
    if (other instanceof DoubleType) {
      if (constant.compareTo((Double) other.asConstant()) <= 0) {
        return this;
      }
      return null;
    }

    if (other instanceof IntType) {
      BigInteger min = (BigInteger) ((IntType) other).getMinBound();
      if (min != null) {
        float floatValue = constant.floatValue();
        if (floatValue == (int) floatValue) {
          if (BigInteger.valueOf((int) floatValue).compareTo(min) <= 0) {
            return this;
          }
        }
        if (BigInteger.valueOf((int) floatValue).compareTo(min) < 0) {
          return this;
        }
      }
      return null;
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).greaterThanOrEqualsValues(this, inLoop);
    }

    return null;
  }

  @Override
  public Type lessThanValues(Type other, boolean inLoop) {
    if (other instanceof DoubleType) {
      if (constant.compareTo((Double) other.asConstant()) < 0) {
        return this;
      }
      return null;
    }

    if (other instanceof IntType) {
      BigInteger min = (BigInteger) ((IntType) other).getMinBound();
      if (min != null) {
        float floatValue = constant.floatValue();
        if (BigInteger.valueOf((int) floatValue).compareTo(min) < 0) {
          return this;
        }
      }
      return null;
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).greaterThanValues(this, inLoop);
    }

    return null;
  }

  public Type unarySub() {
    return constant(-constant);
  }
  
  @Override
  public Type exclude(Type other) {
    if (other instanceof DoubleType) {
      if (Objects.equals(constant, other.asConstant())) {
        return null;
      }
      return this;
    }
    
    if (other instanceof IntType) {
      IntType iType = (IntType) other;
      BigInteger minBound = iType.getMinBound();
      if (minBound == null || minBound.compareTo(BigInteger.valueOf((int) constant.floatValue())) <= 0) {
        BigInteger maxBound = iType.getMaxBound();
        if (maxBound == null || maxBound.compareTo(BigInteger.valueOf((int) constant.floatValue())) >= 0) {
          return null;
        }
      }
      return this;
    }

    if (other instanceof UnionType) {
      return other.exclude(this);
    }
    
    return null;
  }

  public Type mod(DoubleType other) {
    Double value = new Double(constant.floatValue() % other.constant.floatValue());
    return new DoubleType(false, value);
  }
}
