package jdart.compiler.type;

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
}
