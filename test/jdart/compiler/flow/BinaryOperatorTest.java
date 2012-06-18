package jdart.compiler.flow;

import static jdart.compiler.type.CoreTypeRepository.*;
import static jdart.compiler.type.IntTest.range;
import jdart.compiler.type.IntType;

import org.junit.Assert;
import org.junit.Test;

import com.google.dart.compiler.parser.Token;

public class BinaryOperatorTest {
  @Test
  public void testIntInt() {
    IntType int1 = range(false, 3, 3);
    IntType int2 = range(false, 5, 5);
    IntType range1 = range(false, 3, 5);
    IntType range2 = range(false, 2, 6);

    Assert.assertEquals(range(false, 8, 8), FTVisitor.opIntInt(Token.ADD, null, int1, null, int2, null));
    Assert.assertEquals(range(false, 5, 11), FTVisitor.opIntInt(Token.ADD, null, range1, null, range2, null));

    Assert.assertEquals(range(false, -2, -2), FTVisitor.opIntInt(Token.SUB, null, int1, null, int2, null));
    Assert.assertEquals(range(false, -1, 1), FTVisitor.opIntInt(Token.SUB, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 15, 15), FTVisitor.opIntInt(Token.MUL, null, int1, null, int2, null));
    Assert.assertEquals(range(false, 6, 30), FTVisitor.opIntInt(Token.MUL, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 0, 0), FTVisitor.opIntInt(Token.DIV, null, int1, null, int2, null));
    Assert.assertEquals(range(false, 0, 2), FTVisitor.opIntInt(Token.DIV, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 3, 3), FTVisitor.opIntInt(Token.MOD, null, int1, null, int2, null));
    Assert.assertEquals(range(false, 0, 5), FTVisitor.opIntInt(Token.MOD, null, range1, null, range2, null));

    Assert.assertEquals(TRUE_TYPE, FTVisitor.opIntInt(Token.LT, null, int1, null, int2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, FTVisitor.opIntInt(Token.LT, null, range1, null, range2, null));

    Assert.assertEquals(TRUE_TYPE, FTVisitor.opIntInt(Token.LTE, null, int1, null, int2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, FTVisitor.opIntInt(Token.LTE, null, range1, null, range2, null));

    Assert.assertEquals(FALSE_TYPE, FTVisitor.opIntInt(Token.GT, null, int1, null, int2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, FTVisitor.opIntInt(Token.GT, null, range1, null, range2, null));

    Assert.assertEquals(FALSE_TYPE, FTVisitor.opIntInt(Token.GTE, null, int1, null, int2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, FTVisitor.opIntInt(Token.GTE, null, range1, null, range2, null));

    Assert.assertEquals(FALSE_TYPE, FTVisitor.opIntInt(Token.EQ, null, int1, null, int2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, FTVisitor.opIntInt(Token.EQ, null, range1, null, range2, null));

    Assert.assertEquals(TRUE_TYPE, FTVisitor.opIntInt(Token.NE, null, int1, null, int2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, FTVisitor.opIntInt(Token.NE, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 1, 1), FTVisitor.opIntInt(Token.BIT_AND, null, int1, null, int2, null));
    Assert.assertEquals(INT_NON_NULL_TYPE, FTVisitor.opIntInt(Token.BIT_AND, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 7, 7), FTVisitor.opIntInt(Token.BIT_OR, null, int1, null, int2, null));
    Assert.assertEquals(INT_NON_NULL_TYPE, FTVisitor.opIntInt(Token.BIT_OR, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 6, 6), FTVisitor.opIntInt(Token.BIT_XOR, null, int1, null, int2, null));
    Assert.assertEquals(INT_NON_NULL_TYPE, FTVisitor.opIntInt(Token.BIT_XOR, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 96, 96), FTVisitor.opIntInt(Token.SHL, null, int1, null, int2, null));
    Assert.assertEquals(range(false, 12, 320), FTVisitor.opIntInt(Token.SHL, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 0, 0), FTVisitor.opIntInt(Token.SAR, null, int1, null, int2, null));
    Assert.assertEquals(range(false, 0, 1), FTVisitor.opIntInt(Token.SAR, null, range1, null, range2, null));
  }
}
