package type;

import java.math.BigInteger;
import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

public class IntType extends PrimitiveType {
  private final BigInteger minBound;
  private final BigInteger maxBound;
  
	IntType(boolean nullable, /*maybenull*/BigInteger minBound, /*maybenull*/BigInteger maxBound) {
	  super(nullable);
	  this.minBound = minBound;
	  this.maxBound = (Objects.equals(minBound, maxBound))? minBound: maxBound;  // be sure that if the type is constant min == max
	}
	
	public static IntType constant(BigInteger constant) {
	  Objects.requireNonNull(constant);
    return new IntType(false, constant, constant);
  }
	
	@Override
  public int hashCode() {
    return (isNullable()?1 : 0) ^ Objects.hashCode(minBound) ^ Objects.hashCode(maxBound); 
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IntType)) {
      return false;
    }
    IntType intType = (IntType)obj;
    return isNullable() == intType.isNullable() &&
           Objects.equals(minBound, intType.minBound) &&
           Objects.equals(maxBound, intType.maxBound);
  }
  
  @Override
  public String getName() {
    return "int";
  }
  
  @Override
  public String toString() {
    return super.toString() + " ["+infinity('-', minBound)+','+infinity('+', maxBound)+']';
  }
  
  @Override
  ClassElement getLazyElement() {
    return CoreTypeRepository.getCoreTypeRepository().getIntClassElement();
  }
  
  private static String infinity(char sign, BigInteger value) {
    return (value == null)? sign + "infinity": value.toString();
  }
	
	/**
	 * Return the minimum bound or null if the bound is -Infinity.
	 * @return the minimum bound.
	 */
	public /*maybenull*/BigInteger getMinBound() {
    return minBound;
  }
	
	/**
   * Return the maximum bound or null if the bound is +Infinity.
   * @return the maximum bound.
   */
	public /*maybenull*/BigInteger getMaxBound() {
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
	    return Types.INT_TYPE;
	  }
	  return new IntType(true, minBound, maxBound);
	}
	
	@Override
	public IntType asNonNull() {
	  if (!isNullable()) {
	    return this;
	  }
	  if (minBound == null && maxBound == null) {
      return Types.INT_NON_NULL_TYPE;
    }
    return new IntType(false, minBound, maxBound);
	}
	
	@Override
  public <R,P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitIntType(this, parameter);
  }
	
	@Override
	public BigInteger asConstant() {
	  if (minBound == maxBound) {
	    return minBound;
	  }
	  return null;
	}
	
	//
  //  int[min, max] x = ...
  //  if (x <= value) {
  //  
	public /*maybenull*/IntType asTypeLessOrEqualsThan(BigInteger value) {
	  if (maxBound == null || value.compareTo(maxBound) <= 0) {
	    if (minBound != null && value.compareTo(minBound) < 0) {
	      return null;  
      }
	    return new IntType(isNullable(), minBound, value);  
	  }
	  return this;
	}
	
	//
  //  int[min, max] x = ...
  //  if (x < value) {
  //  
  public /*maybenull*/IntType asTypeLessThan(BigInteger value) {
    return asTypeLessOrEqualsThan(value.add(BigInteger.ONE));
  }
	
	//
  //  int[min, max] x = ...
  //  if (x >= value) {
  //  
  public /*maybenull*/IntType asTypeGreaterOrEqualsThan(BigInteger value) {
    if (minBound == null || value.compareTo(minBound) >= 0) {
      if (maxBound != null && value.compareTo(maxBound) > 0) {
        return null;
      }
      return new IntType(isNullable(), value, maxBound);
    }
    return this;
  }
  
  //
  //  int[min, max] x = ...
  //  if (x > value) {
  //  
  public /*maybenull*/IntType asTypeGreaterThan(BigInteger value) {
    return asTypeGreaterOrEqualsThan(value.subtract(BigInteger.ONE));
  }
}
