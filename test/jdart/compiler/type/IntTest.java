package jdart.compiler.type;

import static jdart.compiler.type.IntType.*;
import static jdart.compiler.type.CoreTypeRepository.*;
import static jdart.compiler.type.IntType.DiffResult.*;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

public class IntTest {
  static IntType setNullable(IntType type, boolean nullable) {
    return nullable ? type.asNullable() : type.asNonNull();
  }

  static IntType range(boolean nullable, Integer min, Integer max) {
    IntType res;
    if (min != null && max != null) {
      res = INT_NON_NULL_TYPE.asTypeGreaterOrEqualsThan(BigInteger.valueOf(min)).asTypeLessOrEqualsThan(BigInteger.valueOf(max));
    } else if (min != null) {
      res = INT_NON_NULL_TYPE.asTypeGreaterOrEqualsThan(BigInteger.valueOf(min));
    } else if (max != null) {
      res = INT_NON_NULL_TYPE.asTypeLessOrEqualsThan(BigInteger.valueOf(max));
    } else {
      res = INT_NON_NULL_TYPE;
    }

    return setNullable(res, nullable);
  }

  private static IntType range(Integer min, Integer max) {
    return range(false, min, max);
  }

  @Test
  public void intGreaterThanOrEqualsTest1() {
    IntType int1 = range(2, 3);
    IntType int2 = range(1, 1);
    IntType int3 = range(2, 2);
    IntType int4 = range(4, 5);
    IntType int5 = range(4, 4);
    IntType int6 = range(8, 8);

    IntType expected = null;

    Assert.assertEquals(expected, int2.greaterThanOrEqualsValues(int1, true));
    Assert.assertEquals(expected, int2.greaterThanOrEqualsValues(int1, false));

    Assert.assertEquals(expected, int4.greaterThanOrEqualsValues(int6, true));
    Assert.assertEquals(expected, int4.greaterThanOrEqualsValues(int6, false));

    Assert.assertEquals(expected, int1.greaterThanOrEqualsValues(int4, true));
    Assert.assertEquals(expected, int1.greaterThanOrEqualsValues(int4, false));

    Assert.assertEquals(expected, int3.greaterThanOrEqualsValues(int5, true));
    Assert.assertEquals(expected, int3.greaterThanOrEqualsValues(int5, false));
  }

  @Test
  public void intGreaterThanOrEqualsTest2() {
    IntType int1 = range(2, 5);
    IntType int2 = range(5, 5);
    IntType int3 = range(4, 7);
    IntType int4 = range(1, 6);
    IntType int5 = range(3, 5);
    IntType int6 = range(1, 1);

    IntType expectedNull = null;
    IntType expected1 = range(5, 5);

    Assert.assertEquals(expected1, int1.greaterThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected1, int1.greaterThanOrEqualsValues(int2, false));

    Assert.assertEquals(expectedNull, int1.greaterThanOrEqualsValues(int3, true));
    Assert.assertEquals(expectedNull, int1.greaterThanOrEqualsValues(int3, false));

    Assert.assertEquals(expectedNull, int6.greaterThanOrEqualsValues(int4, true));
    Assert.assertEquals(expectedNull, int6.greaterThanOrEqualsValues(int4, false));

    Assert.assertEquals(expectedNull, int1.greaterThanOrEqualsValues(int5, true));
    Assert.assertEquals(expectedNull, int1.greaterThanOrEqualsValues(int5, false));
  }

  @Test
  public void intGreaterThanOrEqualsTest3() {
    IntType int1 = range(5, 7);
    IntType int2 = range(2, 2);
    IntType int3 = range(-6, -3);
    IntType int4 = range(0, 0);
    IntType int5 = range(0, 3);

    IntType expectedTrue1 = range(2, 7);
    IntType expectedFalse1 = range(5, 7);
    IntType expectedTrue2 = range(-3, 2);
    IntType expectedFalse2 = range(2, 2);
    IntType expectedTrue3 = range(0, 2);
    IntType expectedFalse3 = range(2, 2);
    IntType expectedTrue4 = range(3, 7);
    IntType expectedFalse4 = range(5, 7);

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

  @Test
  public void intGreaterThanOrEqualsTest5() {
    IntType int1 = range(3, 6);
    IntType int2 = range(4, 4);
    IntType int3 = range(4, 5);

    IntType expected1 = range(4, 6);
    IntType expected2 = range(5, 6);

    Assert.assertEquals(expected1, int1.greaterThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected1, int1.greaterThanOrEqualsValues(int2, false));

    Assert.assertEquals(expected2, int1.greaterThanOrEqualsValues(int3, true));
    Assert.assertEquals(expected2, int1.greaterThanOrEqualsValues(int3, false));
  }

  @Test
  public void intGreaterThanOrEqualsTest6() {
    IntType int1 = range(3, 3);
    IntType int2 = range(1, 6);
    IntType int3 = range(3, 5);

    IntType expected = null;

    Assert.assertEquals(expected, int1.greaterThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected, int1.greaterThanOrEqualsValues(int2, false));

    Assert.assertEquals(expected, int3.greaterThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected, int3.greaterThanOrEqualsValues(int2, false));
  }

  @Test
  public void intGreaterThanOrEqualsTest7() {
    IntType int1 = range(3, 3);
    IntType int2 = range(3, 5);

    IntType expected1 = range(3, 3);
    IntType expected2 = range(5, 5);

    Assert.assertEquals(expected1, int1.greaterThanOrEqualsValues(int1, true));
    Assert.assertEquals(expected1, int1.greaterThanOrEqualsValues(int1, false));

    Assert.assertEquals(expected2, int2.greaterThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected2, int2.greaterThanOrEqualsValues(int2, false));
  }

  @Test
  public void diffEqualsTest() {
    Assert.assertEquals(EQUALS, diff(range(3, 3), range(3, 3)));
    Assert.assertEquals(EQUALS, diff(range(3, null), range(3, null)));
    Assert.assertEquals(EQUALS, diff(range(null, 3), range(null, 3)));
    Assert.assertEquals(EQUALS, diff(range(null, null), range(null, null)));
  }

  @Test
  public void diffFirstIsLeft() {
    Assert.assertEquals(FIRST_IS_LEFT, diff(range(3, 5), range(6, 8)));
    Assert.assertEquals(FIRST_IS_LEFT, diff(range(null, 5), range(6, 8)));
    Assert.assertEquals(FIRST_IS_LEFT, diff(range(null, 5), range(6, null)));
    Assert.assertEquals(FIRST_IS_LEFT, diff(range(3, 5), range(6, null)));
  }

  @Test
  public void diffFirstIsLeftOvertlap() {
    Assert.assertEquals(FIRST_IS_LEFT_OVERLAP, diff(range(3, 5), range(4, 8)));
    Assert.assertEquals(FIRST_IS_LEFT_OVERLAP, diff(range(3, 5), range(5, 8)));
    Assert.assertEquals(FIRST_IS_LEFT_OVERLAP, diff(range(3, 5), range(5, 5)));
    Assert.assertEquals(FIRST_IS_LEFT_OVERLAP, diff(range(null, 5), range(5, 5)));
    Assert.assertEquals(FIRST_IS_LEFT_OVERLAP, diff(range(null, 5), range(4, 8)));
    Assert.assertEquals(FIRST_IS_LEFT_OVERLAP, diff(range(null, 5), range(5, 8)));
    Assert.assertEquals(FIRST_IS_LEFT_OVERLAP, diff(range(null, 5), range(5, null)));
    Assert.assertEquals(FIRST_IS_LEFT_OVERLAP, diff(range(null, 5), range(4, null)));
    Assert.assertEquals(FIRST_IS_LEFT_OVERLAP, diff(range(3, 5), range(5, null)));
    Assert.assertEquals(FIRST_IS_LEFT_OVERLAP, diff(range(3, 5), range(4, null)));
    Assert.assertEquals(FIRST_IS_LEFT_OVERLAP, diff(range(null, null), range(5, null)));
  }

  @Test
  public void diffSecondIsLeft() {
    Assert.assertEquals(SECOND_IS_LEFT, diff(range(7, 9), range(2, 6)));
    Assert.assertEquals(SECOND_IS_LEFT, diff(range(7, null), range(null, 6)));
    Assert.assertEquals(SECOND_IS_LEFT, diff(range(7, null), range(2, 6)));
    Assert.assertEquals(SECOND_IS_LEFT, diff(range(7, 9), range(null, 6)));
  }

  @Test
  public void diffSecondIsLeftOvertlap() {
    Assert.assertEquals(SECOND_IS_LEFT_OVERLAP, diff(range(7, 9), range(2, 8)));
    Assert.assertEquals(SECOND_IS_LEFT_OVERLAP, diff(range(8, 9), range(2, 8)));
    Assert.assertEquals(SECOND_IS_LEFT_OVERLAP, diff(range(7, 9), range(7, 7)));
    Assert.assertEquals(SECOND_IS_LEFT_OVERLAP, diff(range(7, null), range(7, 7)));
    Assert.assertEquals(SECOND_IS_LEFT_OVERLAP, diff(range(7, null), range(2, 7)));
    Assert.assertEquals(SECOND_IS_LEFT_OVERLAP, diff(range(7, null), range(2, 8)));
    Assert.assertEquals(SECOND_IS_LEFT_OVERLAP, diff(range(7, null), range(null, 7)));
    Assert.assertEquals(SECOND_IS_LEFT_OVERLAP, diff(range(7, null), range(null, 8)));
    Assert.assertEquals(SECOND_IS_LEFT_OVERLAP, diff(range(7, 9), range(null, 7)));
    Assert.assertEquals(SECOND_IS_LEFT_OVERLAP, diff(range(7, 9), range(null, 8)));
    Assert.assertEquals(SECOND_IS_LEFT_OVERLAP, diff(range(7, null), range(null, null)));
  }

  @Test
  public void diffFirstContainsSecond() {
    Assert.assertEquals(FIRST_CONTAINS_SECOND, diff(range(0, 10), range(2, 8)));
    Assert.assertEquals(FIRST_CONTAINS_SECOND, diff(range(null, null), range(2, 8)));
    Assert.assertEquals(FIRST_CONTAINS_SECOND, diff(range(null, 10), range(2, 8)));
    Assert.assertEquals(FIRST_CONTAINS_SECOND, diff(range(0, null), range(2, 8)));
    Assert.assertEquals(FIRST_CONTAINS_SECOND, diff(range(null, 10), range(null, 8)));
    Assert.assertEquals(FIRST_CONTAINS_SECOND, diff(range(0, null), range(2, null)));
  }

  @Test
  public void diffSecondContainsFirst() {
    Assert.assertEquals(SECOND_CONTAINS_FIRST, diff(range(4, 6), range(2, 8)));
    Assert.assertEquals(SECOND_CONTAINS_FIRST, diff(range(4, 6), range(null, null)));
    Assert.assertEquals(SECOND_CONTAINS_FIRST, diff(range(4, 6), range(null, 8)));
    Assert.assertEquals(SECOND_CONTAINS_FIRST, diff(range(4, 6), range(2, null)));
    Assert.assertEquals(SECOND_CONTAINS_FIRST, diff(range(null, 6), range(null, 8)));
    Assert.assertEquals(SECOND_CONTAINS_FIRST, diff(range(4, null), range(2, null)));
  }

  @Test
  public void intGreaterThanTest1() {
    IntType int1 = range(2, 3);
    IntType int2 = range(1, 1);
    IntType int3 = range(2, 2);
    IntType int4 = range(4, 5);
    IntType int5 = range(4, 4);
    IntType int6 = range(8, 8);

    IntType expected = null;

    Assert.assertEquals(expected, int2.greaterThanValues(int1, true));
    Assert.assertEquals(expected, int2.greaterThanValues(int1, false));

    Assert.assertEquals(expected, int4.greaterThanValues(int6, true));
    Assert.assertEquals(expected, int4.greaterThanValues(int6, false));

    Assert.assertEquals(expected, int1.greaterThanValues(int4, true));
    Assert.assertEquals(expected, int1.greaterThanValues(int4, false));

    Assert.assertEquals(expected, int3.greaterThanValues(int5, true));
    Assert.assertEquals(expected, int3.greaterThanValues(int5, false));
  }

  @Test
  public void intGreaterThanTest2() {
    IntType int1 = range(2, 5);
    IntType int2 = range(5, 5);
    IntType int3 = range(4, 7);
    IntType int4 = range(1, 6);
    IntType int5 = range(3, 5);
    IntType int6 = range(1, 1);

    IntType expected = null;

    Assert.assertEquals(expected, int1.greaterThanValues(int2, true));
    Assert.assertEquals(expected, int1.greaterThanValues(int2, false));

    Assert.assertEquals(expected, int1.greaterThanValues(int3, true));
    Assert.assertEquals(expected, int1.greaterThanValues(int3, false));

    Assert.assertEquals(expected, int6.greaterThanValues(int4, true));
    Assert.assertEquals(expected, int6.greaterThanValues(int4, false));

    Assert.assertEquals(expected, int1.greaterThanValues(int5, true));
    Assert.assertEquals(expected, int1.greaterThanValues(int5, false));
  }

  @Test
  public void intGreaterThanTest3() {
    IntType int1 = range(5, 7);
    IntType int2 = range(2, 2);
    IntType int3 = range(-6, -3);
    IntType int4 = range(0, 0);
    IntType int5 = range(0, 3);

    IntType expectedTrue1 = range(3, 7);
    IntType expectedFalse1 = range(5, 7);
    IntType expectedTrue2 = range(-2, 2);
    IntType expectedFalse2 = range(2, 2);
    IntType expectedTrue3 = range(1, 2);
    IntType expectedFalse3 = range(2, 2);
    IntType expectedTrue4 = range(4, 7);
    IntType expectedFalse4 = range(5, 7);

    Assert.assertEquals(expectedTrue1, int1.greaterThanValues(int2, true));
    Assert.assertEquals(expectedFalse1, int1.greaterThanValues(int2, false));

    Assert.assertEquals(expectedTrue2, int2.greaterThanValues(int3, true));
    Assert.assertEquals(expectedFalse2, int2.greaterThanValues(int3, false));

    Assert.assertEquals(expectedTrue3, int2.greaterThanValues(int4, true));
    Assert.assertEquals(expectedFalse3, int2.greaterThanValues(int4, false));

    Assert.assertEquals(expectedTrue4, int1.greaterThanValues(int5, true));
    Assert.assertEquals(expectedFalse4, int1.greaterThanValues(int5, false));
  }

  @Test
  public void intGreaterThanTest4() {
    IntType int1 = range(3, 4);
    IntType int2 = range(3, 3);
    IntType int3 = range(5, 5);
    IntType int4 = range(1, 5);
    IntType int5 = range(6, 9);
    IntType int6 = range(1, 7);
    IntType int7 = range(5, 7);

    IntType expected1 = range(4, 4);
    IntType expected2 = null;
    IntType expected3 = range(8, 9);
    IntType expected4 = null;

    Assert.assertEquals(expected1, int1.greaterThanValues(int2, true));
    Assert.assertEquals(expected1, int1.greaterThanValues(int2, false));

    Assert.assertEquals(expected2, int3.greaterThanValues(int4, true));
    Assert.assertEquals(expected2, int3.greaterThanValues(int4, false));

    Assert.assertEquals(expected3, int5.greaterThanValues(int6, true));
    Assert.assertEquals(expected3, int5.greaterThanValues(int6, false));

    Assert.assertEquals(expected4, int7.greaterThanValues(int6, true));
    Assert.assertEquals(expected4, int7.greaterThanValues(int6, false));
  }

  @Test
  public void intGreaterThanTest5() {
    IntType int1 = range(3, 6);
    IntType int2 = range(4, 4);
    IntType int3 = range(4, 5);

    IntType expected1 = range(5, 6);
    IntType expected2 = range(6, 6);

    Assert.assertEquals(expected1, int1.greaterThanValues(int2, true));
    Assert.assertEquals(expected1, int1.greaterThanValues(int2, false));

    Assert.assertEquals(expected2, int1.greaterThanValues(int3, true));
    Assert.assertEquals(expected2, int1.greaterThanValues(int3, false));
  }

  @Test
  public void intGreaterThanTest6() {
    IntType int1 = range(3, 3);
    IntType int2 = range(1, 6);
    IntType int3 = range(3, 5);

    IntType expected = null;

    Assert.assertEquals(expected, int1.greaterThanValues(int2, true));
    Assert.assertEquals(expected, int1.greaterThanValues(int2, false));

    Assert.assertEquals(expected, int3.greaterThanValues(int2, true));
    Assert.assertEquals(expected, int3.greaterThanValues(int2, false));
  }

  @Test
  public void intGreaterThanTest7() {
    IntType int1 = range(3, 3);
    IntType int2 = range(3, 5);

    IntType expected = null;

    Assert.assertEquals(expected, int1.greaterThanValues(int1, true));
    Assert.assertEquals(expected, int1.greaterThanValues(int1, false));

    Assert.assertEquals(expected, int2.greaterThanValues(int2, true));
    Assert.assertEquals(expected, int2.greaterThanValues(int2, false));
  }

  @Test
  public void intLessThanTest1() {
    IntType int1 = range(2, 3);
    IntType int2 = range(1, 1);
    IntType int3 = range(2, 2);
    IntType int4 = range(4, 5);
    IntType int5 = range(4, 4);
    IntType int6 = range(8, 8);

    IntType expectedTrue1 = range(1, 1);
    IntType expectedFalse1 = range(1, 1);

    IntType expectedTrue2 = range(4, 7);
    IntType expectedFalse2 = range(4, 5);

    IntType expectedTrue3 = range(2, 3);
    IntType expectedFalse3 = range(2, 3);

    IntType expectedTrue4 = range(2, 3);
    IntType expectedFalse4 = range(2, 2);

    Assert.assertEquals(expectedTrue1, int2.lessThanValues(int1, true));
    Assert.assertEquals(expectedFalse1, int2.lessThanValues(int1, false));

    Assert.assertEquals(expectedTrue2, int4.lessThanValues(int6, true));
    Assert.assertEquals(expectedFalse2, int4.lessThanValues(int6, false));

    Assert.assertEquals(expectedTrue3, int1.lessThanValues(int4, true));
    Assert.assertEquals(expectedFalse3, int1.lessThanValues(int4, false));

    Assert.assertEquals(expectedTrue4, int3.lessThanValues(int5, true));
    Assert.assertEquals(expectedFalse4, int3.lessThanValues(int5, false));
  }

  @Test
  public void intLessThanTest2() {
    IntType int1 = range(2, 5);
    IntType int2 = range(5, 5);
    IntType int3 = range(4, 7);
    IntType int4 = range(1, 6);
    IntType int5 = range(3, 5);
    IntType int6 = range(1, 1);

    IntType expected1 = range(2, 4);

    IntType expected2 = range(2, 3);

    IntType expected3 = null;

    IntType expected4 = range(2, 2);

    Assert.assertEquals(expected1, int1.lessThanValues(int2, true));
    Assert.assertEquals(expected1, int1.lessThanValues(int2, false));

    Assert.assertEquals(expected2, int1.lessThanValues(int3, true));
    Assert.assertEquals(expected2, int1.lessThanValues(int3, false));

    Assert.assertEquals(expected3, int6.lessThanValues(int4, true));
    Assert.assertEquals(expected3, int6.lessThanValues(int4, false));

    Assert.assertEquals(expected4, int1.lessThanValues(int5, true));
    Assert.assertEquals(expected4, int1.lessThanValues(int5, false));
  }

  @Test
  public void intLessThanTest3() {
    IntType int1 = range(5, 7);
    IntType int2 = range(2, 2);
    IntType int3 = range(-6, -3);
    IntType int4 = range(0, 0);
    IntType int5 = range(0, 3);

    IntType expected = null;

    Assert.assertEquals(expected, int1.lessThanValues(int2, true));
    Assert.assertEquals(expected, int1.lessThanValues(int2, false));

    Assert.assertEquals(expected, int2.lessThanValues(int3, true));
    Assert.assertEquals(expected, int2.lessThanValues(int3, false));

    Assert.assertEquals(expected, int2.lessThanValues(int4, true));
    Assert.assertEquals(expected, int2.lessThanValues(int4, false));

    Assert.assertEquals(expected, int1.lessThanValues(int5, true));
    Assert.assertEquals(expected, int1.lessThanValues(int5, false));
  }

  @Test
  public void intLessThanTest4() {
    IntType int1 = range(3, 4);
    IntType int2 = range(3, 3);
    IntType int3 = range(5, 5);
    IntType int4 = range(1, 5);
    IntType int5 = range(6, 9);
    IntType int6 = range(1, 7);
    IntType int7 = range(5, 7);

    IntType expected = null;

    Assert.assertEquals(expected, int1.lessThanValues(int2, true));
    Assert.assertEquals(expected, int1.lessThanValues(int2, false));

    Assert.assertEquals(expected, int3.lessThanValues(int4, true));
    Assert.assertEquals(expected, int3.lessThanValues(int4, false));

    Assert.assertEquals(expected, int5.lessThanValues(int6, true));
    Assert.assertEquals(expected, int5.lessThanValues(int6, false));

    Assert.assertEquals(expected, int7.lessThanValues(int6, true));
    Assert.assertEquals(expected, int7.lessThanValues(int6, false));
  }

  @Test
  public void intLessThanTest5() {
    IntType int1 = range(3, 6);
    IntType int2 = range(4, 4);
    IntType int3 = range(4, 5);

    IntType expected = range(3, 3);

    Assert.assertEquals(expected, int1.lessThanValues(int2, true));
    Assert.assertEquals(expected, int1.lessThanValues(int2, false));

    Assert.assertEquals(expected, int1.lessThanValues(int3, true));
    Assert.assertEquals(expected, int1.lessThanValues(int3, false));
  }

  @Test
  public void intLessThanTest6() {
    IntType int1 = range(3, 3);
    IntType int2 = range(1, 6);
    IntType int3 = range(3, 5);

    IntType expected = null;

    Assert.assertEquals(expected, int1.lessThanValues(int2, true));
    Assert.assertEquals(expected, int1.lessThanValues(int2, false));

    Assert.assertEquals(expected, int3.lessThanValues(int2, true));
    Assert.assertEquals(expected, int3.lessThanValues(int2, false));
  }

  @Test
  public void intLessThanTest7() {
    IntType int1 = range(3, 3);
    IntType int2 = range(3, 5);

    IntType expected = null;

    Assert.assertEquals(expected, int1.lessThanValues(int1, true));
    Assert.assertEquals(expected, int1.lessThanValues(int1, false));

    Assert.assertEquals(expected, int2.lessThanValues(int2, true));
    Assert.assertEquals(expected, int2.lessThanValues(int2, false));
  }
  
  @Test
  public void intLessThanOrEqualsTest1() {
    IntType int1 = range(2, 3);
    IntType int2 = range(1, 1);
    IntType int3 = range(2, 2);
    IntType int4 = range(4, 5);
    IntType int5 = range(4, 4);
    IntType int6 = range(8, 8);

    IntType expectedTrue1 = range(1, 2);
    IntType expectedFalse1 = range(1, 1);

    IntType expectedTrue2 = range(4, 8);
    IntType expectedFalse2 = range(4, 5);

    IntType expectedTrue3 = range(2, 4);
    IntType expectedFalse3 = range(2, 3);

    IntType expectedTrue4 = range(2, 4);
    IntType expectedFalse4 = range(2, 2);

    Assert.assertEquals(expectedTrue1, int2.lessThanOrEqualsValues(int1, true));
    Assert.assertEquals(expectedFalse1, int2.lessThanOrEqualsValues(int1, false));

    Assert.assertEquals(expectedTrue2, int4.lessThanOrEqualsValues(int6, true));
    Assert.assertEquals(expectedFalse2, int4.lessThanOrEqualsValues(int6, false));

    Assert.assertEquals(expectedTrue3, int1.lessThanOrEqualsValues(int4, true));
    Assert.assertEquals(expectedFalse3, int1.lessThanOrEqualsValues(int4, false));

    Assert.assertEquals(expectedTrue4, int3.lessThanOrEqualsValues(int5, true));
    Assert.assertEquals(expectedFalse4, int3.lessThanOrEqualsValues(int5, false));
  }

  @Test
  public void intLessThanOrEqualsTest2() {
    IntType int1 = range(2, 5);
    IntType int2 = range(5, 5);
    IntType int3 = range(4, 7);
    IntType int4 = range(1, 6);
    IntType int5 = range(3, 5);
    IntType int6 = range(1, 1);

    IntType expected1 = range(2, 5);

    IntType expected2 = range(2, 4);

    IntType expected3 = range(1, 1);

    IntType expected4 = range(2, 3);

    Assert.assertEquals(expected1, int1.lessThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected1, int1.lessThanOrEqualsValues(int2, false));

    Assert.assertEquals(expected2, int1.lessThanOrEqualsValues(int3, true));
    Assert.assertEquals(expected2, int1.lessThanOrEqualsValues(int3, false));

    Assert.assertEquals(expected3, int6.lessThanOrEqualsValues(int4, true));
    Assert.assertEquals(expected3, int6.lessThanOrEqualsValues(int4, false));

    Assert.assertEquals(expected4, int1.lessThanOrEqualsValues(int5, true));
    Assert.assertEquals(expected4, int1.lessThanOrEqualsValues(int5, false));
  }

  @Test
  public void intLessThanOrEqualsTest3() {
    IntType int1 = range(5, 7);
    IntType int2 = range(2, 2);
    IntType int3 = range(-6, -3);
    IntType int4 = range(0, 0);
    IntType int5 = range(0, 3);

    IntType expected = null;

    Assert.assertEquals(expected, int1.lessThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected, int1.lessThanOrEqualsValues(int2, false));

    Assert.assertEquals(expected, int2.lessThanOrEqualsValues(int3, true));
    Assert.assertEquals(expected, int2.lessThanOrEqualsValues(int3, false));

    Assert.assertEquals(expected, int2.lessThanOrEqualsValues(int4, true));
    Assert.assertEquals(expected, int2.lessThanOrEqualsValues(int4, false));

    Assert.assertEquals(expected, int1.lessThanOrEqualsValues(int5, true));
    Assert.assertEquals(expected, int1.lessThanOrEqualsValues(int5, false));
  }

  @Test
  public void intLessThanOrEqualsTest4() {
    IntType int1 = range(3, 4);
    IntType int2 = range(3, 3);
    IntType int3 = range(5, 5);
    IntType int4 = range(1, 5);
    IntType int5 = range(6, 9);
    IntType int6 = range(1, 7);
    IntType int7 = range(5, 7);

    IntType expected1 = range(3,3);
    IntType expectedNull = null;

    Assert.assertEquals(expected1, int1.lessThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected1, int1.lessThanOrEqualsValues(int2, false));

    Assert.assertEquals(expectedNull, int3.lessThanOrEqualsValues(int4, true));
    Assert.assertEquals(expectedNull, int3.lessThanOrEqualsValues(int4, false));

    Assert.assertEquals(expectedNull, int5.lessThanOrEqualsValues(int6, true));
    Assert.assertEquals(expectedNull, int5.lessThanOrEqualsValues(int6, false));

    Assert.assertEquals(expectedNull, int7.lessThanOrEqualsValues(int6, true));
    Assert.assertEquals(expectedNull, int7.lessThanOrEqualsValues(int6, false));
  }

  @Test
  public void intLessThanOrEqualsTest5() {
    IntType int1 = range(3, 6);
    IntType int2 = range(4, 4);
    IntType int3 = range(4, 5);

    IntType expected = range(3, 4);

    Assert.assertEquals(expected, int1.lessThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected, int1.lessThanOrEqualsValues(int2, false));

    Assert.assertEquals(expected, int1.lessThanOrEqualsValues(int3, true));
    Assert.assertEquals(expected, int1.lessThanOrEqualsValues(int3, false));
  }

  @Test
  public void intLessThanOrEqualsTest6() {
    IntType int1 = range(3, 3);
    IntType int2 = range(1, 6);
    IntType int3 = range(3, 5);

    IntType expected = null;

    Assert.assertEquals(expected, int1.lessThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected, int1.lessThanOrEqualsValues(int2, false));

    Assert.assertEquals(expected, int3.lessThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected, int3.lessThanOrEqualsValues(int2, false));
  }

  @Test
  public void intLessThanOrEqualsTest7() {
    IntType int1 = range(3, 3);
    IntType int2 = range(3, 5);

    IntType expected = range(3, 3);

    Assert.assertEquals(expected, int1.lessThanOrEqualsValues(int1, true));
    Assert.assertEquals(expected, int1.lessThanOrEqualsValues(int1, false));

    Assert.assertEquals(expected, int2.lessThanOrEqualsValues(int2, true));
    Assert.assertEquals(expected, int2.lessThanOrEqualsValues(int2, false));
  }
  
  @Test
  public void intCommonValuesWithTest1() {
    IntType int1 = range(2, 3);
    IntType int2 = range(1, 1);
    IntType int3 = range(2, 2);
    IntType int4 = range(4, 5);
    IntType int5 = range(4, 4);
    IntType int6 = range(8, 8);

    IntType expectedNull = null;

    Assert.assertEquals(expectedNull, int2.commonValuesWith(int1));
    Assert.assertEquals(expectedNull, int2.commonValuesWith(int1));

    Assert.assertEquals(expectedNull, int4.commonValuesWith(int6));
    Assert.assertEquals(expectedNull, int4.commonValuesWith(int6));

    Assert.assertEquals(expectedNull, int1.commonValuesWith(int4));
    Assert.assertEquals(expectedNull, int1.commonValuesWith(int4));

    Assert.assertEquals(expectedNull, int3.commonValuesWith(int5));
    Assert.assertEquals(expectedNull, int3.commonValuesWith(int5));
  }

  @Test
  public void intCommonValuesWithTest2() {
    IntType int1 = range(2, 5);
    IntType int2 = range(5, 5);
    IntType int3 = range(4, 7);
    IntType int4 = range(1, 6);
    IntType int5 = range(3, 5);
    IntType int6 = range(1, 1);

    IntType expected1 = range(5, 5);
    IntType expected2 = range(4, 5);
    IntType expected3 = range(1, 1);
    IntType expected4 = range(3, 5);

    Assert.assertEquals(expected1, int1.commonValuesWith(int2));
    Assert.assertEquals(expected1, int1.commonValuesWith(int2));

    Assert.assertEquals(expected2, int1.commonValuesWith(int3));
    Assert.assertEquals(expected2, int1.commonValuesWith(int3));

    Assert.assertEquals(expected3, int6.commonValuesWith(int4));
    Assert.assertEquals(expected3, int6.commonValuesWith(int4));

    Assert.assertEquals(expected4, int1.commonValuesWith(int5));
    Assert.assertEquals(expected4, int1.commonValuesWith(int5));
  }

  @Test
  public void intCommonValuesWithTest3() {
    IntType int1 = range(5, 7);
    IntType int2 = range(2, 2);
    IntType int3 = range(-6, -3);
    IntType int4 = range(0, 0);
    IntType int5 = range(0, 3);

    IntType expected = null;

    Assert.assertEquals(expected, int1.commonValuesWith(int2));
    Assert.assertEquals(expected, int1.commonValuesWith(int2));

    Assert.assertEquals(expected, int2.commonValuesWith(int3));
    Assert.assertEquals(expected, int2.commonValuesWith(int3));

    Assert.assertEquals(expected, int2.commonValuesWith(int4));
    Assert.assertEquals(expected, int2.commonValuesWith(int4));

    Assert.assertEquals(expected, int1.commonValuesWith(int5));
    Assert.assertEquals(expected, int1.commonValuesWith(int5));
  }

  @Test
  public void intCommonValuesWithTest4() {
    IntType int1 = range(3, 4);
    IntType int2 = range(3, 3);
    IntType int3 = range(5, 5);
    IntType int4 = range(1, 5);
    IntType int5 = range(6, 9);
    IntType int6 = range(1, 7);
    IntType int7 = range(5, 7);

    IntType expected1 = range(3, 3);
    IntType expected2 = range(5, 5);
    IntType expected3 = range(6, 7);
    IntType expected4 = range(5, 7);

    Assert.assertEquals(expected1, int1.commonValuesWith(int2));
    Assert.assertEquals(expected1, int1.commonValuesWith(int2));

    Assert.assertEquals(expected2, int3.commonValuesWith(int4));
    Assert.assertEquals(expected2, int3.commonValuesWith(int4));

    Assert.assertEquals(expected3, int5.commonValuesWith(int6));
    Assert.assertEquals(expected3, int5.commonValuesWith(int6));

    Assert.assertEquals(expected4, int7.commonValuesWith(int6));
    Assert.assertEquals(expected4, int7.commonValuesWith(int6));
  }

  @Test
  public void intCommonValuesWithTest5() {
    IntType int1 = range(3, 6);
    IntType int2 = range(4, 4);
    IntType int3 = range(4, 5);

    IntType expected1 = range(4, 4);
    IntType expected2 = range(4, 5);

    Assert.assertEquals(expected1, int1.commonValuesWith(int2));
    Assert.assertEquals(expected1, int1.commonValuesWith(int2));

    Assert.assertEquals(expected2, int1.commonValuesWith(int3));
    Assert.assertEquals(expected2, int1.commonValuesWith(int3));
  }

  @Test
  public void intCommonValuesWithTest6() {
    IntType int1 = range(3, 3);
    IntType int2 = range(1, 6);
    IntType int3 = range(3, 5);

    IntType expected1 = range(3, 3);
    IntType expected2 = range(3, 5);

    Assert.assertEquals(expected1, int1.commonValuesWith(int2));
    Assert.assertEquals(expected1, int1.commonValuesWith(int2));

    Assert.assertEquals(expected2, int3.commonValuesWith(int2));
    Assert.assertEquals(expected2, int3.commonValuesWith(int2));
  }

  @Test
  public void intCommonValuesWithTest7() {
    IntType int1 = range(3, 3);
    IntType int2 = range(3, 5);

    IntType expected1 = range(3, 3);
    IntType expected2 = range(3, 5);

    Assert.assertEquals(expected1, int1.commonValuesWith(int1));
    Assert.assertEquals(expected1, int1.commonValuesWith(int1));

    Assert.assertEquals(expected2, int2.commonValuesWith(int2));
    Assert.assertEquals(expected2, int2.commonValuesWith(int2));
  }
  
  @Test
  public void intExcludeValuesTest1() {
    IntType int1 = range(2, 3);
    IntType int2 = range(1, 1);
    IntType int3 = range(2, 2);
    IntType int4 = range(4, 5);
    IntType int5 = range(4, 4);
    IntType int6 = range(8, 8);

    IntType expected1 = range(1, 1);
    IntType expected2 = range(4, 5);
    IntType expected3 = range(2, 3);
    IntType expected4 = range(2, 2);

    Assert.assertEquals(expected1, int2.exclude(int1));
    Assert.assertEquals(expected1, int2.exclude(int1));

    Assert.assertEquals(expected2, int4.exclude(int6));
    Assert.assertEquals(expected2, int4.exclude(int6));

    Assert.assertEquals(expected3, int1.exclude(int4));
    Assert.assertEquals(expected3, int1.exclude(int4));

    Assert.assertEquals(expected4, int3.exclude(int5));
    Assert.assertEquals(expected4, int3.exclude(int5));
  }

  @Test
  public void intExcludeValuesTest2() {
    IntType int1 = range(2, 5);
    IntType int2 = range(5, 5);
    IntType int3 = range(4, 7);
    IntType int4 = range(1, 6);
    IntType int5 = range(3, 5);
    IntType int6 = range(1, 1);

    IntType expected1 = range(2, 4);
    IntType expected2 = range(2, 3);
    IntType expected3 = null;
    IntType expected4 = range(2, 2);

    Assert.assertEquals(expected1, int1.exclude(int2));
    Assert.assertEquals(expected1, int1.exclude(int2));

    Assert.assertEquals(expected2, int1.exclude(int3));
    Assert.assertEquals(expected2, int1.exclude(int3));

    Assert.assertEquals(expected3, int6.exclude(int4));
    Assert.assertEquals(expected3, int6.exclude(int4));

    Assert.assertEquals(expected4, int1.exclude(int5));
    Assert.assertEquals(expected4, int1.exclude(int5));
  }

  @Test
  public void intExcludeValuesTest3() {
    IntType int1 = range(5, 7);
    IntType int2 = range(2, 2);
    IntType int3 = range(-6, -3);
    IntType int4 = range(0, 0);
    IntType int5 = range(0, 3);

    IntType expected1 = range(5, 7);
    IntType expected2 = range(2, 2);
    IntType expected3 = range(2, 2);
    IntType expected4 = range(5, 7);

    Assert.assertEquals(expected1, int1.exclude(int2));
    Assert.assertEquals(expected1, int1.exclude(int2));

    Assert.assertEquals(expected2, int2.exclude(int3));
    Assert.assertEquals(expected2, int2.exclude(int3));

    Assert.assertEquals(expected3, int2.exclude(int4));
    Assert.assertEquals(expected3, int2.exclude(int4));

    Assert.assertEquals(expected4, int1.exclude(int5));
    Assert.assertEquals(expected4, int1.exclude(int5));
  }

  @Test
  public void intExcludeValuesTest4() {
    IntType int1 = range(3, 4);
    IntType int2 = range(3, 3);
    IntType int3 = range(5, 5);
    IntType int4 = range(1, 5);
    IntType int5 = range(6, 9);
    IntType int6 = range(1, 7);
    IntType int7 = range(5, 7);

    IntType expected1 = range(4, 4);
    IntType expected2 = null;
    IntType expected3 = range(1, 6);
    IntType expected4 = null;

    Assert.assertEquals(expected1, int1.exclude(int2));
    Assert.assertEquals(expected1, int1.exclude(int2));

    Assert.assertEquals(expected2, int3.exclude(int4));
    Assert.assertEquals(expected2, int3.exclude(int4));

    Assert.assertEquals(expected3, int5.exclude(int6));
    Assert.assertEquals(expected3, int5.exclude(int6));

    Assert.assertEquals(expected4, int7.exclude(int6));
    Assert.assertEquals(expected4, int7.exclude(int6));
  }

  @Test
  public void intExcludeValuesTest5() {
    IntType int1 = range(3, 6);
    IntType int2 = range(4, 4);
    IntType int3 = range(4, 5);

    Type expected1 = Types.union(range(3, 3), range(5, 6));
    Type expected2 = Types.union(range(3, 3), range(6, 6));

    Assert.assertEquals(expected1, int1.exclude(int2));
    Assert.assertEquals(expected1, int1.exclude(int2));

    Assert.assertEquals(expected2, int1.exclude(int3));
    Assert.assertEquals(expected2, int1.exclude(int3));
  }

  @Test
  public void intExcludeValuesTest6() {
    IntType int1 = range(3, 3);
    IntType int2 = range(1, 6);
    IntType int3 = range(3, 5);

    IntType expected1 = null;
    IntType expected2 = null;

    Assert.assertEquals(expected1, int1.exclude(int2));
    Assert.assertEquals(expected1, int1.exclude(int2));

    Assert.assertEquals(expected2, int3.exclude(int2));
    Assert.assertEquals(expected2, int3.exclude(int2));
  }

  @Test
  public void intExcludeValuesTest7() {
    IntType int1 = range(3, 3);
    IntType int2 = range(3, 5);

    IntType expected1 = null;
    IntType expected2 = null;

    Assert.assertEquals(expected1, int1.exclude(int1));
    Assert.assertEquals(expected1, int1.exclude(int1));

    Assert.assertEquals(expected2, int2.exclude(int2));
    Assert.assertEquals(expected2, int2.exclude(int2));
  }
}
