package type;

import java.math.BigInteger;
import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

import static type.CoreTypeRepository.*;

public class IntType extends PrimitiveType {
  private final BigInteger minBound;
  private final BigInteger maxBound;

  IntType(boolean nullable, /* maybenull */BigInteger minBound, /* maybenull */
      BigInteger maxBound) {
    super(nullable);
    this.minBound = minBound;
    this.maxBound = (Objects.equals(minBound, maxBound)) ? minBound : maxBound;
    // be sure that if the type is constant min == max
  }

  public static IntType constant(BigInteger constant) {
    Objects.requireNonNull(constant);
    return new IntType(false, constant, constant);
  }

  @Override
  public int hashCode() {
    return (isNullable() ? 1 : 0) ^ Objects.hashCode(minBound) ^ Integer.rotateLeft(Objects.hashCode(maxBound), 16);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IntType)) {
      return false;
    }
    IntType intType = (IntType) obj;
    return isNullable() == intType.isNullable() && Objects.equals(minBound, intType.minBound) && Objects.equals(maxBound, intType.maxBound);
  }

  @Override
  ClassElement getLazyElement() {
    return CoreTypeRepository.getCoreTypeRepository().getIntClassElement();
  }

  @Override
  public String getName() {
    return "int";
  }

  @Override
  public String toString() {
    return super.toString() + " [" + infinity('-', minBound) + ',' + infinity('+', maxBound) + ']';
  }

  private static String infinity(char sign, BigInteger value) {
    return (value == null) ? sign + "infinity" : value.toString();
  }

  /**
   * Return the minimum bound or null if the bound is -Infinity.
   * 
   * @return the minimum bound.
   */
  public/* maybenull */BigInteger getMinBound() {
    return minBound;
  }

  /**
   * Return the maximum bound or null if the bound is +Infinity.
   * 
   * @return the maximum bound.
   */
  public/* maybenull */BigInteger getMaxBound() {
    return maxBound;
  }

  public boolean isMinBoundInfinity() {
    return minBound == null;
  }

  public boolean isMaxBoundInfinity() {
    return maxBound == null;
  }

  @Override
  public IntType asNullable() {
    if (isNullable()) {
      return this;
    }
    if (minBound == null && maxBound == null) {
      return INT_TYPE;
    }
    return new IntType(true, minBound, maxBound);
  }

  @Override
  public IntType asNonNull() {
    if (!isNullable()) {
      return this;
    }
    if (minBound == null && maxBound == null) {
      return INT_NON_NULL_TYPE;
    }
    return new IntType(false, minBound, maxBound);
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitIntType(this, parameter);
  }

  @Override
  public BigInteger asConstant() {
    if (minBound == maxBound) {
      return minBound;
    }
    return null;
  }

  @Override
  Type merge(AbstractType type) {
    if (type == INT_TYPE) {
      return INT_TYPE;
    }
    if (type == INT_NON_NULL_TYPE) {
      return (isNullable) ? INT_TYPE : INT_NON_NULL_TYPE;
    }
    if (!(type instanceof IntType)) {
      return super.merge(type);
    }
    if (this == INT_TYPE) {
      return INT_TYPE;
    }
    if (this == INT_NON_NULL_TYPE) {
      return (type.isNullable) ? INT_TYPE : INT_NON_NULL_TYPE;
    }

    // test inclusion
    IntType intType = (IntType) type;

    if (maxBound != null && intType.minBound != null && maxBound.compareTo(intType.minBound.subtract(BigInteger.ONE)) < 0) {
      // no intersection
      return UnionType.createUnionType(this, intType);
    }

    if (minBound != null && intType.maxBound != null && intType.maxBound.compareTo(minBound.subtract(BigInteger.ONE)) < 0) {
      // no intersection
      return UnionType.createUnionType(intType, this);
    }

    BigInteger min = (minBound == null || intType.minBound == null) ? null : 
      minBound.compareTo(intType.minBound) < 0 ? minBound : intType.minBound;
    BigInteger max = (maxBound == null || intType.maxBound == null) ? null : 
      maxBound.compareTo(intType.maxBound) > 0 ? maxBound : intType.maxBound;
    boolean nullable = isNullable || intType.isNullable;
    if (min == null && max == null) {
      return (isNullable) ? INT_TYPE : INT_NON_NULL_TYPE;
    }
    return new IntType(nullable, min, max);
  }

  //
  // int[min, max] x = ...
  // if (x <= value) {
  //
  public/* maybenull */IntType asTypeLessOrEqualsThan(BigInteger value) {
    if (maxBound == null || value.compareTo(maxBound) <= 0) {
      if (minBound != null && value.compareTo(minBound) < 0) {
        return null;
      }
      return new IntType(isNullable(), minBound, value);
    }
    return this;
  }

  //
  // int[min, max] x = ...
  // if (x < value) {
  //
  public/* maybenull */IntType asTypeLessThan(BigInteger value) {
    return asTypeLessOrEqualsThan(value.add(BigInteger.ONE));
  }

  //
  // int[min, max] x = ...
  // if (x >= value) {
  //
  public/* maybenull */IntType asTypeGreaterOrEqualsThan(BigInteger value) {
    if (minBound == null || value.compareTo(minBound) >= 0) {
      if (maxBound != null && value.compareTo(maxBound) > 0) {
        return null;
      }
      return new IntType(isNullable(), value, maxBound);
    }
    return this;
  }

  //
  // int[min, max] x = ...
  // if (x > value) {
  //
  public/* maybenull */IntType asTypeGreaterThan(BigInteger value) {
    return asTypeGreaterOrEqualsThan(value.subtract(BigInteger.ONE));
  }
}
