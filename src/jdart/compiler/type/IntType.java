package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

public class IntType extends PrimitiveType implements NumType {
  private final BigInteger minBound;
  private final BigInteger maxBound;

  IntType(boolean nullable, /* maybenull */BigInteger minBound, /* maybenull */
      BigInteger maxBound) {
    super(nullable);

    if (minBound != null && maxBound != null) {
      if (minBound.compareTo(maxBound) > 0) {
        throw new IllegalArgumentException("minBound must be lesser than maxBound.");
      }
    }

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

  @Override
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

  public IntType addInt(IntType type) {
    BigInteger minBound = (this.minBound == null | type.minBound == null) ? null : this.minBound.add(type.minBound);
    BigInteger maxBound = (this.maxBound == null | type.maxBound == null) ? null : this.maxBound.add(type.maxBound);
    if (minBound == null && maxBound == null) {
      return INT_NON_NULL_TYPE;
    }
    return new IntType(false, minBound, maxBound);
  }

  public IntType subInt(IntType type) {
    BigInteger minBound = (this.minBound == null | type.minBound == null) ? null : this.minBound.subtract(type.minBound);
    BigInteger maxBound = (this.maxBound == null | type.maxBound == null) ? null : this.maxBound.subtract(type.maxBound);
    if (minBound == null && maxBound == null) {
      return INT_NON_NULL_TYPE;
    }
    
    if (maxBound != null && minBound != null && minBound.compareTo(maxBound) > 0) {
      return new IntType(false, maxBound, minBound);
    }
    return new IntType(false, minBound, maxBound);
  }

  public IntType mul(IntType type) {
    BigInteger minBound = (this.minBound == null | type.minBound == null) ? null : this.minBound.multiply(type.minBound);
    BigInteger maxBound = (this.maxBound == null | type.maxBound == null) ? null : this.maxBound.multiply(type.maxBound);
    if (minBound == null && maxBound == null) {
      return INT_NON_NULL_TYPE;
    }
    if (minBound != null && minBound.compareTo(maxBound) > 0) {
      return new IntType(false, maxBound, minBound);
    }
    return new IntType(false, minBound, maxBound);
  }

  /**
   * Return the result type of the division : this / other.
   * It's a Dart division, the return is always a double.
   * @param other Divisor.
   * @return The result type of the division.
   */
  // In dart 5 / 2 = 2.5 and 4 / 2 = 2.0. We must use trunc (~/) to have a java-like divide.
  public Type div(IntType other) {
    BigInteger cst = asConstant();
    BigInteger oCst = other.asConstant();
    if (cst != null && oCst != null) {
      double doubleValue1 = cst.doubleValue();
      double doubleValue2 = oCst.doubleValue();
      return DoubleType.constant(doubleValue1 / doubleValue2);
    }
    return DOUBLE_NON_NULL_TYPE;
  }

  /**
   * Return the result type of the operation divide and truncate : this ~/ other.
   * @param other Divisor.
   * @return The result type.
   */
  // trunc is the java-like division 5 ~/ 2 = 2 and 4 ~/ 2 = 2. 
  public IntType trunc(IntType type) {
    BigInteger minBound = (this.minBound == null | type.maxBound == null) ? null : this.minBound.divide(type.maxBound);
    BigInteger maxBound = (this.maxBound == null | type.minBound == null) ? null : this.maxBound.divide(type.minBound);
    if (minBound == null && maxBound == null) {
      return INT_NON_NULL_TYPE;
    }
    if (minBound != null && minBound.compareTo(maxBound) > 0) {
      return new IntType(false, maxBound, minBound);
    }
    return new IntType(false, minBound, maxBound);
  }

  public Type shiftLeft(IntType type) {
    BigInteger minBound = (this.minBound == null | type.minBound == null) ? null : this.minBound.shiftLeft(type.minBound.intValue());
    BigInteger maxBound = (this.maxBound == null | type.maxBound == null) ? null : this.maxBound.shiftLeft(type.maxBound.intValue());
    if (minBound == null && maxBound == null) {
      return INT_NON_NULL_TYPE;
    }
    return new IntType(false, minBound, maxBound);
  }

  public Type shiftRight(IntType type) {
    BigInteger minBound = (this.minBound == null | type.maxBound == null) ? null : this.minBound.shiftRight(type.maxBound.intValue());
    BigInteger maxBound = (this.maxBound == null | type.minBound == null) ? null : this.maxBound.shiftRight(type.minBound.intValue());
    if (minBound == null && maxBound == null) {
      return INT_NON_NULL_TYPE;
    }
    return new IntType(false, minBound, maxBound);
  }

  @Override
  public Type commonValuesWith(Type type) {
    if (type instanceof IntType) {
      return intersect(this, (IntType) type);
    }

    if (type instanceof DoubleType) {
      DoubleType dType = (DoubleType) type;
      double constant = dType.asConstant().doubleValue();
      BigDecimal bigDecValue = BigDecimal.valueOf(constant);
      try {
        BigInteger valueOfCst = bigDecValue.toBigIntegerExact();
        return intersect(new IntType(isNullable() && type.isNullable(), valueOfCst, valueOfCst), this);
      } catch (ArithmeticException e) {
        return null;
      }
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
    default:
    }
    throw new IllegalStateException();
  }

  enum DiffResult {
    FIRST_CONTAINS_SECOND,
    FIRST_IS_LEFT,
    FIRST_IS_LEFT_OVERLAP,
    EQUALS,
    SECOND_IS_LEFT_OVERLAP,
    SECOND_IS_LEFT,
    SECOND_CONTAINS_FIRST;
  }

  static DiffResult diff(IntType type1, IntType type2) {
    BigInteger minBound1 = type1.minBound;
    BigInteger maxBound1 = type1.maxBound;
    BigInteger minBound2 = type2.minBound;
    BigInteger maxBound2 = type2.maxBound;

    if (minBound1 == null) {
      // ]-inf; ?] & [?; ?]
      if (minBound2 == null) {
        // ]-inf; ?] & ]-inf; ?]
        if (maxBound1 == null) {
          // ]-inf; +inf[ & ]-inf; ?]
          if (maxBound2 == null) {
            // ]-inf; +inf[ & ]-inf; +inf[
            return DiffResult.EQUALS;
          }
          // ]-inf; +inf[ & ]-inf; l[
          return DiffResult.FIRST_CONTAINS_SECOND;
        }
        // ]-inf; j] & ]-inf; ?]
        if (maxBound2 == null) {
          // ]-inf; j] & ]-inf; +inf[
          return DiffResult.SECOND_CONTAINS_FIRST;
        }
        // ]-inf; j] & ]-inf; l]
        int max1CompareToMax2 = maxBound1.compareTo(maxBound2);
        if (max1CompareToMax2 < 0) {
          // ]-inf; j] & ]-inf; l] with j < l
          return DiffResult.SECOND_CONTAINS_FIRST;
        } else if (max1CompareToMax2 == 0) {
          // ]-inf; j] & ]-inf; l] with j == l
          return DiffResult.EQUALS;
        } else {
          // ]-inf; j] & ]-inf; l] with j > l
          return DiffResult.FIRST_CONTAINS_SECOND;
        }
      }
      // ]-inf; ?] & [k; ?]
      if (maxBound1 == null) {
        // ]-inf; +inf[ & [k; ?]
        if (maxBound2 == null) {
          return DiffResult.FIRST_IS_LEFT_OVERLAP;
        }
        return DiffResult.FIRST_CONTAINS_SECOND;
      }
      // ]-inf; j] & [k; ?]
      if (maxBound2 == null) {
        // ]-inf; j] & [k; +inf[
        int max1CompareToMin2 = maxBound1.compareTo(minBound2);
        if (max1CompareToMin2 < 0) {
          // ]-inf; j] & [k; +inf[ && j < k
          return DiffResult.FIRST_IS_LEFT;
        }
        // ]-inf; j] & [k; +inf[ && j >= k
        return DiffResult.FIRST_IS_LEFT_OVERLAP;
      }
      // ]-inf; j] & [k; l]
      int max1CompareToMin2 = maxBound1.compareTo(minBound2);
      int max1CompareToMax2 = maxBound1.compareTo(maxBound2);
      if (max1CompareToMax2 > 0) {
        // ]-inf; j] & [k; l] && j > l
        return DiffResult.FIRST_CONTAINS_SECOND;
      } else if (max1CompareToMax2 == 0) {
        // ]-inf; j] & [k; l] && j == l
        return DiffResult.FIRST_IS_LEFT_OVERLAP;
      }
      // ]-inf; j] & [k; l] && j < l
      if (max1CompareToMin2 < 0) {
        // ]-inf; j] & [k; l] && j < k && j < l
        return DiffResult.FIRST_IS_LEFT;
      } else if (max1CompareToMin2 == 0) {
        // ]-inf; j] & [k; l] && j == k && j < l
        return DiffResult.FIRST_IS_LEFT_OVERLAP;
      }
      // ]-inf; j] & [k; l] && j > k && j < l
      return DiffResult.FIRST_IS_LEFT_OVERLAP;
    }
    // [i; ?] & [?; ?]
    if (maxBound1 == null) {
      // [i; +inf[ & [?; ?]
      if (minBound2 == null) {
        // [i; +inf[ & ]-inf; ?]
        if (maxBound2 == null) {
          // [i; +inf[ & ]-inf; +inf[
          return DiffResult.SECOND_IS_LEFT_OVERLAP;
        }
        // [i; +inf[ & ]-inf; l]
        int min1CompareToMax2 = minBound1.compareTo(maxBound2);
        if (min1CompareToMax2 <= 0) {
          // [i; +inf[ & ]-inf; l] && i <= j
          return DiffResult.SECOND_IS_LEFT_OVERLAP;
        }
        // [i; +inf[ & ]-inf; l] && i > j
        return DiffResult.SECOND_IS_LEFT;
      }
      // [i; +inf[ & [k; ?]
      int min1CompareToMin2 = minBound1.compareTo(minBound2);
      if (min1CompareToMin2 < 0) {
        // [i; +inf[ & [k; ?] && i < k
        return DiffResult.FIRST_CONTAINS_SECOND;
      } else if (min1CompareToMin2 == 0) {
        // [i; +inf[ & [k; ?] && i == k
        if (maxBound2 == null) {
          // [i; +inf[ & [k; +inf[ && i == k
          return DiffResult.EQUALS;
        }
        if (minBound1.compareTo(maxBound2) == 0) {
          // [i; +inf[ & [k; ?] && i == k && i == l
          return DiffResult.SECOND_IS_LEFT_OVERLAP;
        }
        // [i; +inf[ & [k; l] && i == k
        return DiffResult.FIRST_CONTAINS_SECOND;
      } else {
        // [i; +inf[ & [k; ?] && i > k
        if (maxBound2 == null) {
          // [i; +inf[ & [k; +inf[ && i > k
          return DiffResult.SECOND_CONTAINS_FIRST;
        }
        // [i; +inf[ & [k; l] && i > k
        int min1CompareToMax2 = minBound1.compareTo(maxBound2);
        if (min1CompareToMax2 <= 0) {
          // [i; +inf[ & [k; l] && i > k && i <= l
          return DiffResult.SECOND_IS_LEFT_OVERLAP;
        }
        // [i; +inf[ & [k; l] && i > k && i > l
        return DiffResult.SECOND_IS_LEFT;
      }
    }
    // [i; j] & [?; ?]
    if (minBound2 == null) {
      // [i; j] & ]-inf; ?]
      if (maxBound2 == null) {
        // [i; j] & ]-inf; +inf[
        return DiffResult.SECOND_CONTAINS_FIRST;
      }
      // [i; j] & ]-inf; l]
      int min1CompareToMax2 = minBound1.compareTo(maxBound2);
      if (min1CompareToMax2 > 0) {
        // [i; j] & ]-inf; l] && i > l
        return DiffResult.SECOND_IS_LEFT;
      } else if (min1CompareToMax2 == 0) {
        // [i; j] & ]-inf; l] && i == l
        return DiffResult.SECOND_IS_LEFT_OVERLAP;
      } else {
        // [i; j] & ]-inf; l] && i < l
        int max1CompareToMax2 = maxBound1.compareTo(maxBound2);
        if (max1CompareToMax2 <= 0) {
          // [i; j] & ]-inf; l] && i < l && j <= l
          return DiffResult.SECOND_CONTAINS_FIRST;
        }
        // [i; j] & ]-inf; l] && i < l && j > l
        return DiffResult.SECOND_IS_LEFT_OVERLAP;
      }
    }
    // [i; j] & [k; ?]
    if (maxBound2 == null) {
      int min1CompareToMin2 = minBound1.compareTo(minBound2);
      int max1CompareToMin2 = maxBound1.compareTo(minBound2);

      if (max1CompareToMin2 < 0) {
        // [i; j] & [k; +inf[ && j < k
        return DiffResult.FIRST_IS_LEFT;
      } else if (max1CompareToMin2 == 0) {
        // [i; j] & [k; +inf[ && j == k
        return DiffResult.FIRST_IS_LEFT_OVERLAP;
      } else {
        // [i; j] & [k; +inf[ && j > k
        if (min1CompareToMin2 < 0) {
          // [i; j] & [k; +inf[ && j > k && i < k
          return DiffResult.FIRST_IS_LEFT_OVERLAP;
        }
        // [i; j] & [k; +inf[ && j > k && i >= k
        return DiffResult.SECOND_CONTAINS_FIRST;
      }
    }
    // [i; j] & [k; l]
    int min1CompareToMin2 = minBound1.compareTo(minBound2);
    int min1CompareToMax2 = minBound1.compareTo(maxBound2);
    int max1CompareToMin2 = maxBound1.compareTo(minBound2);
    int max1CompareToMax2 = maxBound1.compareTo(maxBound2);

    if (min1CompareToMin2 == 0 && max1CompareToMax2 == 0) {
      return DiffResult.EQUALS;
    }

    if (max1CompareToMin2 < 0) {
      // [i; j] & [k; l] && j < k
      return DiffResult.FIRST_IS_LEFT;
    } else if (max1CompareToMin2 == 0) {
      // [i; j] & [k; l] j == k
      if (min1CompareToMin2 <= 0) {
        // [i; j] & [k; l] && j == k && i < k
        return DiffResult.FIRST_IS_LEFT_OVERLAP;
      }
      throw new AssertionError();
    } else {
      // [i; j] & [k; l] j > k
      if (min1CompareToMin2 < 0) {
        // [i; j] & [k; l] j > k && i < k
        if (max1CompareToMax2 <= 0) {
          // [i; j] & [k; l] j > k && i < k && j <= l
          return DiffResult.FIRST_IS_LEFT_OVERLAP;
        }
        // [i; j] & [k; l] j > k && i < k && j > l
        return DiffResult.FIRST_CONTAINS_SECOND;
      } else if (min1CompareToMin2 == 0) {
        // [i; j] & [k; l] j > k && i == k
        if (max1CompareToMax2 == 0) {
          // [i; j] & [k; l] j > k && i == k && j == l
          return DiffResult.EQUALS;
        } else if (max1CompareToMax2 < 0) {
          // [i; j] & [k; l] j > k && i == k && j < l
          return DiffResult.SECOND_CONTAINS_FIRST;
        } else {
          // [i; j] & [k; l] j > k && i == k && j > l
          if (min1CompareToMax2 == 0) {
            return DiffResult.SECOND_IS_LEFT_OVERLAP;
          }
          return DiffResult.FIRST_CONTAINS_SECOND;
        }
      } else {
        // [i; j] & [k; l] j > k && i > k
        if (min1CompareToMax2 > 0) {
          // [i; j] & [k; l] i > l
          return DiffResult.SECOND_IS_LEFT;
        } else if (min1CompareToMax2 < 0) {
          // [i; j] & [k; l] j > k && i > k && i < l
          if (max1CompareToMax2 < 0) {
            // [i; j] & [k; l] j > k && i > k && i < l && j < l
            return DiffResult.SECOND_CONTAINS_FIRST;
          }
          // [i; j] & [k; l] j > k && i > k && i < l && j >= l
          return DiffResult.SECOND_IS_LEFT_OVERLAP;
        }
        // [i; j] & [k; l] j > k && i > k && i == l
        return DiffResult.SECOND_IS_LEFT_OVERLAP;
      }
    }
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
    DiffResult diff = diff(this, other);
    switch (diff) {
    case FIRST_CONTAINS_SECOND:
    case SECOND_CONTAINS_FIRST:
    case FIRST_IS_LEFT_OVERLAP:
    case SECOND_IS_LEFT_OVERLAP:
    case EQUALS:
      return true;
    default:
      return false;
    }
  }

  @Override
  public Type lessThanOrEqualsValues(Type other, boolean inLoop) {
    if (other instanceof IntType) {
      IntType iType = (IntType) other;
      BigInteger cst = asConstant();
      BigInteger oCst = iType.asConstant();

      DiffResult diff = diff(this, iType);

      if (oCst != null) {
        switch (diff) {
        case EQUALS:
          return this.asNonNull();
        case SECOND_IS_LEFT_OVERLAP:
          if (minBound.equals(oCst)) {
            return other;
          }
        //$FALL-THROUGH$
        case SECOND_IS_LEFT:
          return null;
        case FIRST_IS_LEFT:
          if (!inLoop) {
            return this.asNonNull();
          }
          //$FALL-THROUGH$
        case FIRST_IS_LEFT_OVERLAP:
        case FIRST_CONTAINS_SECOND:
          return new IntType(false, minBound, oCst);
        default:
          throw new IllegalStateException();
        }
      }

      if (cst != null) {
        switch (diff) {
        case EQUALS:
        case FIRST_IS_LEFT_OVERLAP:
          return this.asNonNull();
        case SECOND_IS_LEFT:
        case SECOND_IS_LEFT_OVERLAP:
        case SECOND_CONTAINS_FIRST:
          return null;
        case FIRST_IS_LEFT:
          if (!inLoop) {
            return this.asNonNull();
          }
          return new IntType(false, cst, iType.minBound);
        default:
          throw new IllegalStateException();
        }
      }

      switch (diff) {
      case EQUALS:
        return constant(this.minBound).asNonNull();
      case FIRST_IS_LEFT:
        if (inLoop) {
          return new IntType(false, minBound, iType.minBound);
        }
        return this;
      case SECOND_IS_LEFT:
      case SECOND_IS_LEFT_OVERLAP:
      case SECOND_CONTAINS_FIRST:
        return null;
      case FIRST_CONTAINS_SECOND:
      case FIRST_IS_LEFT_OVERLAP:
        return new IntType(false, minBound, iType.minBound);
      default:
        return VOID_TYPE;
      }
    }

    if (other instanceof DoubleType) {
      BigInteger cst = (BigInteger) other.asConstant();
      if (maxBound != null) {
        if (maxBound.compareTo(cst) <= 0) {
          return this.asNonNull();
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
    if (other instanceof IntType) {
      IntType iType = (IntType) other;
      BigInteger cst = asConstant();
      BigInteger oCst = iType.asConstant();

      DiffResult diff = diff(this, iType);

      if (oCst != null) {
        switch (diff) {
        case EQUALS:
        case SECOND_IS_LEFT:
        case SECOND_IS_LEFT_OVERLAP:
          return null;
        case FIRST_IS_LEFT:
          if (!inLoop) {
            return this.asNonNull();
          }
          //$FALL-THROUGH$
        case FIRST_IS_LEFT_OVERLAP:
        case FIRST_CONTAINS_SECOND:
          return new IntType(false, minBound, oCst.subtract(BigInteger.ONE));
        default:
          throw new IllegalStateException();
        }
      }

      if (cst != null) {
        switch (diff) {
        case EQUALS:
        case SECOND_IS_LEFT:
        case SECOND_CONTAINS_FIRST:
        case SECOND_IS_LEFT_OVERLAP:
        case FIRST_IS_LEFT_OVERLAP:
          return null;
        case FIRST_IS_LEFT:
          if (!inLoop) {
            return this.asNonNull();
          }
          return new IntType(false, cst, iType.minBound.subtract(BigInteger.ONE));
        default:
          throw new IllegalStateException();
        }
      }

      switch (diff) {
      case EQUALS:
        return null;
      case FIRST_IS_LEFT:
        if (inLoop) {
          return new IntType(false, minBound, iType.minBound.subtract(BigInteger.ONE));
        }
        return this;
      case SECOND_IS_LEFT:
      case SECOND_IS_LEFT_OVERLAP:
      case SECOND_CONTAINS_FIRST:
        return null;
      case FIRST_CONTAINS_SECOND:
      case FIRST_IS_LEFT_OVERLAP:
        return new IntType(false, minBound, iType.minBound.subtract(BigInteger.ONE));
      default:
        return VOID_TYPE;
      }
    }

    if (other instanceof DoubleType) {
      BigInteger cst = (BigInteger) other.asConstant();
      if (maxBound != null) {
        if (maxBound.compareTo(cst) < 0) {
          return this.asNonNull();
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
    if (other instanceof IntType) {
      IntType iType = (IntType) other;
      BigInteger cst = asConstant();
      BigInteger oCst = iType.asConstant();

      DiffResult diff = diff(this, iType);

      if (oCst != null) {
        switch (diff) {
        case FIRST_IS_LEFT:
          return null;
        case FIRST_IS_LEFT_OVERLAP:
          return iType;
        case FIRST_CONTAINS_SECOND:
          return new IntType(false, oCst, maxBound);
        case SECOND_IS_LEFT:
          if (inLoop) {
            return new IntType(false, oCst, maxBound);
          }
          //$FALL-THROUGH$
        case SECOND_IS_LEFT_OVERLAP:
        case EQUALS:
          return this;

        default: // SECOND_CONTAINS_FIRST
        }
      }

      if (cst != null) {
        switch (diff) {
        case FIRST_IS_LEFT:
        case FIRST_IS_LEFT_OVERLAP:
        case SECOND_CONTAINS_FIRST:
          return null;
        case SECOND_IS_LEFT:
          if (inLoop) {
            return new IntType(false, iType.maxBound, cst);
          }
          //$FALL-THROUGH$
        case SECOND_IS_LEFT_OVERLAP:
        case EQUALS:
          return this;
        default: // FIRST_CONTAINS_SECOND
        }
      }

      switch (diff) {
      case FIRST_IS_LEFT:
      case FIRST_IS_LEFT_OVERLAP:
      case SECOND_CONTAINS_FIRST:
        return null;
      case EQUALS:
        return constant(maxBound).asNonNull();
      case SECOND_IS_LEFT:
        if (!inLoop) {
          return this;
        }
        //$FALL-THROUGH$
      case FIRST_CONTAINS_SECOND:
      case SECOND_IS_LEFT_OVERLAP:
        return new IntType(false, iType.maxBound, maxBound);
      default:
        return VOID_TYPE;
      }
    }

    if (other instanceof DoubleType) {
      BigInteger cst = (BigInteger) other.asConstant();
      if (maxBound != null) {
        if (maxBound.compareTo(cst) <= 0) {
          return this.asNonNull();
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
  public Type greaterThanValues(Type other, boolean inLoop) {
    if (other instanceof IntType) {
      IntType iType = (IntType) other;
      BigInteger cst = asConstant();
      BigInteger oCst = iType.asConstant();

      DiffResult diff = diff(this, iType);

      if (oCst != null) {
        switch (diff) {
        case EQUALS:
        case FIRST_IS_LEFT:
        case FIRST_IS_LEFT_OVERLAP:
          return null;
        case FIRST_CONTAINS_SECOND:
          return new IntType(false, oCst.add(BigInteger.ONE), maxBound);
        case SECOND_IS_LEFT:
          if (inLoop) {
            return new IntType(false, oCst.add(BigInteger.ONE), maxBound);
          }
          return this;
        case SECOND_IS_LEFT_OVERLAP:
          return new IntType(false, oCst.add(BigInteger.ONE), maxBound);
        default: // SECOND_CONTAINS_FIRST
        }
      }

      if (cst != null) {
        switch (diff) {
        case FIRST_IS_LEFT:
        case FIRST_IS_LEFT_OVERLAP:
        case SECOND_CONTAINS_FIRST:
        case SECOND_IS_LEFT_OVERLAP:
        case EQUALS:
          return null;
        case SECOND_IS_LEFT:
          if (inLoop) {
            return new IntType(false, iType.maxBound.add(BigInteger.ONE), cst);
          }
          return this;
        default: // FIRST_CONTAINS_SECOND
        }
      }

      switch (diff) {
      case FIRST_IS_LEFT:
      case FIRST_IS_LEFT_OVERLAP:
      case SECOND_CONTAINS_FIRST:
      case EQUALS:
        return null;
      case SECOND_IS_LEFT:
        if (!inLoop) {
          return this;
        }
        //$FALL-THROUGH$
      case FIRST_CONTAINS_SECOND:
      case SECOND_IS_LEFT_OVERLAP:
        if (maxBound.equals(iType.maxBound)) {
          return null;
        }
        return new IntType(false, iType.maxBound.add(BigInteger.ONE), maxBound);
      default:
        return VOID_TYPE;
      }
    }

    if (other instanceof DoubleType) {
      BigInteger cst = (BigInteger) other.asConstant();
      if (maxBound != null) {
        if (maxBound.compareTo(cst) <= 0) {
          return this.asNonNull();
        }
      }
      return null;
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).greaterThanOrEqualsValues(this, inLoop);
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
      return constant != null && constant.equals(other.asConstant());
    case FIRST_IS_LEFT_OVERLAP:
      return other.minBound != null && maxBound != null && other.minBound.equals(maxBound);
    default:
      return false;
    }
  }

  @Override
  public Type unarySub() {
    BigInteger min;
    BigInteger max;

    if (minBound != null) {
      max = minBound.negate();
    } else {
      max = null;
    }

    if (maxBound != null) {
      min = maxBound.negate();
    } else {
      min = null;
    }

    if (min == null && max == null) {
      return this;
    }

    return new IntType(isNullable(), min, max);
  }

  @Override
  public Type exclude(Type other) {
    if (other instanceof IntType) {
      return excludeInt((IntType) other);
    }

    if (other instanceof DoubleType) {
      DoubleType dType = (DoubleType) other;
      float floatValue = dType.asConstant().floatValue();
      if (floatValue == (int) floatValue) {
        return excludeInt(constant(BigInteger.valueOf((int) floatValue)));
      }
    }

    if (other instanceof UnionType) {
      return other.exclude(this);
    }

    return null;
  }

  /**
   * Remove type range to this range.
   * 
   * For example if this range is [10; 20] ad type range is [12; 17], the return
   * range will be union([10; 12], [17; 20])
   * 
   * @param iType
   *          Element to remove
   * @return A new type with the right range. Or null if the range is null.
   */
  private Type excludeInt(IntType iType) {
    BigInteger cst = asConstant();
    DiffResult diff = diff(this, iType);

    switch (diff) {
    case EQUALS:
    case SECOND_CONTAINS_FIRST:
      return null;
    case FIRST_IS_LEFT:
    case SECOND_IS_LEFT:
      return this;
    case FIRST_IS_LEFT_OVERLAP:
      if (cst == null) {
        return new IntType(false, minBound, iType.minBound.subtract(BigInteger.ONE));
      }
      return null;
    case SECOND_IS_LEFT_OVERLAP:
      if (cst == null && !maxBound.equals(iType.maxBound)) {
        return new IntType(false, iType.maxBound.add(BigInteger.ONE), maxBound);
      }
      return null;
    case FIRST_CONTAINS_SECOND:
      return Types.union(new IntType(false, minBound, iType.minBound.subtract(BigInteger.ONE)),
          new IntType(false, iType.maxBound.add(BigInteger.ONE), maxBound));

    default:
      throw new IllegalStateException();
    }
  }

  @Override
  public Type add(Type other) {
    if (other instanceof IntType) {
      return addInt((IntType) other);
    }
    
    if (other instanceof DoubleType) {
      return ((DoubleType) other).add(this);
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).add(this);
    }

    return null;
  }

  private Type modInt(IntType other) {
    BigInteger cst = asConstant();
    BigInteger oCst = other.asConstant();

    if (cst != null && oCst != null) {
      BigInteger value = cst.mod(oCst);
      return constant(value);
    }

    if (other.maxBound != null && other.minBound != null) {
      BigInteger val = other.maxBound.subtract(BigInteger.ONE);
      if (BigInteger.ZERO.compareTo(val) < 0) {
        return new IntType(false, BigInteger.ZERO, val);
      }
    }
    return DYNAMIC_TYPE;
  }

  @Override
  public Type mod(Type other) {
    if (other instanceof IntType) {
      return modInt((IntType) other);
    }

    if (other instanceof DoubleType) {
      return ((DoubleType) other).reverseMod(this);
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).reverseMod(this);
    }

    return null;
  }

  @Override
  public Type sub(Type other) {
    if (other instanceof IntType) {
      return subInt((IntType) other);
    }
    
    if (other instanceof DoubleType) {
      return ((DoubleType) other).reverseSub(this);
    }

    if (other instanceof UnionType) {
      return ((UnionType) other).reverseSub(this);
    }

    return null;
  }

  public Type bitAnd(IntType other) {
    if (asConstant() != null && other.asConstant() != null) {
      BigInteger value = asConstant().and(other.asConstant());
      return IntType.constant(value);
    }
    return INT_NON_NULL_TYPE;
  }

  public Type bitOr(IntType other) {
    if (asConstant() != null && other.asConstant() != null) {
      BigInteger value = asConstant().or(other.asConstant());
      return IntType.constant(value);
    }
    return INT_NON_NULL_TYPE;
  }

  public Type bitXor(IntType other) {
    if (asConstant() != null && other.asConstant() != null) {
      BigInteger value = asConstant().xor(other.asConstant());
      return IntType.constant(value);
    }
    return INT_NON_NULL_TYPE;
  }

  @Override
  public boolean isIncludeIn(Type other) {
    if (isNullable() && !other.isNullable()) {
      return false;
    }

    if (other instanceof IntType) {
      IntType iType = (IntType) other;
      DiffResult diff = diff(this, iType);

      switch (diff) {
      case FIRST_IS_LEFT:
      case SECOND_IS_LEFT:
      case FIRST_CONTAINS_SECOND:
        return false;
      case EQUALS:
      case SECOND_CONTAINS_FIRST:
        return true;
      case FIRST_IS_LEFT_OVERLAP:
        if (iType.minBound.compareTo(minBound) <= 0) {
          return true;
        }
        return false;
      case SECOND_IS_LEFT_OVERLAP:
        if (iType.maxBound.compareTo(maxBound) >= 0) {
          return true;
        }
        return false;
        
      default:
        throw new IllegalStateException();
      }
    }

    if (other instanceof DoubleType) {
      Double doubleConstant = ((DoubleType) other).asConstant();
      if (doubleConstant != null) {
        if (asDouble().equals(other)) {
          return true;
        }
        return false;
      }
      return true;
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
    if (other.isNullable() && !isNullable()) {
      return false;
    }

    if (other instanceof DoubleType) {
      DoubleType dType = (DoubleType) other;
      BigDecimal bigDec = BigDecimal.valueOf(dType.asConstant());
      try {
        BigInteger bigInt = bigDec.toBigIntegerExact();
        other = new IntType(other.isNullable(), bigInt, bigInt);
      } catch (ArithmeticException e) {
        BigInteger bigInt = bigDec.toBigInteger();
        other = new IntType(other.isNullable(), bigInt, bigInt.add(BigInteger.ONE));
      }
    }

    if (other instanceof IntType) {
      IntType iType = (IntType) other;
      DiffResult diff = diff(this, iType);

      switch (diff) {
      case FIRST_IS_LEFT_OVERLAP:
        if (maxBound != null && maxBound.compareTo(iType.maxBound) >= 0) {
          return true;
        }
        return false;
      case FIRST_CONTAINS_SECOND:
      case EQUALS:
        return true;
      default:
        return false;
      }
    }

    if (other instanceof NullType) {
      return true;
    }

    return false;
  }

  /**
   * Return a IntType whose value is (~constant).
   * @return {@link IntType} with constant value or INT_NON_NULL_TYPE
   */
  public Type bitNot() {
    BigInteger constant = asConstant();
    if (constant != null) {
      return constant(constant.not());
    }
    return INT_NON_NULL_TYPE;
  }
}
