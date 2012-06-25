package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

public class DoubleType extends PrimitiveType implements NumType {
  private final Double constant;

  DoubleType(boolean isNullable, Double constant) {
    super(isNullable);
    this.constant = constant;
  }

  public static DoubleType constant(double constant) {
    return new DoubleType(false, constant);
  }

  @Override
  public Type asDouble() {
    return this;
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
  public Type lessThanOrEqualsValues(Type other, boolean inLoop) {
    if (other instanceof DoubleType) {
      if (constant.compareTo((Double) other.asConstant()) <= 0) {
        return this;
      }
      return null;
    }

    if (other instanceof IntType) {
      BigInteger min = ((IntType) other).getMinBound();
      if (min != null) {
        float floatValue = constant.floatValue();
        if (BigInteger.valueOf((int) floatValue).compareTo(min) <= 0) {
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
      BigInteger min = ((IntType) other).getMinBound();
      if (min != null) {
        float floatValue = constant.floatValue();
        int compareTo = BigInteger.valueOf((int) floatValue).compareTo(min);
        if (compareTo < 0) {
          return this;
        } else if (compareTo == 0) {
          if (floatValue != (int) floatValue) {
            return this;
          }
        }
      }
      return null;
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).greaterThanValues(this, inLoop);
    }

    return null;
  }

  @Override
  public Type greaterThanOrEqualsValues(Type other, boolean inLoop) {
    if (other instanceof DoubleType) {
      if (constant.compareTo((Double) other.asConstant()) >= 0) {
        return this;
      }
      return null;
    }

    if (other instanceof IntType) {
      BigInteger max = ((IntType) other).getMaxBound();
      if (max != null) {
        float floatValue = constant.floatValue();
        if (BigInteger.valueOf((int) floatValue).compareTo(max) >= 0) {
          return this;
        }
      }
      return null;
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).lessThanOrEqualsValues(this, inLoop);
    }

    return null;
  }

  @Override
  public Type greaterThanValues(Type other, boolean inLoop) {
    if (other instanceof DoubleType) {
      if (constant.compareTo((Double) other.asConstant()) > 0) {
        return this;
      }
      return null;
    }

    if (other instanceof IntType) {
      BigInteger max = ((IntType) other).getMaxBound();
      if (max != null) {
        float floatValue = constant.floatValue();
        int compareTo = BigInteger.valueOf((int) floatValue).compareTo(max);
        if (compareTo > 0) {
          return this;
        } else if (compareTo == 0) {
          if (floatValue != (int) floatValue) {
            return this;
          }
        }
      }
      return null;
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).greaterThanValues(this, inLoop);
    }

    return null;
  }

  @Override
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

  @Override
  public Type add(Type other) {
    if (other instanceof IntType) {
      DoubleType dType = ((IntType) other).asDouble();
      return add(dType);
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).add(this);
    }

    return null;
  }

  @Override
  public Type sub(Type other) {
    if (other instanceof IntType) {
      DoubleType dType = ((IntType) other).asDouble();
      return sub(dType);
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).reverseSub(this);
    }

    return null;
  }

  @Override
  public Type mod(Type other) {
    if (other instanceof IntType) {
      DoubleType dType = ((IntType) other).asDouble();
      return mod(dType);
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).reverseMod(this);
    }

    return null;
  }

  public Type reverseMod(IntType other) {
    DoubleType dType = other.asDouble();
    return mod(dType);
  }

  public Type reverseSub(IntType other) {
    DoubleType dType = other.asDouble();
    return sub(dType);
  }

  @Override
  public boolean isIncludeIn(Type other) {
    if (isNullable() && !other.isNullable()) {
      return false;
    }

    if (other instanceof DoubleType) {
      Double otherConstant = ((DoubleType) other).constant;
      if (constant != null) {
        if (otherConstant != null) {
          return constant.equals(otherConstant);
        }
        // the first test "isNullable() && ! other.isNullable()" allow to avoid
        // the case when this == DOUBLE_TYPE and other == DOUBLE_NON_NULL_TYPE.
        return true;
      }
      if (otherConstant == null) {
        return true;
      }
      return false;
    }

    if (other instanceof IntType) {
      IntType intType = (IntType) other;
      if (constant == null && intType.getMaxBound() == null && intType.getMinBound() == null) {
        return true;
      }

      IntType iType;
      BigDecimal bigDecValue = BigDecimal.valueOf(constant);
      try {
        BigInteger bigIntValue = bigDecValue.toBigIntegerExact();
        iType = new IntType(isNullable(), bigIntValue, bigIntValue);
      } catch (ArithmeticException e) {
        BigInteger bigIntValue = bigDecValue.toBigInteger();
        iType = new IntType(isNullable(), bigIntValue, bigIntValue.add(BigInteger.ONE));
      }

      return iType.isIncludeIn(other);
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).reverseIsIncludeIn(this);
    }

    if (other instanceof DynamicType) {
      return true;
    }

    return false;
  }

  @Override
  public boolean isAssignableFrom(Type other) {
    if (other instanceof NumType) {
      Object otherCst = other.asConstant();
      if (otherCst == null) {
        if (asConstant() == null) {
          if (isNullable() || !other.isNullable()) {
            return true;
          }
        }
        return false;
      }

      if (asConstant() == null) {
        if (isNullable() || !other.isNullable()) {
          return true;
        }
        return false;
      }

      if (other instanceof DoubleType) {
        Double doubleCst = (Double) otherCst;
        return asConstant().equals(doubleCst);
      }
      if (other instanceof IntType) {
        BigInteger bigIntCst = (BigInteger) otherCst;
        return asConstant().equals(bigIntCst.floatValue());
      }
      return false;
    }

    if (other instanceof NullType) {
      if (isNullable()) {
        return true;
      }
    }

    return false;
  }

  public static Type truncate(DoubleType dType1, DoubleType dType2) {
    Double asConstant1 = dType1.asConstant();
    Double asConstant2 = dType2.asConstant();
    
    if (asConstant1 != null && asConstant2 != null) {
      BigDecimal bigDec1 = BigDecimal.valueOf(asConstant1);
      BigDecimal bigDec2 = BigDecimal.valueOf(asConstant2);
      
      return constant(bigDec1.divideToIntegralValue(bigDec2).toBigInteger().doubleValue());
    }
    
    return DOUBLE_NON_NULL_TYPE;
  }
}
