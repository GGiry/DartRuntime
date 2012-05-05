package type;

import java.math.BigInteger;

import junit.framework.Assert;

import org.junit.Test;

public class TypeTest {
  @Test
  public void nullableByDefaults() {
    Assert.assertTrue(Types.BOOL_TYPE.isNullable());
    Assert.assertTrue(Types.INT_TYPE.isNullable());
    Assert.assertTrue(Types.DOUBLE_TYPE.isNullable());
    Assert.assertTrue(Types.NULL_TYPE.isNullable());
  }
  
  @Test
  public void nonNullByDefaults() {
    Assert.assertFalse(Types.BOOL_NON_NULL_TYPE.isNullable());
    Assert.assertFalse(Types.INT_NON_NULL_TYPE.isNullable());
    Assert.assertFalse(Types.DOUBLE_NON_NULL_TYPE.isNullable());
  }
  
  @Test
  public void nullableStillConst() {
    Assert.assertEquals(Types.TRUE.asNullable().asConstant(), true);
    Assert.assertEquals(Types.FALSE.asNullable().asConstant(), false);
    Assert.assertEquals(IntType.constant(BigInteger.TEN).asNullable().asConstant(), BigInteger.TEN);
    Assert.assertEquals(DoubleType.constant(42.0).asNullable().asConstant(), 42.0);
    Assert.assertEquals(Types.NULL_TYPE.asNullable().asConstant(), Type.NULL_VALUE);
  }
  
  @Test
  public void nonNullStillConst() {
    Assert.assertEquals(Types.TRUE.asNonNull().asConstant(), true);
    Assert.assertEquals(Types.FALSE.asNonNull().asConstant(), false);
    Assert.assertEquals(IntType.constant(BigInteger.TEN).asNonNull().asConstant(), BigInteger.TEN);
    Assert.assertEquals(DoubleType.constant(42.0).asNonNull().asConstant(), 42.0);
  }
  
  @Test(expected=IllegalStateException.class)
  public void nullAsNonNull() {
    Types.NULL_TYPE.asNonNull();
  }
  
  @Test
  public void constBoolean() {
    Assert.assertTrue(Types.TRUE.asConstant());
    Assert.assertTrue(!Types.TRUE.isNullable());
    Assert.assertTrue(Types.TRUE.asNullable().isNullable());
    Assert.assertFalse(Types.FALSE.asConstant());
    Assert.assertTrue(!Types.FALSE.isNullable());
    Assert.assertTrue(Types.FALSE.asNullable().isNullable());
  }
  
  @Test
  public void booleanAsNullable() {
    Assert.assertEquals(Types.BOOL_TYPE, Types.BOOL_TYPE.asNullable());
    Assert.assertEquals(Types.BOOL_TYPE, Types.BOOL_NON_NULL_TYPE.asNullable());
    Assert.assertTrue(Types.BOOL_TYPE.asNullable().isNullable());
    Assert.assertTrue(Types.BOOL_NON_NULL_TYPE.asNullable().isNullable());
  }
  
  @Test
  public void booleanAsNonNull() {
    Assert.assertEquals(Types.BOOL_NON_NULL_TYPE, Types.BOOL_TYPE.asNonNull());
    Assert.assertEquals(Types.BOOL_NON_NULL_TYPE, Types.BOOL_NON_NULL_TYPE.asNonNull());
    Assert.assertFalse(Types.BOOL_TYPE.asNonNull().isNullable());
    Assert.assertFalse(Types.BOOL_NON_NULL_TYPE.asNonNull().isNullable());
  }
  
  @Test
  public void constInt() {
    Assert.assertEquals(BigInteger.ZERO, IntType.constant(BigInteger.ZERO).asConstant());
    Assert.assertEquals(BigInteger.ONE, IntType.constant(BigInteger.ONE).asConstant());
    Assert.assertEquals(BigInteger.valueOf(42), IntType.constant(BigInteger.valueOf(42)).asConstant());
  }
  
  @Test
  public void intAsNullable() {
    Assert.assertEquals(Types.INT_TYPE, Types.INT_TYPE.asNullable());
    Assert.assertEquals(Types.INT_TYPE, Types.INT_NON_NULL_TYPE.asNullable());
    Assert.assertTrue(Types.INT_TYPE.asNullable().isNullable());
    Assert.assertTrue(Types.INT_NON_NULL_TYPE.asNullable().isNullable());
    Assert.assertTrue(IntType.constant(BigInteger.ONE).asNullable().isNullable());
  }
  
  @Test
  public void intAsNonNull() {
    Assert.assertEquals(Types.INT_NON_NULL_TYPE, Types.INT_TYPE.asNonNull());
    Assert.assertEquals(Types.INT_NON_NULL_TYPE, Types.INT_NON_NULL_TYPE.asNonNull());
    Assert.assertFalse(Types.INT_TYPE.asNonNull().isNullable());
    Assert.assertFalse(Types.INT_NON_NULL_TYPE.asNonNull().isNullable());
    Assert.assertFalse(IntType.constant(BigInteger.ONE).asNonNull().isNullable());
  }
  
  @Test
  public void intBounds() {
    Assert.assertTrue(Types.INT_TYPE.isMinBoundInfinity());
    Assert.assertTrue(Types.INT_TYPE.isMaxBoundInfinity());
    
    Assert.assertEquals(BigInteger.ONE, Types.INT_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ONE).getMinBound());
    Assert.assertEquals(BigInteger.TEN, Types.INT_TYPE.asTypeLessOrEqualsThan(BigInteger.TEN).getMaxBound());
    
    Assert.assertEquals(BigInteger.ZERO, Types.INT_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ZERO).
                                                        asTypeLessOrEqualsThan(BigInteger.ZERO).
                                                        asConstant());
    
    Assert.assertEquals(BigInteger.TEN, Types.INT_TYPE.asTypeLessOrEqualsThan(BigInteger.TEN).
                                                       asTypeGreaterOrEqualsThan(BigInteger.TEN).
                                                       asConstant());
    
    Assert.assertEquals(BigInteger.ONE, Types.INT_TYPE.asTypeLessOrEqualsThan(BigInteger.TEN).
                                                       asTypeLessOrEqualsThan(BigInteger.ONE).
                                                       getMaxBound());
    
    Assert.assertEquals(BigInteger.ONE, Types.INT_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ZERO).
                                                       asTypeGreaterOrEqualsThan(BigInteger.ONE).
                                                       getMinBound());
  }
  
  @Test
  public void consDouble() {
    Assert.assertEquals(0.0, DoubleType.constant(0.0).asConstant());
    Assert.assertEquals(42.0, DoubleType.constant(42.0).asConstant());
    Assert.assertTrue(DoubleType.constant(Double.NaN).asConstant().isNaN());
  }
  
  @Test
  public void doubleAsNullable() {
    Assert.assertEquals(Types.DOUBLE_TYPE, Types.DOUBLE_TYPE.asNullable());
    Assert.assertEquals(Types.DOUBLE_TYPE, Types.DOUBLE_NON_NULL_TYPE.asNullable());
    Assert.assertTrue(Types.DOUBLE_TYPE.asNullable().isNullable());
    Assert.assertTrue(Types.DOUBLE_NON_NULL_TYPE.asNullable().isNullable());
    Assert.assertTrue(DoubleType.constant(777.0).asNullable().isNullable());
  }
  
  @Test
  public void doubleAsNonNull() {
    Assert.assertEquals(Types.DOUBLE_NON_NULL_TYPE, Types.DOUBLE_TYPE.asNonNull());
    Assert.assertEquals(Types.DOUBLE_NON_NULL_TYPE, Types.DOUBLE_NON_NULL_TYPE.asNonNull());
    Assert.assertFalse(Types.DOUBLE_TYPE.asNonNull().isNullable());
    Assert.assertFalse(Types.DOUBLE_NON_NULL_TYPE.asNonNull().isNullable());
    Assert.assertFalse(DoubleType.constant(345.0).asNonNull().isNullable());
  }
  
  @Test
  public void nullAsNullable() {
    Assert.assertEquals(Types.NULL_TYPE, Types.NULL_TYPE.asNullable());
    Assert.assertTrue(Types.NULL_TYPE.asNullable().isNullable());
  }
}
