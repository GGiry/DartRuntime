package jdart.compiler.flow;

import static jdart.compiler.type.CoreTypeRepository.*;
import static jdart.compiler.type.IntTest.range;

import jdart.compiler.type.DoubleType;
import jdart.compiler.type.DynamicType;
import jdart.compiler.type.IntType;
import jdart.compiler.type.NullType;
import jdart.compiler.type.Type;
import jdart.compiler.type.Types;
import jdart.compiler.type.UnionTest;
import jdart.compiler.type.UnionType;

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

  @Test
  public void testDoubleDouble() {
    DoubleType double1 = DoubleType.constant(3.5);
    DoubleType double2 = DoubleType.constant(5.3);
    DoubleType doubleRound1 = DoubleType.constant(6);;
    DoubleType doubleRound2 = DoubleType.constant(10);;

    Assert.assertEquals(DoubleType.constant(8.8), FTVisitor.opDoubleDouble(Token.ADD, null, double1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(16), FTVisitor.opDoubleDouble(Token.ADD, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(DoubleType.constant(3.5 - 5.3), FTVisitor.opDoubleDouble(Token.SUB, null, double1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(-4), FTVisitor.opDoubleDouble(Token.SUB, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(DoubleType.constant(3.5 * 5.3), FTVisitor.opDoubleDouble(Token.MUL, null, double1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(60), FTVisitor.opDoubleDouble(Token.MUL, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(DoubleType.constant(3.5 / 5.3), FTVisitor.opDoubleDouble(Token.DIV, null, double1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(6. / 10.), FTVisitor.opDoubleDouble(Token.DIV, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(DoubleType.constant(3.5), FTVisitor.opDoubleDouble(Token.MOD, null, double1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(5.2), FTVisitor.opDoubleDouble(Token.MOD, null, DoubleType.constant(10.5), null, double2, null));
    Assert.assertEquals(DoubleType.constant(6. % 10.), FTVisitor.opDoubleDouble(Token.MOD, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(TRUE_TYPE, FTVisitor.opDoubleDouble(Token.LT, null, double1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, FTVisitor.opDoubleDouble(Token.LT, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(TRUE_TYPE, FTVisitor.opDoubleDouble(Token.LTE, null, double1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, FTVisitor.opDoubleDouble(Token.LTE, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(FALSE_TYPE, FTVisitor.opDoubleDouble(Token.GT, null, double1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, FTVisitor.opDoubleDouble(Token.GT, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(FALSE_TYPE, FTVisitor.opDoubleDouble(Token.GTE, null, double1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, FTVisitor.opDoubleDouble(Token.GTE, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(FALSE_TYPE, FTVisitor.opDoubleDouble(Token.EQ, null, double1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, FTVisitor.opDoubleDouble(Token.EQ, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(TRUE_TYPE, FTVisitor.opDoubleDouble(Token.NE, null, double1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, FTVisitor.opDoubleDouble(Token.NE, null, doubleRound1, null, doubleRound2, null));
  }
  
  @Test
  public void testBoolBool() {
    Assert.assertEquals(TRUE_TYPE, FTVisitor.opBoolBool(Token.EQ, null, TRUE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, FTVisitor.opBoolBool(Token.EQ, null, TRUE_TYPE, null, FALSE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, FTVisitor.opBoolBool(Token.EQ, null, FALSE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(TRUE_TYPE, FTVisitor.opBoolBool(Token.EQ, null, FALSE_TYPE, null, FALSE_TYPE, null));
    
    Assert.assertEquals(FALSE_TYPE, FTVisitor.opBoolBool(Token.NE, null, TRUE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(TRUE_TYPE, FTVisitor.opBoolBool(Token.NE, null, TRUE_TYPE, null, FALSE_TYPE, null));
    Assert.assertEquals(TRUE_TYPE, FTVisitor.opBoolBool(Token.NE, null, FALSE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, FTVisitor.opBoolBool(Token.NE, null, FALSE_TYPE, null, FALSE_TYPE, null));
    
    Assert.assertEquals(TRUE_TYPE, FTVisitor.opBoolBool(Token.AND, null, TRUE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, FTVisitor.opBoolBool(Token.AND, null, TRUE_TYPE, null, FALSE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, FTVisitor.opBoolBool(Token.AND, null, FALSE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, FTVisitor.opBoolBool(Token.AND, null, FALSE_TYPE, null, FALSE_TYPE, null));
    
    Assert.assertEquals(TRUE_TYPE, FTVisitor.opBoolBool(Token.OR, null, TRUE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(TRUE_TYPE, FTVisitor.opBoolBool(Token.OR, null, TRUE_TYPE, null, FALSE_TYPE, null));
    Assert.assertEquals(TRUE_TYPE, FTVisitor.opBoolBool(Token.OR, null, FALSE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, FTVisitor.opBoolBool(Token.OR, null, FALSE_TYPE, null, FALSE_TYPE, null));
  }
  
  @Test
  public void testBinaryOpIntDouble() {
    IntType int1 = range(false, 3, 3);
    DoubleType double2 = DoubleType.constant(5.3);
    
    FTVisitor visitor = new FTVisitor(null, null);
    
    Assert.assertEquals(DoubleType.constant(8.3), visitor.visitBinaryOp(null, Token.ADD, null, int1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(8.3), visitor.visitBinaryOp(null, Token.ADD, null, double2, null, int1, null));
    
    Assert.assertEquals(DoubleType.constant(3 - 5.3), visitor.visitBinaryOp(null,Token.SUB, null, int1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(5.3 - 3), visitor.visitBinaryOp(null,Token.SUB, null, double2, null, int1, null));

    Assert.assertEquals(DoubleType.constant(3 * 5.3), visitor.visitBinaryOp(null,Token.MUL, null, int1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(5.3 * 3), visitor.visitBinaryOp(null,Token.MUL, null, double2, null, int1, null));

    Assert.assertEquals(DoubleType.constant(3 / 5.3), visitor.visitBinaryOp(null,Token.DIV, null, int1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(5.3 / 3), visitor.visitBinaryOp(null,Token.DIV, null, double2, null, int1, null));

    Assert.assertEquals(DoubleType.constant(3 % 5.3), visitor.visitBinaryOp(null,Token.MOD, null, int1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(5.3 % 3), visitor.visitBinaryOp(null,Token.MOD, null, double2, null, int1, null));

    Assert.assertEquals(TRUE_TYPE, visitor.visitBinaryOp(null,Token.LT, null, int1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, visitor.visitBinaryOp(null,Token.LT, null, double2, null, int1, null));

    Assert.assertEquals(TRUE_TYPE, visitor.visitBinaryOp(null,Token.LTE, null, int1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, visitor.visitBinaryOp(null,Token.LTE, null, double2, null, int1, null));

    Assert.assertEquals(FALSE_TYPE, visitor.visitBinaryOp(null,Token.GT, null, int1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, visitor.visitBinaryOp(null,Token.GT, null, double2, null, int1, null));

    Assert.assertEquals(FALSE_TYPE, visitor.visitBinaryOp(null,Token.GTE, null, int1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, visitor.visitBinaryOp(null,Token.GTE, null, double2, null, int1, null));

    Assert.assertEquals(FALSE_TYPE, visitor.visitBinaryOp(null,Token.EQ, null, int1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, visitor.visitBinaryOp(null,Token.EQ, null, double2, null, int1, null));

    Assert.assertEquals(TRUE_TYPE, visitor.visitBinaryOp(null,Token.NE, null, int1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, visitor.visitBinaryOp(null,Token.NE, null, double2, null, int1, null));
  }
  
  @Test
  public void testBinaryOpUnions() {
    IntType int1 = range(false, 3, 3);
    DoubleType double2 = DoubleType.constant(5.3);
    UnionType union1 = UnionTest.union(range(false, 3, 4), range(false, 6, 10));
    UnionType union2 = UnionTest.union(range(false, 2, 4), DoubleType.constant(3.9));
    
    FTVisitor visitor = new FTVisitor(null, null);
    
    Assert.assertEquals(UnionTest.union(range(false, 6, 7), range(false, 9, 13)), 
        visitor.visitBinaryOp(null, Token.ADD, null, union1, null, int1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.ADD, null, union1, null, double2, null));
    Assert.assertEquals(UnionTest.union(range(false, 6, 7), range(false, 9, 13)), 
        visitor.visitBinaryOp(null, Token.ADD, null, int1, null, union1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.ADD, null, double2, null, union1, null));
    
    /*Assert.assertEquals(UnionTest.union(range(false, 5, 14), DOUBLE_NON_NULL_TYPE), 
        visitor.visitBinaryOp(null, Token.ADD, null, union1, null, union2, null));*/

    Assert.assertEquals(UnionTest.union(range(false, 0, 1), range(false, 3, 7)), 
        visitor.visitBinaryOp(null,Token.SUB, null, union1, null, int1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.SUB, null, union1, null, double2, null));
    Assert.assertEquals(UnionTest.union(range(false, -1, 0), range(false, -7, -3)), 
        visitor.visitBinaryOp(null, Token.SUB, null, int1, null, union1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.SUB, null, double2, null, union1, null));

    Assert.assertEquals(UnionTest.union(range(false, 9, 12), range(false, 18, 30)), 
        visitor.visitBinaryOp(null,Token.MUL, null, union1, null, int1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.MUL, null, union1, null, double2, null));
    Assert.assertEquals(UnionTest.union(range(false, 9, 12), range(false, 18, 30)), 
        visitor.visitBinaryOp(null,Token.MUL, null, int1, null, union1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.MUL, null, double2, null, union1, null));

    Assert.assertEquals(range(false, 1, 3), visitor.visitBinaryOp(null,Token.DIV, null, union1, null, int1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.DIV, null, union1, null, double2, null));
    Assert.assertEquals(range(false, 0, 1), visitor.visitBinaryOp(null,Token.DIV, null, int1, null, union1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.DIV, null, double2, null, union1, null));

    Assert.assertEquals(range(false, 0, 2), visitor.visitBinaryOp(null,Token.MOD, null, union1, null, int1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.MOD, null, union1, null, double2, null));
    Assert.assertEquals(range(false, 0, 9), visitor.visitBinaryOp(null,Token.MOD, null, int1, null, union1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.MOD, null, double2, null, union1, null));

    Assert.assertEquals(FALSE_TYPE, visitor.visitBinaryOp(null,Token.LT, null, union1, null, int1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.LT, null, union1, null, double2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null,Token.LT, null, int1, null, union1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.LT, null, double2, null, union1, null));

    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null,Token.LTE, null, union1, null, int1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.LTE, null, union1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, visitor.visitBinaryOp(null,Token.LTE, null, int1, null, union1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.LTE, null, double2, null, union1, null));

    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null,Token.GT, null, union1, null, int1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.GT, null, union1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, visitor.visitBinaryOp(null,Token.GT, null, int1, null, union1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.GT, null, double2, null, union1, null));
    
    Assert.assertEquals(TRUE_TYPE, visitor.visitBinaryOp(null,Token.GTE, null, union1, null, int1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.GTE, null, union1, null, double2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null,Token.GTE, null, int1, null, union1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.GTE, null, double2, null, union1, null));

    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null,Token.EQ, null, union1, null, int1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.EQ, null, union1, null, double2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null,Token.EQ, null, int1, null, union1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.EQ, null, double2, null, union1, null));

    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null,Token.NE, null, union1, null, int1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.NE, null, union1, null, double2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null,Token.NE, null, int1, null, union1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.NE, null, double2, null, union1, null));
  }
  
  @Test
  public void testBinaryOpDynamic() {
    IntType int1 = range(false, 3, 3);
    DoubleType double2 = DoubleType.constant(5.3);
    DynamicType dyn = DYNAMIC_NON_NULL_TYPE;
    
    FTVisitor visitor = new FTVisitor(null, null);
    
    Assert.assertEquals(DYNAMIC_NON_NULL_TYPE, visitor.visitBinaryOp(null,Token.ADD, null, dyn, null, int1, null));
    Assert.assertEquals(DYNAMIC_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.ADD, null, dyn, null, double2, null));
    Assert.assertEquals(DYNAMIC_NON_NULL_TYPE, visitor.visitBinaryOp(null,Token.ADD, null, int1, null, dyn, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, visitor.visitBinaryOp(null, Token.ADD, null, double2, null, dyn, null));
  }
}