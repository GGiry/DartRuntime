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
}
