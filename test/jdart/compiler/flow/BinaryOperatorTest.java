package jdart.compiler.flow;

import static jdart.compiler.type.CoreTypeRepository.*;
import static jdart.compiler.type.IntTest.range;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jdart.compiler.type.DoubleType;
import jdart.compiler.type.DynamicType;
import jdart.compiler.type.IntType;
import jdart.compiler.type.Type;
import jdart.compiler.type.UnionTest;
import jdart.compiler.type.UnionType;

import org.junit.Assert;
import org.junit.Test;

import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartExpression;
import com.google.dart.compiler.parser.Token;

public class BinaryOperatorTest {
  @Test
  public void testIntInt() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    IntType int1 = range(false, 3, 3);
    IntType int2 = range(false, 5, 5);
    IntType range1 = range(false, 3, 5);
    IntType range2 = range(false, 2, 6);
    
    Method method = FTVisitor.class.getDeclaredMethod("opIntInt", Token.class, DartExpression.class, Type.class, DartExpression.class, Type.class, FlowEnv.class);
    method.setAccessible(true);

    Assert.assertEquals(range(false, 8, 8), method.invoke(FTVisitor.class, Token.ADD, null, int1, null, int2, null));
    Assert.assertEquals(range(false, 5, 11), method.invoke(FTVisitor.class, Token.ADD, null, range1, null, range2, null));

    Assert.assertEquals(range(false, -2, -2), method.invoke(FTVisitor.class, Token.SUB, null, int1, null, int2, null));
    Assert.assertEquals(range(false, -1, 1), method.invoke(FTVisitor.class, Token.SUB, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 15, 15), method.invoke(FTVisitor.class, Token.MUL, null, int1, null, int2, null));
    Assert.assertEquals(range(false, 6, 30), method.invoke(FTVisitor.class, Token.MUL, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 0, 0), method.invoke(FTVisitor.class, Token.DIV, null, int1, null, int2, null));
    Assert.assertEquals(range(false, 0, 2), method.invoke(FTVisitor.class, Token.DIV, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 3, 3), method.invoke(FTVisitor.class, Token.MOD, null, int1, null, int2, null));
    Assert.assertEquals(range(false, 0, 5), method.invoke(FTVisitor.class, Token.MOD, null, range1, null, range2, null));

    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class, Token.LT, null, int1, null, int2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(FTVisitor.class, Token.LT, null, range1, null, range2, null));

    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class, Token.LTE, null, int1, null, int2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(FTVisitor.class, Token.LTE, null, range1, null, range2, null));

    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class, Token.GT, null, int1, null, int2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(FTVisitor.class, Token.GT, null, range1, null, range2, null));

    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class, Token.GTE, null, int1, null, int2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(FTVisitor.class, Token.GTE, null, range1, null, range2, null));

    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class, Token.EQ, null, int1, null, int2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(FTVisitor.class, Token.EQ, null, range1, null, range2, null));

    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class, Token.NE, null, int1, null, int2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(FTVisitor.class, Token.NE, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 1, 1), method.invoke(FTVisitor.class, Token.BIT_AND, null, int1, null, int2, null));
    Assert.assertEquals(INT_NON_NULL_TYPE, method.invoke(FTVisitor.class, Token.BIT_AND, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 7, 7), method.invoke(FTVisitor.class, Token.BIT_OR, null, int1, null, int2, null));
    Assert.assertEquals(INT_NON_NULL_TYPE, method.invoke(FTVisitor.class, Token.BIT_OR, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 6, 6), method.invoke(FTVisitor.class, Token.BIT_XOR, null, int1, null, int2, null));
    Assert.assertEquals(INT_NON_NULL_TYPE, method.invoke(FTVisitor.class, Token.BIT_XOR, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 96, 96), method.invoke(FTVisitor.class, Token.SHL, null, int1, null, int2, null));
    Assert.assertEquals(range(false, 12, 320), method.invoke(FTVisitor.class, Token.SHL, null, range1, null, range2, null));

    Assert.assertEquals(range(false, 0, 0), method.invoke(FTVisitor.class, Token.SAR, null, int1, null, int2, null));
    Assert.assertEquals(range(false, 0, 1), method.invoke(FTVisitor.class, Token.SAR, null, range1, null, range2, null));
  }

  @Test
  public void testDoubleDouble() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    DoubleType double1 = DoubleType.constant(3.5);
    DoubleType double2 = DoubleType.constant(5.3);
    DoubleType doubleRound1 = DoubleType.constant(6);
    DoubleType doubleRound2 = DoubleType.constant(10);
    
    Method method = FTVisitor.class.getDeclaredMethod("opDoubleDouble", Token.class, DartExpression.class, Type.class, DartExpression.class, Type.class, FlowEnv.class);
    method.setAccessible(true);

    Assert.assertEquals(DoubleType.constant(8.8), method.invoke(FTVisitor.class,Token.ADD, null, double1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(16), method.invoke(FTVisitor.class,Token.ADD, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(DoubleType.constant(3.5 - 5.3), method.invoke(FTVisitor.class,Token.SUB, null, double1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(-4), method.invoke(FTVisitor.class,Token.SUB, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(DoubleType.constant(3.5 * 5.3), method.invoke(FTVisitor.class,Token.MUL, null, double1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(60), method.invoke(FTVisitor.class,Token.MUL, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(DoubleType.constant(3.5 / 5.3), method.invoke(FTVisitor.class,Token.DIV, null, double1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(6. / 10.), method.invoke(FTVisitor.class,Token.DIV, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(DoubleType.constant(3.5), method.invoke(FTVisitor.class,Token.MOD, null, double1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(5.2), method.invoke(FTVisitor.class,Token.MOD, null, DoubleType.constant(10.5), null, double2, null));
    Assert.assertEquals(DoubleType.constant(6. % 10.), method.invoke(FTVisitor.class,Token.MOD, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.LT, null, double1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.LT, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.LTE, null, double1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.LTE, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.GT, null, double1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.GT, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.GTE, null, double1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.GTE, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.EQ, null, double1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.EQ, null, doubleRound1, null, doubleRound2, null));

    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.NE, null, double1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.NE, null, doubleRound1, null, doubleRound2, null));
  }
  
  @Test
  public void testBoolBool() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method method = FTVisitor.class.getDeclaredMethod("opBoolBool", Token.class, DartExpression.class, Type.class, DartExpression.class, Type.class, FlowEnv.class);
    method.setAccessible(true);
    
    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.EQ, null, TRUE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.EQ, null, TRUE_TYPE, null, FALSE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.EQ, null, FALSE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.EQ, null, FALSE_TYPE, null, FALSE_TYPE, null));
    
    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.NE, null, TRUE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.NE, null, TRUE_TYPE, null, FALSE_TYPE, null));
    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.NE, null, FALSE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.NE, null, FALSE_TYPE, null, FALSE_TYPE, null));
    
    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.AND, null, TRUE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.AND, null, TRUE_TYPE, null, FALSE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.AND, null, FALSE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.AND, null, FALSE_TYPE, null, FALSE_TYPE, null));
    
    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.OR, null, TRUE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.OR, null, TRUE_TYPE, null, FALSE_TYPE, null));
    Assert.assertEquals(TRUE_TYPE, method.invoke(FTVisitor.class,Token.OR, null, FALSE_TYPE, null, TRUE_TYPE, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(FTVisitor.class,Token.OR, null, FALSE_TYPE, null, FALSE_TYPE, null));
  }
  
  @Test
  public void testBinaryOpIntDouble() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    IntType int1 = range(false, 3, 3);
    DoubleType double2 = DoubleType.constant(5.3);
    
    FTVisitor visitor = new FTVisitor(null, null);

    Method method = FTVisitor.class.getDeclaredMethod("visitBinaryOp", DartBinaryExpression.class, Token.class, DartExpression.class, Type.class, 
        DartExpression.class, Type.class, FlowEnv.class);
    method.setAccessible(true);
    
    Assert.assertEquals(DoubleType.constant(8.3), method.invoke(visitor, null, Token.ADD, null, int1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(8.3), method.invoke(visitor, null, Token.ADD, null, double2, null, int1, null));
    
    Assert.assertEquals(DoubleType.constant(3 - 5.3), method.invoke(visitor, null,Token.SUB, null, int1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(5.3 - 3), method.invoke(visitor, null,Token.SUB, null, double2, null, int1, null));

    Assert.assertEquals(DoubleType.constant(3 * 5.3), method.invoke(visitor, null,Token.MUL, null, int1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(5.3 * 3), method.invoke(visitor, null,Token.MUL, null, double2, null, int1, null));

    Assert.assertEquals(DoubleType.constant(3 / 5.3), method.invoke(visitor, null,Token.DIV, null, int1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(5.3 / 3), method.invoke(visitor, null,Token.DIV, null, double2, null, int1, null));

    Assert.assertEquals(DoubleType.constant(3 % 5.3), method.invoke(visitor, null,Token.MOD, null, int1, null, double2, null));
    Assert.assertEquals(DoubleType.constant(5.3 % 3), method.invoke(visitor, null,Token.MOD, null, double2, null, int1, null));

    Assert.assertEquals(TRUE_TYPE, method.invoke(visitor, null,Token.LT, null, int1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(visitor, null,Token.LT, null, double2, null, int1, null));

    Assert.assertEquals(TRUE_TYPE, method.invoke(visitor, null,Token.LTE, null, int1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(visitor, null,Token.LTE, null, double2, null, int1, null));

    Assert.assertEquals(FALSE_TYPE, method.invoke(visitor, null,Token.GT, null, int1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, method.invoke(visitor, null,Token.GT, null, double2, null, int1, null));

    Assert.assertEquals(FALSE_TYPE, method.invoke(visitor, null,Token.GTE, null, int1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, method.invoke(visitor, null,Token.GTE, null, double2, null, int1, null));

    Assert.assertEquals(FALSE_TYPE, method.invoke(visitor, null,Token.EQ, null, int1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(visitor, null,Token.EQ, null, double2, null, int1, null));

    Assert.assertEquals(TRUE_TYPE, method.invoke(visitor, null,Token.NE, null, int1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, method.invoke(visitor, null,Token.NE, null, double2, null, int1, null));
  }
  @Test
  public void testBinaryOpUnions() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    IntType int1 = range(false, 3, 3);
    DoubleType double2 = DoubleType.constant(5.3);
    UnionType union1 = UnionTest.union(range(false, 3, 4), range(false, 6, 10));
    UnionType union2 = UnionTest.union(range(false, 2, 4), DoubleType.constant(3.9));
    
    FTVisitor visitor = new FTVisitor(null, null);
    Method method = FTVisitor.class.getDeclaredMethod("visitBinaryOp", DartBinaryExpression.class, Token.class, DartExpression.class, Type.class, 
        DartExpression.class, Type.class, FlowEnv.class);
    method.setAccessible(true);
    
    Assert.assertEquals(UnionTest.union(range(false, 6, 7), range(false, 9, 13)), 
        method.invoke(visitor, null, Token.ADD, null, union1, null, int1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, method.invoke(visitor, null, Token.ADD, null, union1, null, double2, null));
    Assert.assertEquals(UnionTest.union(range(false, 6, 7), range(false, 9, 13)), 
        method.invoke(visitor, null, Token.ADD, null, int1, null, union1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, method.invoke(visitor, null, Token.ADD, null, double2, null, union1, null));

    Assert.assertEquals(UnionTest.union(range(false, 0, 1), range(false, 3, 7)), 
        method.invoke(visitor, null,Token.SUB, null, union1, null, int1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, method.invoke(visitor, null, Token.SUB, null, union1, null, double2, null));
    Assert.assertEquals(UnionTest.union(range(false, -1, 0), range(false, -7, -3)), 
        method.invoke(visitor, null, Token.SUB, null, int1, null, union1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, method.invoke(visitor, null, Token.SUB, null, double2, null, union1, null));

    Assert.assertEquals(UnionTest.union(range(false, 9, 12), range(false, 18, 30)), 
        method.invoke(visitor, null,Token.MUL, null, union1, null, int1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, method.invoke(visitor, null, Token.MUL, null, union1, null, double2, null));
    Assert.assertEquals(UnionTest.union(range(false, 9, 12), range(false, 18, 30)), 
        method.invoke(visitor, null,Token.MUL, null, int1, null, union1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, method.invoke(visitor, null, Token.MUL, null, double2, null, union1, null));

    Assert.assertEquals(range(false, 1, 3), method.invoke(visitor, null,Token.DIV, null, union1, null, int1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, method.invoke(visitor, null, Token.DIV, null, union1, null, double2, null));
    Assert.assertEquals(range(false, 0, 1), method.invoke(visitor, null,Token.DIV, null, int1, null, union1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, method.invoke(visitor, null, Token.DIV, null, double2, null, union1, null));

    Assert.assertEquals(range(false, 0, 2), method.invoke(visitor, null,Token.MOD, null, union1, null, int1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, method.invoke(visitor, null, Token.MOD, null, union1, null, double2, null));
    Assert.assertEquals(range(false, 0, 9), method.invoke(visitor, null,Token.MOD, null, int1, null, union1, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, method.invoke(visitor, null, Token.MOD, null, double2, null, union1, null));

    Assert.assertEquals(FALSE_TYPE, method.invoke(visitor, null,Token.LT, null, union1, null, int1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null, Token.LT, null, union1, null, double2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null,Token.LT, null, int1, null, union1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null, Token.LT, null, double2, null, union1, null));

    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null,Token.LTE, null, union1, null, int1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null, Token.LTE, null, union1, null, double2, null));
    Assert.assertEquals(TRUE_TYPE, method.invoke(visitor, null,Token.LTE, null, int1, null, union1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null, Token.LTE, null, double2, null, union1, null));

    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null,Token.GT, null, union1, null, int1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null, Token.GT, null, union1, null, double2, null));
    Assert.assertEquals(FALSE_TYPE, method.invoke(visitor, null,Token.GT, null, int1, null, union1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null, Token.GT, null, double2, null, union1, null));
    
    Assert.assertEquals(TRUE_TYPE, method.invoke(visitor, null,Token.GTE, null, union1, null, int1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null, Token.GTE, null, union1, null, double2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null,Token.GTE, null, int1, null, union1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null, Token.GTE, null, double2, null, union1, null));

    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null,Token.EQ, null, union1, null, int1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null, Token.EQ, null, union1, null, double2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null,Token.EQ, null, int1, null, union1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null, Token.EQ, null, double2, null, union1, null));

    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null,Token.NE, null, union1, null, int1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null, Token.NE, null, union1, null, double2, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null,Token.NE, null, int1, null, union1, null));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, method.invoke(visitor, null, Token.NE, null, double2, null, union1, null));
  }
  
  @Test
  public void testBinaryOpDynamic() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    IntType int1 = range(false, 3, 3);
    DoubleType double2 = DoubleType.constant(5.3);
    DynamicType dyn = DYNAMIC_NON_NULL_TYPE;
    
    FTVisitor visitor = new FTVisitor(null, null);
    Method method = FTVisitor.class.getDeclaredMethod("visitBinaryOp", DartBinaryExpression.class, Token.class, DartExpression.class, Type.class, 
        DartExpression.class, Type.class, FlowEnv.class);
    method.setAccessible(true);
    
    Assert.assertEquals(DYNAMIC_NON_NULL_TYPE, method.invoke(visitor, null,Token.ADD, null, dyn, null, int1, null));
    Assert.assertEquals(DYNAMIC_NON_NULL_TYPE, method.invoke(visitor, null, Token.ADD, null, dyn, null, double2, null));
    Assert.assertEquals(DYNAMIC_NON_NULL_TYPE, method.invoke(visitor, null,Token.ADD, null, int1, null, dyn, null));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, method.invoke(visitor, null, Token.ADD, null, double2, null, dyn, null));
  }
}