package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;

import java.math.BigInteger;
import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

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
  public String toString() {
    return "int" + super.toString() + " [" + infinity('-', minBound) + ',' + infinity('+', maxBound) + ']';
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
  NullableType merge(NullableType type) {
    if (type == INT_TYPE) {
      return INT_TYPE;
    }
    if (type == INT_NON_NULL_TYPE) {
      return (isNullable()) ? INT_TYPE : INT_NON_NULL_TYPE;
    }
    if (!(type instanceof IntType)) {
      return super.merge(type);
    }
    if (this == INT_TYPE) {
      return INT_TYPE;
    }
    if (this == INT_NON_NULL_TYPE) {
      return (type.isNullable()) ? INT_TYPE : INT_NON_NULL_TYPE;
    }

    // test inclusion
    IntType intType = (IntType) type;

    if (maxBound != null && intType.minBound != null && maxBound.add(BigInteger.ONE).compareTo(intType.minBound) < 0) {
      // no intersection
      return UnionType.createUnionType(this, intType);
    }

    if (minBound != null && intType.maxBound != null && intType.maxBound.add(BigInteger.ONE).compareTo(minBound) < 0) {
      // no intersection
      return UnionType.createUnionType(intType, this);
    }

    BigInteger min = (minBound == null || intType.minBound == null) ? null : minBound.compareTo(intType.minBound) < 0 ? minBound : intType.minBound;
    BigInteger max = (maxBound == null || intType.maxBound == null) ? null : maxBound.compareTo(intType.maxBound) > 0 ? maxBound : intType.maxBound;
    boolean nullable = isNullable() || intType.isNullable();
    if (min == null && max == null) {
      return (isNullable()) ? INT_TYPE : INT_NON_NULL_TYPE;
    }
    return new IntType(nullable, min, max);
  }

  public DoubleType asDouble() {
    if (minBound != null && minBound == maxBound) {
      DoubleType type = DoubleType.constant(minBound.doubleValue());
      return (isNullable()) ? type.asNullable() : type;
    }
    return (isNullable()) ? DOUBLE_TYPE : DOUBLE_NON_NULL_TYPE;
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

  public IntType add(IntType type) {
    BigInteger minBound = (this.minBound == null | type.minBound == null) ? null : this.minBound.add(type.minBound);
    BigInteger maxBound = (this.maxBound == null | type.maxBound == null) ? null : this.maxBound.add(type.maxBound);
    if (minBound == null && maxBound == null) {
      return INT_NON_NULL_TYPE;
    }
    return new IntType(false, minBound, maxBound);
  }

  public IntType sub(IntType type) {
    BigInteger minBound = (this.minBound == null | type.minBound == null) ? null : this.minBound.subtract(type.minBound);
    BigInteger maxBound = (this.maxBound == null | type.maxBound == null) ? null : this.maxBound.subtract(type.maxBound);
    if (minBound == null && maxBound == null) {
      return INT_NON_NULL_TYPE;
    }
    return new IntType(false, minBound, maxBound);
  }

  /*
   * public IntType mul(IntType type) { BigInteger minBound = (this.minBound ==
   * null | type.minBound == null)? null: this.minBound.multiply(type.minBound);
   * BigInteger maxBound = (this.maxBound == null | type.maxBound == null)?
   * null: this.maxBound.multiply(type.maxBound); if (minBound == null &&
   * maxBound == null) { return INT_NON_NULL_TYPE; } return new IntType(false,
   * minBound, maxBound); }
   */

  /**
   * Returns <code>true</code> if this type is include in the specified type.
   * 
   * @param intType
   *          Reference type.
   * @return <code>true</code> if this type is include in the specified type.
   */
  public boolean isIncludeIn(IntType intType) {
    if (minBound == null && intType.minBound != null) {
      return false;
    }

    if (maxBound == null && intType.maxBound != null) {
      return false;
    }
    boolean min = false;
    if (intType.minBound == null || intType.minBound.compareTo(minBound) <= 0) {
      min = true;
    }
    boolean max = false;
    if (intType.maxBound == null || intType.maxBound.compareTo(maxBound) >= 0) {
      max = true;
    }

    return min && max;
  }

  @Override
  public Type commonValuesWith(Type type) {
    if (type instanceof IntType) {
      return intersect(this, (IntType) type);
    }

    if (type instanceof DoubleType) {
      DoubleType dType = (DoubleType) type;
      double constant = dType.asConstant().doubleValue();

      if (((int) constant) == constant) {
        BigInteger valueOfCst = BigInteger.valueOf((long) constant);
        return intersect(new IntType(isNullable() && type.isNullable(), valueOfCst, valueOfCst), this);
      }
      return null;
    }

    if (type instanceof UnionType) {
      return type.commonValuesWith(this);
    }

    return null;
  }

  /**
   * Returns the intersection of type1 and type2.
   * 
   * @param type1
   * @param type2
   * @return The intersection of type1 and type2. Or null if two ranges doens't
   *         intersect.
   */
  public static IntType intersect(IntType type1, IntType type2) {
    DiffResult diff = diff(type1, type2);

    switch (diff) {
    case FIRST_IS_LEFT:
    case SECOND_IS_LEFT:
      return null;
    case FIRST_CONTAINS_SECOND:
      return type2;
    case SECOND_CONTAINS_FIRST:
      return type1;
    case EQUALS:
      return type1;
    case FIRST_IS_LEFT_OVERLAP:
      return new IntType(type1.isNullable() && type2.isNullable(), type2.minBound, type1.maxBound);
    case SECOND_IS_LEFT_OVERLAP:
      return new IntType(type1.isNullable() && type2.isNullable(), type1.minBound, type2.maxBound);
    }
    throw new IllegalStateException();
  }

  private enum DiffResult {
    FIRST_CONTAINS_SECOND(-3),
    FIRST_IS_LEFT(-2),
    FIRST_IS_LEFT_OVERLAP(-1),
    EQUALS(0),
    SECOND_IS_LEFT_OVERLAP(1),
    SECOND_IS_LEFT(2),
    SECOND_CONTAINS_FIRST(3);

    private final int value;

    private DiffResult(int value) {
      this.value = value;
    }

    public static DiffResult getDiff(int value) {
      for (DiffResult diffResult : values()) {
        if (diffResult.value == value) {
          return diffResult;
        }
      }
      throw new IllegalArgumentException("Value must be in range (-3, 3)");
    }
  }

  private static DiffResult diff(IntType type1, IntType type2) {
    int tmp = diffHelper(type1, type2);
    if (tmp != 0) {
      return DiffResult.getDiff(-tmp);
    }

    tmp = diffHelper(type2, type1);
    if (tmp != 0) {
      return DiffResult.getDiff(tmp);
    }
    if (type1.minBound == null && type2.minBound == null && type1.maxBound == null && type2.maxBound == null) {
      return DiffResult.EQUALS;
    }
    throw new IllegalStateException();
  }

  private static int diffHelper(IntType type1, IntType type2) {
    if (type2.minBound != null) {
      if (type2.maxBound != null) {
        if (type1.minBound == null || type1.minBound.compareTo(type2.minBound) < 0) {
          if (type1.maxBound == null || type1.maxBound.compareTo(type2.maxBound) > 0) {
            return 3;
          }
        }
      }
      if (type1.maxBound != null) {
        if (type1.maxBound.compareTo(type2.minBound) < 0) {
          return 2;
        } else if (type1.minBound == null || type1.minBound.compareTo(type2.minBound) < 0) {
          return 1;
        }
      }
    }
    return 0;
  }

  /**
   * Returns <code>true</code> if this type as common values with the specified
   * type.
   * 
   * @param other
   *          Type to check.
   * @return <code>true</code> if this type as common values with the specified
   *         type.
   */
  public boolean hasCommonValuesWith(IntType other) {
    if (minBound == null) {
      // min == -inf
      if (maxBound == null || other.minBound == null || maxBound.compareTo(other.minBound) < 0) {
        // max == +inf || other.min == inf || max < other.min
        return true;
      }
      return false;
    } else {
      // min != -inf
      if (other.maxBound == null) {
        // oher.max == +inf
        if (maxBound != null) {
          // max != +inf
          if (other.minBound == null || maxBound.compareTo(other.minBound) > 0) {
            // other.min == inf || max > other.min
            return true;
          }
          return false;
        }
        // max == +inf
        return true;
      } else {
        // min != -inf
        // other.max != +inf
        if (minBound.compareTo(other.maxBound) > 0) {
          return false;
        }
        return true;
      }
    }
  }

  @Override
  public Type invert() {
    if (minBound == null) {
      if (maxBound == null) {
        return null;
      }
    }

    Type result;
    if (minBound != null) {
      result = new IntType(isNullable(), null, minBound.subtract(BigInteger.ONE));
      if (maxBound != null) {
        result = Types.union(result, new IntType(isNullable(), maxBound.add(BigInteger.ONE), null));
      }
      return result;
    }

    return new IntType(isNullable(), maxBound.add(BigInteger.ONE), null);
  }

  @Override
  public Type LTEValues(Type other) {
    if (other instanceof IntType) {
      IntType iType = (IntType) other;
      BigInteger cst = asConstant();
      BigInteger oCst = iType.asConstant();
      
      if (oCst != null) {
        DiffResult diff = diff(this, iType);
        switch (diff) {
        case FIRST_IS_LEFT:
        case SECOND_IS_LEFT:
        case EQUALS:
          return this;
        case FIRST_CONTAINS_SECOND:
          return new IntType(isNullable(), minBound, oCst);
        default:
          throw new IllegalStateException();
        }
      }

      if (cst != null) {
        DiffResult diff = diff(this, iType);
        switch (diff) {
        case FIRST_IS_LEFT:
        case SECOND_IS_LEFT:
        case EQUALS:
          return this;
        case SECOND_CONTAINS_FIRST:
          return new IntType(isNullable(), cst, iType.maxBound);
        default:
          throw new IllegalStateException();
        }
      }

      // - a = [10; 20], b = [15; 25] (no change)
      return VOID_TYPE;
    }

    if (other instanceof DoubleType) {
      BigInteger cst = (BigInteger) other.asConstant();
      if (maxBound != null) {
        if (maxBound.compareTo(cst) <= 0) {
          return this;
        }
      }
      return null;
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).GTEValues(this);
    }
    return null;
  }

  @Override
  public Type LTValues(Type other) {
    if (other instanceof IntType) {
      IntType iType = (IntType) other;
      BigInteger cst = asConstant();
      BigInteger oCst = iType.asConstant();

      if (oCst != null) {
        DiffResult diff = diff(this, iType);

        switch (diff) {
        case FIRST_IS_LEFT:
        case SECOND_IS_LEFT:
        case EQUALS:
          return this;
        case FIRST_CONTAINS_SECOND:
          return new IntType(isNullable(), minBound, oCst.subtract(BigInteger.ONE));
        default:
          throw new IllegalStateException();
        }
      }

      if (cst != null) {
        DiffResult diff = diff(this, iType);

        switch (diff) {
        case FIRST_IS_LEFT:
        case SECOND_IS_LEFT:
        case EQUALS:
          return this;
        case SECOND_CONTAINS_FIRST:
          return new IntType(isNullable(), cst.add(BigInteger.ONE), iType.maxBound);
        default:
          throw new IllegalStateException();
        }
      }

      return VOID_TYPE;
    }

    if (other instanceof DoubleType) {
      BigInteger cst = (BigInteger) other.asConstant();
      if (maxBound != null) {
        if (maxBound.compareTo(cst) < 0) {
          return this;
        }
      }
      return null;
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).GTValues(this);
    }
    return null;
  }

  public boolean isStrictLT(IntType other) {
    if (diff(this, other) == DiffResult.FIRST_IS_LEFT) {
      return true;
    }
    return false;
  }

  public boolean isStrictLTE(IntType other) {
    DiffResult diff = diff(this, other);

    switch (diff) {
    case FIRST_IS_LEFT:
      return true;
    case EQUALS:
      BigInteger constant = asConstant();
      if (constant != null && constant.equals(other.asConstant())) {
        return true;
      }
      return false;
    case FIRST_IS_LEFT_OVERLAP:
      if (other.minBound != null && maxBound != null && other.minBound.equals(maxBound)) {
        return true;
      }
    }
    return false;
  }
}
