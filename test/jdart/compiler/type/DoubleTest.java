package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;
import static jdart.compiler.type.IntTest.range;

import junit.framework.Assert;

import org.junit.Test;

public class DoubleTest {

  @Test
  public void lessOrEqualsThantest() {
    DoubleType dType = DoubleType.constant(6.2);
    IntType iTypeContains = range(false, 4, 8);
    IntType iTypeBefore = range(false, 2, 6);
    IntType iTypeAfter = range(false, 8, 10);

    DoubleType dTypeRound = DoubleType.constant(7);
    IntType borderMin = range(false, 7, 10);
    IntType borderMax = range(false, 2, 7);
    IntType eq = range(false, 7, 7);

    Assert.assertEquals(null, dType.lessThanOrEqualsValues(iTypeContains, false));
    Assert.assertEquals(null, dType.lessThanOrEqualsValues(iTypeBefore, false));
    Assert.assertEquals(dType, dType.lessThanOrEqualsValues(iTypeAfter, false));

    Assert.assertEquals(null, dTypeRound.lessThanOrEqualsValues(iTypeContains, false));
    Assert.assertEquals(null, dTypeRound.lessThanOrEqualsValues(iTypeBefore, false));
    Assert.assertEquals(dTypeRound, dTypeRound.lessThanOrEqualsValues(iTypeAfter, false));
    Assert.assertEquals(dTypeRound, dTypeRound.lessThanOrEqualsValues(borderMin, false));
    Assert.assertEquals(null, dTypeRound.lessThanOrEqualsValues(borderMax, false));
    Assert.assertEquals(dTypeRound, dTypeRound.lessThanOrEqualsValues(eq, false));
  }

  @Test
  public void lessThantest() {
    DoubleType dType = DoubleType.constant(6.2);
    IntType iTypeContains = range(false, 4, 8);
    IntType iTypeBefore = range(false, 2, 6);
    IntType iTypeAfter = range(false, 8, 10);

    DoubleType dTypeRound = DoubleType.constant(7);
    IntType borderMin = range(false, 7, 10);
    IntType borderMax = range(false, 2, 7);
    IntType eq = range(false, 7, 7);

    Assert.assertEquals(null, dType.lessThanValues(iTypeContains, false));
    Assert.assertEquals(null, dType.lessThanValues(iTypeBefore, false));
    Assert.assertEquals(dType, dType.lessThanValues(iTypeAfter, false));

    Assert.assertEquals(null, dTypeRound.lessThanValues(iTypeContains, false));
    Assert.assertEquals(null, dTypeRound.lessThanValues(iTypeBefore, false));
    Assert.assertEquals(dTypeRound, dTypeRound.lessThanValues(iTypeAfter, false));
    Assert.assertEquals(null, dTypeRound.lessThanValues(borderMin, false));
    Assert.assertEquals(null, dTypeRound.lessThanValues(borderMax, false));
    Assert.assertEquals(null, dTypeRound.lessThanValues(eq, false));
  }

  @Test
  public void greaterOrEqualsThantest() {
    DoubleType dType = DoubleType.constant(6.2);
    IntType iTypeContains = range(false, 4, 8);
    IntType iTypeBefore = range(false, 2, 6);
    IntType iTypeAfter = range(false, 8, 10);

    DoubleType dTypeRound = DoubleType.constant(7);
    IntType borderMin = range(false, 7, 10);
    IntType borderMax = range(false, 2, 7);
    IntType eq = range(false, 7, 7);

    Assert.assertEquals(null, dType.greaterThanOrEqualsValues(iTypeContains, false));
    Assert.assertEquals(dType, dType.greaterThanOrEqualsValues(iTypeBefore, false));
    Assert.assertEquals(null, dType.greaterThanOrEqualsValues(iTypeAfter, false));

    Assert.assertEquals(null, dTypeRound.greaterThanOrEqualsValues(iTypeContains, false));
    Assert.assertEquals(dTypeRound, dTypeRound.greaterThanOrEqualsValues(iTypeBefore, false));
    Assert.assertEquals(null, dTypeRound.greaterThanOrEqualsValues(iTypeAfter, false));
    Assert.assertEquals(null, dTypeRound.greaterThanOrEqualsValues(borderMin, false));
    Assert.assertEquals(dTypeRound, dTypeRound.greaterThanOrEqualsValues(borderMax, false));
    Assert.assertEquals(dTypeRound, dTypeRound.greaterThanOrEqualsValues(eq, false));
  }

  @Test
  public void greaterThantest() {
    DoubleType dType = DoubleType.constant(6.2);
    IntType iTypeContains = range(false, 4, 8);
    IntType iTypeBefore = range(false, 2, 6);
    IntType iTypeAfter = range(false, 8, 10);

    DoubleType dTypeRound = DoubleType.constant(7);
    IntType borderMin = range(false, 7, 10);
    IntType borderMax = range(false, 2, 7);
    IntType eq = range(false, 7, 7);

    Assert.assertEquals(null, dType.greaterThanValues(iTypeContains, false));
    Assert.assertEquals(dType, dType.greaterThanValues(iTypeBefore, false));
    Assert.assertEquals(null, dType.greaterThanValues(iTypeAfter, false));

    Assert.assertEquals(null, dTypeRound.greaterThanValues(iTypeContains, false));
    Assert.assertEquals(dTypeRound, dTypeRound.greaterThanValues(iTypeBefore, false));
    Assert.assertEquals(null, dTypeRound.greaterThanValues(iTypeAfter, false));
    Assert.assertEquals(null, dTypeRound.greaterThanValues(borderMin, false));
    Assert.assertEquals(null, dTypeRound.greaterThanValues(borderMax, false));
    Assert.assertEquals(null, dTypeRound.greaterThanValues(eq, false));
  }

  @Test
  public void isIncluded() {
    DoubleType dType1 = DoubleType.constant(5.2).asNonNull();
    DoubleType dType2 = DoubleType.constant(5).asNonNull();
    DoubleType dType3 = DoubleType.constant(5.2).asNullable();
    DoubleType dType4 = DoubleType.constant(5).asNullable();
    
    Assert.assertFalse(dType1.isIncludeIn(NEGATIVE_INT32_TYPE));
    Assert.assertTrue(dType1.isIncludeIn(POSITIVE_INT32_TYPE));
    Assert.assertTrue(dType1.isIncludeIn(INT32_TYPE));
    Assert.assertTrue(dType1.isIncludeIn(INT_NON_NULL_TYPE));
    Assert.assertTrue(dType1.isIncludeIn(INT_TYPE));
    Assert.assertTrue(dType1.isIncludeIn(range(true, 5, 6)));
    Assert.assertTrue(dType1.isIncludeIn(range(false, 5, 6)));
    Assert.assertTrue(dType1.isIncludeIn(DOUBLE_NON_NULL_TYPE));
    Assert.assertTrue(dType1.isIncludeIn(DOUBLE_TYPE));
    Assert.assertTrue(dType1.isIncludeIn(DYNAMIC_NON_NULL_TYPE));
    Assert.assertTrue(dType1.isIncludeIn(DYNAMIC_TYPE));
    
    Assert.assertFalse(dType2.isIncludeIn(NEGATIVE_INT32_TYPE));
    Assert.assertTrue(dType2.isIncludeIn(POSITIVE_INT32_TYPE));
    Assert.assertTrue(dType2.isIncludeIn(INT32_TYPE));
    Assert.assertTrue(dType2.isIncludeIn(INT_NON_NULL_TYPE));
    Assert.assertTrue(dType2.isIncludeIn(INT_TYPE));
    Assert.assertTrue(dType2.isIncludeIn(range(true, 5, 6)));
    Assert.assertTrue(dType2.isIncludeIn(range(false, 5, 6)));
    Assert.assertTrue(dType2.isIncludeIn(DOUBLE_NON_NULL_TYPE));
    Assert.assertTrue(dType2.isIncludeIn(DOUBLE_TYPE));
    Assert.assertTrue(dType2.isIncludeIn(DYNAMIC_NON_NULL_TYPE));
    Assert.assertTrue(dType2.isIncludeIn(DYNAMIC_TYPE));
    
    Assert.assertFalse(dType3.isIncludeIn(NEGATIVE_INT32_TYPE));
    Assert.assertFalse(dType3.isIncludeIn(POSITIVE_INT32_TYPE));
    Assert.assertFalse(dType3.isIncludeIn(INT32_TYPE));
    Assert.assertFalse(dType3.isIncludeIn(INT_NON_NULL_TYPE));
    Assert.assertTrue(dType3.isIncludeIn(INT_TYPE));
    Assert.assertTrue(dType3.isIncludeIn(range(true, 5, 6)));
    Assert.assertFalse(dType3.isIncludeIn(range(false, 5, 6)));
    Assert.assertFalse(dType3.isIncludeIn(DOUBLE_NON_NULL_TYPE));
    Assert.assertTrue(dType3.isIncludeIn(DOUBLE_TYPE));
    Assert.assertFalse(dType3.isIncludeIn(DYNAMIC_NON_NULL_TYPE));
    Assert.assertTrue(dType3.isIncludeIn(DYNAMIC_TYPE));
    
    Assert.assertFalse(dType4.isIncludeIn(NEGATIVE_INT32_TYPE));
    Assert.assertFalse(dType4.isIncludeIn(POSITIVE_INT32_TYPE));
    Assert.assertFalse(dType4.isIncludeIn(INT32_TYPE));
    Assert.assertFalse(dType4.isIncludeIn(INT_NON_NULL_TYPE));
    Assert.assertTrue(dType4.isIncludeIn(INT_TYPE));
    Assert.assertTrue(dType4.isIncludeIn(range(true, 5, 6)));
    Assert.assertFalse(dType4.isIncludeIn(range(false, 5, 6)));
    Assert.assertFalse(dType4.isIncludeIn(DOUBLE_NON_NULL_TYPE));
    Assert.assertTrue(dType4.isIncludeIn(DOUBLE_TYPE));
    Assert.assertFalse(dType4.isIncludeIn(DYNAMIC_NON_NULL_TYPE));
    Assert.assertTrue(dType4.isIncludeIn(DYNAMIC_TYPE));
  }
}
