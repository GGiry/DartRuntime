package type;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import static type.CoreTypeRepository.*;

public class TypeTest {
  @Test
  public void nullableByDefaults() {
    Assert.assertTrue(BOOL_TYPE.isNullable());
    Assert.assertTrue(INT_TYPE.isNullable());
    Assert.assertTrue(DOUBLE_TYPE.isNullable());
    Assert.assertTrue(NULL_TYPE.isNullable());
  }

  @Test
  public void nonNullByDefaults() {
    Assert.assertFalse(BOOL_NON_NULL_TYPE.isNullable());
    Assert.assertFalse(INT_NON_NULL_TYPE.isNullable());
    Assert.assertFalse(DOUBLE_NON_NULL_TYPE.isNullable());
  }

  @Test
  public void nullableStillConst() {
    Assert.assertEquals(TRUE.asNullable().asConstant(), true);
    Assert.assertEquals(FALSE.asNullable().asConstant(), false);
    Assert.assertEquals(IntType.constant(BigInteger.TEN).asNullable().asConstant(), BigInteger.TEN);
    Assert.assertEquals(DoubleType.constant(42.0).asNullable().asConstant(), (Double)42.0);
    Assert.assertEquals(NULL_TYPE.asNullable().asConstant(), Type.NULL_VALUE);
  }

  @Test
  public void nonNullStillConst() {
    Assert.assertEquals(TRUE.asNonNull().asConstant(), true);
    Assert.assertEquals(FALSE.asNonNull().asConstant(), false);
    Assert.assertEquals(IntType.constant(BigInteger.TEN).asNonNull().asConstant(), BigInteger.TEN);
    Assert.assertEquals(DoubleType.constant(42.0).asNonNull().asConstant(), (Double)42.0);
  }

  @Test(expected = IllegalStateException.class)
  public void nullAsNonNull() {
    NULL_TYPE.asNonNull();
  }

  @Test
  public void constBoolean() {
    Assert.assertTrue(TRUE.asConstant());
    Assert.assertTrue(!TRUE.isNullable());
    Assert.assertTrue(TRUE.asNullable().isNullable());
    Assert.assertFalse(FALSE.asConstant());
    Assert.assertTrue(!FALSE.isNullable());
    Assert.assertTrue(FALSE.asNullable().isNullable());
  }

  @Test
  public void booleanAsNullable() {
    Assert.assertEquals(BOOL_TYPE, BOOL_TYPE.asNullable());
    Assert.assertEquals(BOOL_TYPE, BOOL_NON_NULL_TYPE.asNullable());
    Assert.assertTrue(BOOL_TYPE.asNullable().isNullable());
    Assert.assertTrue(BOOL_NON_NULL_TYPE.asNullable().isNullable());
  }

  @Test
  public void booleanAsNonNull() {
    Assert.assertEquals(BOOL_NON_NULL_TYPE, BOOL_TYPE.asNonNull());
    Assert.assertEquals(BOOL_NON_NULL_TYPE, BOOL_NON_NULL_TYPE.asNonNull());
    Assert.assertFalse(BOOL_TYPE.asNonNull().isNullable());
    Assert.assertFalse(BOOL_NON_NULL_TYPE.asNonNull().isNullable());
  }

  @Test
  public void constInt() {
    Assert.assertEquals(BigInteger.ZERO, IntType.constant(BigInteger.ZERO).asConstant());
    Assert.assertEquals(BigInteger.ONE, IntType.constant(BigInteger.ONE).asConstant());
    Assert.assertEquals(BigInteger.valueOf(42), IntType.constant(BigInteger.valueOf(42)).asConstant());
  }

  @Test
  public void intAsNullable() {
    Assert.assertEquals(INT_TYPE, INT_TYPE.asNullable());
    Assert.assertEquals(INT_TYPE, INT_NON_NULL_TYPE.asNullable());
    Assert.assertTrue(INT_TYPE.asNullable().isNullable());
    Assert.assertTrue(INT_NON_NULL_TYPE.asNullable().isNullable());
    Assert.assertTrue(IntType.constant(BigInteger.ONE).asNullable().isNullable());
  }

  @Test
  public void intAsNonNull() {
    Assert.assertEquals(INT_NON_NULL_TYPE, INT_TYPE.asNonNull());
    Assert.assertEquals(INT_NON_NULL_TYPE, INT_NON_NULL_TYPE.asNonNull());
    Assert.assertFalse(INT_TYPE.asNonNull().isNullable());
    Assert.assertFalse(INT_NON_NULL_TYPE.asNonNull().isNullable());
    Assert.assertFalse(IntType.constant(BigInteger.ONE).asNonNull().isNullable());
  }

  @Test
  public void intBounds() {
    Assert.assertTrue(INT_TYPE.isMinBoundInfinity());
    Assert.assertTrue(INT_TYPE.isMaxBoundInfinity());

    Assert.assertEquals(BigInteger.ONE, INT_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ONE).getMinBound());
    Assert.assertEquals(BigInteger.TEN, INT_TYPE.asTypeLessOrEqualsThan(BigInteger.TEN).getMaxBound());

    Assert.assertEquals(BigInteger.ZERO, INT_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ZERO).asTypeLessOrEqualsThan(BigInteger.ZERO).asConstant());

    Assert.assertEquals(BigInteger.TEN, INT_TYPE.asTypeLessOrEqualsThan(BigInteger.TEN).asTypeGreaterOrEqualsThan(BigInteger.TEN).asConstant());

    Assert.assertEquals(BigInteger.ONE, INT_TYPE.asTypeLessOrEqualsThan(BigInteger.TEN).asTypeLessOrEqualsThan(BigInteger.ONE).getMaxBound());

    Assert.assertEquals(BigInteger.ONE, INT_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ZERO).asTypeGreaterOrEqualsThan(BigInteger.ONE).getMinBound());
  }

  @Test
  public void consDouble() {
    Assert.assertEquals((Double)0.0, DoubleType.constant(0.0).asConstant());
    Assert.assertEquals((Double)42.0, DoubleType.constant(42.0).asConstant());
    Assert.assertTrue(DoubleType.constant(Double.NaN).asConstant().isNaN());
  }

  @Test
  public void doubleAsNullable() {
    Assert.assertEquals(DOUBLE_TYPE, DOUBLE_TYPE.asNullable());
    Assert.assertEquals(DOUBLE_TYPE, DOUBLE_NON_NULL_TYPE.asNullable());
    Assert.assertTrue(DOUBLE_TYPE.asNullable().isNullable());
    Assert.assertTrue(DOUBLE_NON_NULL_TYPE.asNullable().isNullable());
    Assert.assertTrue(DoubleType.constant(777.0).asNullable().isNullable());
  }

  @Test
  public void doubleAsNonNull() {
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, DOUBLE_TYPE.asNonNull());
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, DOUBLE_NON_NULL_TYPE.asNonNull());
    Assert.assertFalse(DOUBLE_TYPE.asNonNull().isNullable());
    Assert.assertFalse(DOUBLE_NON_NULL_TYPE.asNonNull().isNullable());
    Assert.assertFalse(DoubleType.constant(345.0).asNonNull().isNullable());
  }

  @Test
  public void nullAsNullable() {
    Assert.assertEquals(NULL_TYPE, NULL_TYPE.asNullable());
    Assert.assertTrue(NULL_TYPE.asNullable().isNullable());
  }
}
