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
    Assert.assertTrue(Types.BOOL_TYPE.asNullable().isNullable());
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
    Assert.assertTrue(Types.INT_TYPE.asNullable().isNullable());
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
    Assert.assertTrue(Types.DOUBLE_TYPE.asNullable().isNullable());
  }
  
  @Test
  public void nullAsNullable() {
    Assert.assertEquals(Types.NULL_TYPE, Types.NULL_TYPE.asNullable());
    Assert.assertTrue(Types.NULL_TYPE.asNullable().isNullable());
  }
  
  
}
