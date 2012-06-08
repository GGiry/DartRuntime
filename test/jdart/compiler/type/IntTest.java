package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

public class IntTest {
  private IntType range(Integer min, Integer max) {
    if (min != null && max != null) {
      return INT_NON_NULL_TYPE.asTypeGreaterOrEqualsThan(BigInteger.valueOf(min)).asTypeLessOrEqualsThan(BigInteger.valueOf(max));
    }
    if (min != null) {
      return INT_NON_NULL_TYPE.asTypeGreaterOrEqualsThan(BigInteger.valueOf(min));
    }
    if (max != null) {
      return INT_NON_NULL_TYPE.asTypeLessOrEqualsThan(BigInteger.valueOf(max));
    }
    return INT_NON_NULL_TYPE;
  }

  @Test
  public void intGreaterThanOrEqualsTest1() {
    IntType int1 = range(2, 3);
    IntType int2 = range(1, 1);
    IntType int3 = range(2, 2);
    IntType int4 = range(4, 5);
    IntType int5 = range(4, 4);

    IntType expected = null;

    Assert.assertEquals(expected, int2.greaterThanOrEqualsValues(int1, true));
    Assert.assertEquals(expected, int2.greaterThanOrEqualsValues(int1, false));

    Assert.assertEquals(expected, int3.greaterThanOrEqualsValues(int4, true));
    Assert.assertEquals(expected, int3.greaterThanOrEqualsValues(int4, false));

    Assert.assertEquals(expected, int1.greaterThanOrEqualsValues(int4, true));
    Assert.assertEquals(expected, int1.greaterThanOrEqualsValues(int4, false));

    Assert.assertEquals(expected, int3.greaterThanOrEqualsValues(int5, true));
    Assert.assertEquals(expected, int3.greaterThanOrEqualsValues(int5, false));
  }

  @Test
  public void intGreaterThanOrEqualsTest2() {
    IntType int1 = range(2, 3);
    IntType int2 = range(5, 5);
    IntType int3 = range(4, 7);
    IntType int4 = range(1, 6);
    IntType int5 = range(3, 5);
    IntType int6 = range(1, 1);

    IntType expected = null;

    Assert.assertEquals(expected, int1.greaterThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected, int1.greaterThanOrEqualsValues(int2, false));

    Assert.assertEquals(expected, int1.greaterThanOrEqualsValues(int3, true));
    Assert.assertEquals(expected, int1.greaterThanOrEqualsValues(int3, false));

    Assert.assertEquals(expected, int6.greaterThanOrEqualsValues(int4, true));
    Assert.assertEquals(expected, int6.greaterThanOrEqualsValues(int4, false));

    Assert.assertEquals(expected, int1.greaterThanOrEqualsValues(int5, true));
    Assert.assertEquals(expected, int1.greaterThanOrEqualsValues(int5, false));
  }

  @Test
  public void intGreaterThanOrEqualsTest3() {
    IntType int1 = range(5, 7);
    IntType int2 = range(2, 2);
    IntType int3 = range(-6, -3);
    IntType int4 = range(0, 0);
    IntType int5 = range(0, 3);

    IntType expectedTrue1   = range(2, 7);
    IntType expectedFalse1  = range(5, 7);
    IntType expectedTrue2   = range(-3, 2);
    IntType expectedFalse2  = range(2, 2);
    IntType expectedTrue3   = range(0, 2);
    IntType expectedFalse3  = range(2, 2);
    IntType expectedTrue4   = range(3, 7);
    IntType expectedFalse4  = range(5, 7);

    Assert.assertEquals(expectedTrue1, int1.greaterThanOrEqualsValues(int2, true));
    Assert.assertEquals(expectedFalse1, int1.greaterThanOrEqualsValues(int2, false));

    Assert.assertEquals(expectedTrue2, int2.greaterThanOrEqualsValues(int3, true));
    Assert.assertEquals(expectedFalse2, int2.greaterThanOrEqualsValues(int3, false));

    Assert.assertEquals(expectedTrue3, int2.greaterThanOrEqualsValues(int4, true));
    Assert.assertEquals(expectedFalse3, int2.greaterThanOrEqualsValues(int4, false));

    Assert.assertEquals(expectedTrue4, int1.greaterThanOrEqualsValues(int5, true));
    Assert.assertEquals(expectedFalse4, int1.greaterThanOrEqualsValues(int5, false));
  }
  
  @Test
  public void intGreaterThanOrEqualsTest4() {
    IntType int1 = range(3, 4);
    IntType int2 = range(3, 3);
    IntType int3 = range(5, 5);
    IntType int4 = range(1, 5);
    IntType int5 = range(6, 9);
    IntType int6 = range(1, 7);
    IntType int7 = range(5, 7);

    IntType expected1 = range(3, 4);
    IntType expected2 = range(5, 5);
    IntType expected3 = range(7, 9);
    IntType expected4 = range(7, 7);

    Assert.assertEquals(expected1, int1.greaterThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected1, int1.greaterThanOrEqualsValues(int2, false));

    Assert.assertEquals(expected2, int3.greaterThanOrEqualsValues(int4, true));
    Assert.assertEquals(expected2, int3.greaterThanOrEqualsValues(int4, false));

    Assert.assertEquals(expected3, int5.greaterThanOrEqualsValues(int6, true));
    Assert.assertEquals(expected3, int5.greaterThanOrEqualsValues(int6, false));

    Assert.assertEquals(expected4, int7.greaterThanOrEqualsValues(int6, true));
    Assert.assertEquals(expected4, int7.greaterThanOrEqualsValues(int6, false));
  }
}
