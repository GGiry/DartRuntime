package jdart.compiler.flow;

import static jdart.compiler.flow.FTVisitor.Liveness.ALIVE;
import static jdart.compiler.flow.FTVisitor.Liveness.DEAD;
import static jdart.compiler.type.CoreTypeRepository.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jdart.compiler.type.ArrayType;
import jdart.compiler.type.BoolType;
import jdart.compiler.type.DoubleType;
import jdart.compiler.type.DynamicType;
import jdart.compiler.type.IntType;
import jdart.compiler.type.InterfaceType;
import jdart.compiler.type.OwnerType;
import jdart.compiler.type.Type;
import jdart.compiler.type.TypeMapper;
import jdart.compiler.type.Types;
import jdart.compiler.type.UnionType;
import jdart.compiler.visitor.ASTVisitor2;

import com.google.dart.compiler.ast.DartArrayAccess;
import com.google.dart.compiler.ast.DartArrayLiteral;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartBooleanLiteral;
import com.google.dart.compiler.ast.DartDoWhileStatement;
import com.google.dart.compiler.ast.DartDoubleLiteral;
import com.google.dart.compiler.ast.DartEmptyStatement;
import com.google.dart.compiler.ast.DartExprStmt;
import com.google.dart.compiler.ast.DartExpression;
import com.google.dart.compiler.ast.DartForStatement;
import com.google.dart.compiler.ast.DartFunction;
import com.google.dart.compiler.ast.DartFunctionObjectInvocation;
import com.google.dart.compiler.ast.DartIdentifier;
import com.google.dart.compiler.ast.DartIfStatement;
import com.google.dart.compiler.ast.DartIntegerLiteral;
import com.google.dart.compiler.ast.DartMethodInvocation;
import com.google.dart.compiler.ast.DartNewExpression;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartNullLiteral;
import com.google.dart.compiler.ast.DartParameter;
import com.google.dart.compiler.ast.DartParenthesizedExpression;
import com.google.dart.compiler.ast.DartPropertyAccess;
import com.google.dart.compiler.ast.DartReturnStatement;
import com.google.dart.compiler.ast.DartStatement;
import com.google.dart.compiler.ast.DartStringLiteral;
import com.google.dart.compiler.ast.DartSuperExpression;
import com.google.dart.compiler.ast.DartThisExpression;
import com.google.dart.compiler.ast.DartThrowStatement;
import com.google.dart.compiler.ast.DartTypeNode;
import com.google.dart.compiler.ast.DartUnaryExpression;
import com.google.dart.compiler.ast.DartUnqualifiedInvocation;
import com.google.dart.compiler.ast.DartVariable;
import com.google.dart.compiler.ast.DartVariableStatement;
import com.google.dart.compiler.ast.DartWhileStatement;
import com.google.dart.compiler.parser.Token;
import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.ElementKind;
import com.google.dart.compiler.resolver.EnclosingElement;
import com.google.dart.compiler.resolver.FieldElement;
import com.google.dart.compiler.resolver.MethodElement;
import com.google.dart.compiler.resolver.MethodNodeElement;
import com.google.dart.compiler.resolver.NodeElement;
import com.google.dart.compiler.resolver.VariableElement;

public class FTVisitor extends ASTVisitor2<Type, FlowEnv> {
  final TypeHelper typeHelper;
  private final HashMap<DartNode, Type> typeMap = new HashMap<>();
  private final MethodCallResolver methodCallResolver;
  private final StatementVisitor statementVisitor;
  private Type inferredReturnType;

  public FTVisitor(TypeHelper typeHelper, MethodCallResolver methodCallResolver) {
    this.typeHelper = typeHelper;
    this.methodCallResolver = methodCallResolver;
    this.statementVisitor = new StatementVisitor();
  }

  /**
   * Collect inferred return type
   * 
   * @param type
   *          an inferred return type.
   */
  void addInferredReturnType(Type type) {
    if (inferredReturnType == null) {
      inferredReturnType = type;
      return;
    }
    inferredReturnType = Types.union(inferredReturnType, type);
  }

  public Type getInferredReturnType(Type declaredReturnType) {
    return (inferredReturnType == null) ? declaredReturnType : inferredReturnType;
  }

  enum Liveness {
    ALIVE,
    DEAD
  }

  // entry points
  public Type typeFlow(DartNode node, FlowEnv flowEnv) {
    return accept(node, flowEnv);
  }

  public Liveness liveness(DartNode node, FlowEnv flowEnv) {
    return statementVisitor.liveness(node, flowEnv);
  }

  // --- helper methods

  private static void operandIsNonNull(DartExpression expr, FlowEnv flowEnv) {
    if (!(expr instanceof DartIdentifier)) {
      return;
    }
    Element element = expr.getElement();
    if (!(element instanceof VariableElement)) {
      return;
    }
    VariableElement variable = (VariableElement) element;
    Type type = flowEnv.getType(variable);
    flowEnv.register(variable, type.asNonNull());
  }

  void changeOperandsTypes(Type type, DartBinaryExpression node, FlowEnv parameter) {
    if (type == VOID_TYPE || type == null) {
      return;
    }

    DartExpression arg1 = node.getArg1();
    DartExpression arg2 = node.getArg2();
    Type type1 = accept(arg1, parameter);
    Type type2 = accept(arg2, parameter);
    Token operator = node.getOperator();
    VariableElement element1 = (VariableElement) arg1.getElement();
    VariableElement element2 = (VariableElement) arg2.getElement();

    switch (operator) {
    case EQ_STRICT:
    case EQ:
    case NE_STRICT:
    case NE:
      if (element1 != null) {
        parameter.register(element1, type);
      }
      if (element2 != null) {
        parameter.register(element2, type);
      }
      break;
    case LTE:
    case GTE:
    case LT:
    case GT:
      if (element1 != null) {
        if (parameter.inLoop() || type1.asConstant() == null) {
          parameter.register(element1, type);
        }
      }
      if (element2 != null) {
        if (parameter.inLoop() || type2.asConstant() == null) {
          parameter.register(element2, type);
        }
      }
      break;
    default:
      throw new IllegalStateException("You must implement changeOperand for " + operator + " (" + operator.name() + ")");
    }
  }

  @Override
  protected Type accept(DartNode node, FlowEnv flowEnv) {
    Type type = super.accept(node, flowEnv);
    if (type == null) {
      return null;
    }
    // record type of the AST node
    typeMap.put(node, type);
    return type;
  }

  // Don't implement the DartTypeNode's visitor, it should be never visited.
  // The type of the corresponding Element should be used instead
  @Override
  public Type visitTypeNode(DartTypeNode node, FlowEnv unused) {
    throw new AssertionError("this method should never be called");
  }

  @Override
  public Type visitFunction(DartFunction node, FlowEnv flowEnv) {
    throw new AssertionError("this method should never be called");
  }

  @Override
  public Type visitParameter(DartParameter node, FlowEnv unused) {
    // use the declared type of the parameter
    return typeHelper.asType(true, node.getElement().getType());
  }

  // --- statements

  class StatementVisitor extends ASTVisitor2<Liveness, FlowEnv> {
    Liveness liveness(DartNode node, FlowEnv flowEnv) {
      return accept(node, flowEnv);
    }

    @Override
    public Liveness visitBlock(DartBlock node, FlowEnv flowEnv) {
      // each instruction should be compatible with void
      Liveness liveness = ALIVE;
      for (DartStatement statement : node.getStatements()) {
        liveness = accept(statement, flowEnv.expectedType(VOID_TYPE));
      }
      return liveness;
    }

    // --- statements

    @Override
    public Liveness visitReturnStatement(DartReturnStatement node, FlowEnv flowEnv) {
      DartExpression value = node.getValue();
      Type type;
      if (value != null) {
        // return should return a value compatible with
        // the function declared return type
        type = FTVisitor.this.accept(value, flowEnv.expectedType(flowEnv.getReturnType()));
      } else {
        type = VOID_TYPE;
      }
      addInferredReturnType(type);
      return DEAD;
    }

    @Override
    public Liveness visitThrowStatement(DartThrowStatement node, FlowEnv flowEnv) {
      if (node.getException() == null) {
        // TODO correctly handle the error?
        System.err.println("Throw statement: null exception");
        throw null;
      }

      accept(node.getException(), flowEnv);
      return DEAD;
    }

    @Override
    public Liveness visitVariableStatement(DartVariableStatement node, FlowEnv flowEnv) {
      for (DartVariable variable : node.getVariables()) {
        accept(variable, flowEnv);
      }
      return ALIVE;
    }

    @Override
    public Liveness visitVariable(DartVariable node, FlowEnv flowEnv) {
      DartExpression value = node.getValue();
      if (value == null) {
        // variable is not initialized, in Dart variables are initialized
        // with null by default
        flowEnv.register(node.getElement(), NULL_TYPE);
        return null;
      }
      // the type is the type of the initialization expression
      VariableElement element = node.getElement();
      Type declaredType = typeHelper.asType(true, element.getType());
      Type type = FTVisitor.this.accept(value, flowEnv.expectedType(declaredType));
      flowEnv.register(element, type);
      return null;
    }

    @Override
    public Liveness visitExprStmt(DartExprStmt node, FlowEnv flowEnv) {
      DartExpression expression = node.getExpression();
      if (expression != null) {
        // statement expression expression should return void
        FTVisitor.this.accept(expression, flowEnv.expectedType(VOID_TYPE));
      }
      return null;
    }

    @Override
    public Liveness visitIfStatement(DartIfStatement node, FlowEnv parameter) {
      DartExpression condition = node.getCondition();
      Type conditionType = FTVisitor.this.accept(condition, parameter.expectedType(BOOL_NON_NULL_TYPE));

      FTVisitor.ConditionVisitor conditionVisitor = new ConditionVisitor(FTVisitor.this);

      FlowEnv envThen = new FlowEnv(parameter);
      FlowEnv envElse = new FlowEnv(parameter);

      conditionVisitor.accept(condition, new ConditionEnv(parameter, envThen, envElse));
      Liveness trueLiveness = DEAD;
      if (conditionType != FALSE_TYPE) {
        trueLiveness = accept(node.getThenStatement(), envThen);
        parameter.merge(envThen);
      }
      Liveness falseLiveness = ALIVE;
      if (node.getElseStatement() != null) {
        falseLiveness = DEAD;
        if (conditionType != TRUE_TYPE) {
          falseLiveness = accept(node.getElseStatement(), envElse);
          parameter.merge(envElse);
        }
      } else {
        if (trueLiveness == DEAD) {
          parameter.copyAll(envElse);
        }
      }
      return (trueLiveness == ALIVE && falseLiveness == ALIVE) ? ALIVE : DEAD;
    }

    private Liveness computeLoop(DartExpression condition, DartStatement body, DartStatement /*
     * maybe
     * null
     */init, DartExpression /*
     * maybe
     * null
     */increment, FlowEnv parameter) {
      FlowEnv loopEnv = new FlowEnv(parameter, parameter.getReturnType(), parameter.getExpectedType(), true);
      FlowEnv afterLoopEnv = new FlowEnv(parameter, parameter.getReturnType(), parameter.getExpectedType(), false);
      if (init != null) {
        accept(init, loopEnv);
      }

      // condition should be a boolean
      FTVisitor.this.accept(condition, loopEnv.expectedType(BOOL_NON_NULL_TYPE));
      FTVisitor.ConditionVisitor conditionVisitor = new ConditionVisitor(FTVisitor.this);

      FTVisitor.LoopVisitor loopVisitor = new LoopVisitor();
      Set<VariableElement> list = loopVisitor.accept(body, parameter);
      System.out.println(list);

      for (VariableElement element : list) {
        Type type = parameter.getType(element);
        if (type != null) {
          parameter.register(element, Types.widening(type));
        }
      }

      conditionVisitor.accept(condition, new ConditionEnv(parameter, loopEnv, afterLoopEnv));
      accept(body, loopEnv);
      if (increment != null) {
        FTVisitor.this.accept(increment, loopEnv);
      }
      FTVisitor.this.accept(condition, loopEnv.expectedType(BOOL_NON_NULL_TYPE));

      parameter.copyAll(loopEnv);
      parameter.copyAll(afterLoopEnv);
      return ALIVE;
    }

    @Override
    public Liveness visitForStatement(DartForStatement node, FlowEnv parameter) {
      return computeLoop(node.getCondition(), node.getBody(), node.getInit(), node.getIncrement(), parameter);
    }

    @Override
    public Liveness visitWhileStatement(DartWhileStatement node, FlowEnv parameter) {
      return computeLoop(node.getCondition(), node.getBody(), null, null, parameter);
    }

    @Override
    public Liveness visitDoWhileStatement(DartDoWhileStatement node, FlowEnv parameter) {
      return computeLoop(node.getCondition(), node.getBody(), null, null, parameter);
    }

    @Override
    public Liveness visitEmptyStatement(DartEmptyStatement node, FlowEnv parameter) {
      return ALIVE;
    }
  }

  static class ConditionEnv {
    final FlowEnv trueEnv;
    final FlowEnv falseEnv;
    final FlowEnv parent;

    public ConditionEnv(FlowEnv parent, FlowEnv trueEnv, FlowEnv falseEnv) {
      this.parent = parent;
      this.trueEnv = trueEnv;
      this.falseEnv = falseEnv;
    }

    /**
     * @return the environment representing variables when condition is true.
     */
    public FlowEnv getTrueEnv() {
      return trueEnv;
    }

    /**
     * @return the environment representing variables when condition is false.
     */
    public FlowEnv getFalseEnv() {
      return falseEnv;
    }

    /**
     * @return the environment before condition.
     */
    public FlowEnv getParent() {
      return parent;
    }
  }

  static class ConditionVisitor extends ASTVisitor2<Void, FTVisitor.ConditionEnv> {
    private final FTVisitor visitor;

    public ConditionVisitor(FTVisitor visitor) {
      this.visitor = visitor;
    }

    @Override
    protected Void accept(DartNode node, FTVisitor.ConditionEnv parameter) {
      return super.accept(node, parameter);
    }

    @Override
    public Void visitBinaryExpression(DartBinaryExpression node, FTVisitor.ConditionEnv parameter) {
      DartExpression arg1 = node.getArg1();
      DartExpression arg2 = node.getArg2();
      Type type1 = visitor.accept(arg1, parameter.parent);
      Type type2 = visitor.accept(arg2, parameter.parent);
      Element element1 = arg1.getElement();
      Element element2 = arg2.getElement();
      Token operator = node.getOperator();
      switch (operator) {
      case EQ_STRICT:
      case EQ: {
        Type commonValues = type1.commonValuesWith(type2);
        if (element1 != null && element1 instanceof VariableElement) {
          parameter.trueEnv.register((VariableElement) element1, commonValues);
          parameter.falseEnv.register((VariableElement) element1, type1.exclude(type2));
        }
        if (element2 != null && element2 instanceof VariableElement) {
          parameter.trueEnv.register((VariableElement) element2, commonValues);
          parameter.falseEnv.register((VariableElement) element2, type2.exclude(type1));
        }
        break;
      }
      case NE_STRICT:
      case NE: {
        Type commonValues = type1.commonValuesWith(type2);
        if (element1 != null && element1 instanceof VariableElement) {
          parameter.trueEnv.register((VariableElement) element1, type1.exclude(type2));
          parameter.falseEnv.register((VariableElement) element1, commonValues);
        }
        if (element2 != null && element2 instanceof VariableElement) {
          parameter.trueEnv.register((VariableElement) element2, type2.exclude(type1));
          parameter.falseEnv.register((VariableElement) element2, commonValues);
        }
        break;
      }
      case LTE: {
        if (element1 != null && element1 instanceof VariableElement) {
          Type lessThanOrEqualsValues = type1.lessThanOrEqualsValues(type2, parameter.trueEnv.inLoop());
          Type greaterThanValues = type1.greaterThanValues(type2, parameter.falseEnv.inLoop());
          if (lessThanOrEqualsValues != null) {
            parameter.trueEnv.register((VariableElement) element1, lessThanOrEqualsValues);
          }
          if (greaterThanValues != null) {
            parameter.falseEnv.register((VariableElement) element1, greaterThanValues);
          }
        }
        if (element2 != null && element2 instanceof VariableElement) {
          parameter.trueEnv.register((VariableElement) element2, type2);
          parameter.falseEnv.register((VariableElement) element2, type2);
        }
        break;
      }
      case GTE: {
        if (element1 != null && element1 instanceof VariableElement) {
          Type greaterThanOrEqualsValues = type1.greaterThanOrEqualsValues(type2, parameter.trueEnv.inLoop());
          Type lessThanValues = type1.lessThanValues(type2, parameter.falseEnv.inLoop());
          if (greaterThanOrEqualsValues != null) {
            parameter.trueEnv.register((VariableElement) element1, greaterThanOrEqualsValues);
          }
          if (lessThanValues != null) {
            parameter.falseEnv.register((VariableElement) element1, lessThanValues);
          }
        }
        if (element2 != null && element2 instanceof VariableElement) {
          parameter.trueEnv.register((VariableElement) element2, type2);
          parameter.falseEnv.register((VariableElement) element2, type2);
        }
        break;
      }
      case LT: {
        Type lessThanValues = type1.lessThanValues(type2, parameter.trueEnv.inLoop());
        Type greaterThanOrEqualsValues = type1.greaterThanOrEqualsValues(type2, parameter.falseEnv.inLoop());
        if (element1 != null && element1 instanceof VariableElement) {
          if (lessThanValues != null) {
            parameter.trueEnv.register((VariableElement) element1, lessThanValues);
          }
          if (greaterThanOrEqualsValues != null) {
            parameter.falseEnv.register((VariableElement) element1, greaterThanOrEqualsValues);
          }
        }
        if (element2 != null && element2 instanceof VariableElement) {
          parameter.trueEnv.register((VariableElement) element2, type2);
          parameter.falseEnv.register((VariableElement) element2, type2);
        }
        break;
      }
      case GT: {
        Type greaterThanValues = type1.greaterThanValues(type2, parameter.trueEnv.inLoop());
        Type lessThanOrEqualsValues = type1.lessThanOrEqualsValues(type2, parameter.falseEnv.inLoop());
        if (element1 != null && element1 instanceof VariableElement) {
          if (greaterThanValues != null) {
            parameter.trueEnv.register((VariableElement) element1, greaterThanValues);
          }
          if (lessThanOrEqualsValues != null) {
            parameter.falseEnv.register((VariableElement) element1, lessThanOrEqualsValues);
          }
        }
        if (element2 != null && element2 instanceof VariableElement) {
          parameter.trueEnv.register((VariableElement) element2, type2);
          parameter.falseEnv.register((VariableElement) element2, type2);
        }
        break;
      }
      case AND: {
        FlowEnv cpyTrue = new FlowEnv(parameter.trueEnv);
        FlowEnv cpyFalse = new FlowEnv(parameter.falseEnv);
        FlowEnv cpyParent = new FlowEnv(parameter.parent);

        FTVisitor.ConditionEnv conditionEnv = new ConditionEnv(cpyParent, cpyTrue, cpyFalse);
        accept(arg1, conditionEnv);
        parameter.trueEnv.mergeCommonValues(cpyTrue);
        accept(arg2, conditionEnv);
        parameter.trueEnv.mergeCommonValues(cpyTrue);
        break;
      }
      case OR: {
        FlowEnv cpyTrue = new FlowEnv(parameter.trueEnv);
        FlowEnv cpyFalse = new FlowEnv(parameter.falseEnv);
        FlowEnv cpyParent = new FlowEnv(parameter.parent);

        FTVisitor.ConditionEnv conditionEnv = new ConditionEnv(cpyParent, cpyTrue, cpyFalse);
        accept(arg1, conditionEnv);
        parameter.falseEnv.mergeCommonValues(cpyFalse);
        accept(arg2, conditionEnv);
        parameter.falseEnv.merge(cpyFalse);
        break;
      }

      default:
        throw new IllegalStateException("You have to implement ConditionVisitor.visitBinaryExpression() for " + operator + " (" + operator.name() + ")");
      }
      return null;
    }

    @Override
    public Void visitBooleanLiteral(DartBooleanLiteral node, FTVisitor.ConditionEnv parameter) {
      return null;
    }
  }

  static class LoopVisitor extends ASTVisitor2<Set<VariableElement>, FlowEnv> {
    public LoopVisitor() {
      super();
    }

    @Override
    protected Set<VariableElement> accept(DartNode node, FlowEnv parameter) {
      return super.accept(node, parameter);
    }

    @Override
    public Set<VariableElement> visitBlock(DartBlock node, FlowEnv parameter) {
      HashSet<VariableElement> list = new HashSet<>();
      for (DartStatement statement : node.getStatements()) {
        list.addAll(accept(statement, parameter));
      }
      return list;
    }

    @Override
    public Set<VariableElement> visitIfStatement(DartIfStatement node, FlowEnv parameter) {
      HashSet<VariableElement> list = new HashSet<>();
      list.addAll(accept(node.getThenStatement(), parameter));
      if (node.getElseStatement() != null) {
        list.addAll(accept(node.getElseStatement(), parameter));
      }
      return list;
    }

    @Override
    public Set<VariableElement> visitExprStmt(DartExprStmt node, FlowEnv parameter) {
      HashSet<VariableElement> list = new HashSet<>();
      list.addAll(accept(node.getExpression(), parameter));
      return list;
    }

    @Override
    public Set<VariableElement> visitBinaryExpression(DartBinaryExpression node, FlowEnv parameter) {
      HashSet<VariableElement> list = new HashSet<>();
      if (node.getOperator().isAssignmentOperator()) {
        list.add((VariableElement) node.getArg1().getElement());
      }
      return list;
    }
  }

  // --- expressions
  @Override
  public Type visitIdentifier(DartIdentifier node, FlowEnv flowEnv) {
    NodeElement element = node.getElement();
    switch (element.getKind()) {
    case VARIABLE:
    case PARAMETER:
      return flowEnv.getType((VariableElement) element);
    case FIELD:
      return typeHelper.asType(true, element.getType());
    case METHOD:
      // reference a method by name
      return typeHelper.asType(false, element.getType());
    default:
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Type visitThisExpression(DartThisExpression node, FlowEnv flowEnv) {
    // the type of this is stored in the flow env
    return flowEnv.getThisType();
  }

  @Override
  public Type visitBinaryExpression(DartBinaryExpression node, FlowEnv parameter) {
    DartExpression arg1 = node.getArg1();
    DartExpression arg2 = node.getArg2();
    Type type1 = accept(arg1, parameter);
    Type type2 = accept(arg2, parameter);
    Token operator = node.getOperator();

    if (!operator.isAssignmentOperator()) {
      return visitBinaryOp(node, operator, arg1, type1, arg2, type2, parameter);
    }

    // if it's an assignment, rewrite it as a binary operator
    Type resultType;
    if (operator == Token.ASSIGN) {
      resultType = type2;
    } else {
      Token binaryOp = operator.asBinaryOperator();
      // workaround issue https://code.google.com/p/dart/issues/detail?id=3033
      if (operator == Token.ASSIGN_MOD) {
        binaryOp = Token.MOD;
      } else if (operator == Token.ASSIGN_TRUNC) {
        binaryOp = Token.TRUNC;
      }
      resultType = visitBinaryOp(node, binaryOp, arg1, type1, arg2, type2, parameter);
    }

    Element element1 = arg1.getElement();
    switch (element1.getKind()) {
    case VARIABLE:
    case PARAMETER:
      parameter.register((VariableElement) element1, resultType);
      return resultType;
    case FIELD:
      return resultType;
    default:
      throw new AssertionError("Assignment Expr: " + element1.getKind() + " not implemented");
    }
  }

  /*TODO 
  private */static Type opIntInt(Token operator, DartExpression arg1, Type type1, DartExpression arg2, Type type2, FlowEnv flowEnv) {
    IntType iType1 = (IntType) type1;
    IntType iType2 = (IntType) type2;
    switch (operator) {
    case NE:
    case NE_STRICT:
      return iType1.hasCommonValuesWith(iType2) ? BOOL_NON_NULL_TYPE : TRUE_TYPE;
    case EQ:
    case EQ_STRICT:
      return iType1.hasCommonValuesWith(iType2) ? BOOL_NON_NULL_TYPE : FALSE_TYPE;
    case LT:
      return iType1.isStrictLT(iType2) ? TRUE_TYPE : iType2.isStrictLTE(iType1) ? FALSE_TYPE : BOOL_NON_NULL_TYPE;
    case LTE:
      return iType1.isStrictLTE(iType2) ? TRUE_TYPE : iType2.isStrictLT(iType1) ? FALSE_TYPE : BOOL_NON_NULL_TYPE;
    case GT:
      return iType2.isStrictLT(iType1) ? TRUE_TYPE : iType1.isStrictLTE(iType2) ? FALSE_TYPE : BOOL_NON_NULL_TYPE;
    case GTE:
      return iType2.isStrictLTE(iType1) ? TRUE_TYPE : iType1.isStrictLT(iType2) ? FALSE_TYPE : BOOL_NON_NULL_TYPE;
    case ADD:
    case SUB:
    case MUL:
    case DIV:
    case MOD:
    case BIT_AND:
    case BIT_OR:
    case BIT_XOR:
    case SHL:
    case SAR:
      operandIsNonNull(arg1, flowEnv);
      operandIsNonNull(arg2, flowEnv);

      switch (operator) {
      case ADD:
        return iType1.add(iType2);
      case SUB:
        return iType1.sub(iType2);
      case MUL:
        return iType1.mul(iType2);
      case DIV:
        return iType1.div(iType2);
      case MOD:
        return iType1.mod(iType2);
      case BIT_AND:
        return iType1.bitAnd(iType2);
      case BIT_OR:
        return iType1.bitOr(iType2);
      case BIT_XOR:
        return iType1.bitXor(iType2);
      case SHL:
        return iType1.shiftLeft(iType2);
      case SAR:
        return iType1.shiftRight(iType2);
      default:
      }
    default:
      throw new AssertionError("Binary Expr: " + type1 + " " + operator + " " + type2 + " (" + operator.name() + ") not implemented");
    }
  }

  /*TODO 
  private */static Type opDoubleDouble(Token operator, DartExpression arg1, Type type1, DartExpression arg2, Type type2, FlowEnv flowEnv) {
    Double asConstant1 = ((DoubleType) type1).asConstant();
    Double asConstant2 = ((DoubleType) type2).asConstant();
    switch (operator) {
    case NE:
    case NE_STRICT:
      if (asConstant1 != null && asConstant2 != null) {
        return asConstant1.equals(asConstant2) ? FALSE_TYPE : TRUE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case EQ:
    case EQ_STRICT:
      if (asConstant1 != null && asConstant2 != null) {
        return asConstant1.equals(asConstant2) ? TRUE_TYPE : FALSE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case LT:
      if (asConstant1 != null && asConstant2 != null) {
        return (asConstant1.compareTo(asConstant2) < 0) ? TRUE_TYPE : FALSE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case LTE:
      if (asConstant1 != null && asConstant2 != null) {
        return (asConstant1.compareTo(asConstant2) <= 0) ? TRUE_TYPE : FALSE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case GT:
      if (asConstant1 != null && asConstant2 != null) {
        return (asConstant1.compareTo(asConstant2) > 0) ? TRUE_TYPE : FALSE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case GTE:
      if (asConstant1 != null && asConstant2 != null) {
        return (asConstant1.compareTo(asConstant2) >= 0) ? TRUE_TYPE : FALSE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case ADD:
    case SUB:
    case MOD:
    case BIT_AND:
    case BIT_OR:
    case BIT_XOR:
      operandIsNonNull(arg1, flowEnv);
      operandIsNonNull(arg2, flowEnv);

      if (asConstant1 != null && asConstant2 != null) {
        switch (operator) {
        case ADD:
          return DoubleType.constant(asConstant1 + asConstant2);
        case SUB:
          return DoubleType.constant(asConstant1 - asConstant2);
        case MOD:
          return DoubleType.constant(asConstant1 % asConstant2);
        case BIT_AND:
          return DoubleType.constant(asConstant1.byteValue() & asConstant2.byteValue());
        case BIT_OR:
          return DoubleType.constant(asConstant1.byteValue() | asConstant2.byteValue());
        case BIT_XOR:
          return DoubleType.constant(asConstant1.byteValue() ^ asConstant2.byteValue());
        default:
        }
      }
    default:
      throw new AssertionError("Binary Expr: " + type1 + " " + operator + " " + type2 + " (" + operator.name() + ") not implemented");
    }
  }

  /*TODO 
  private */static Type opIntDouble(Token operator, DartExpression arg1, Type type1, DartExpression arg2, Type type2, FlowEnv flowEnv) {
    IntType iType1 = (IntType) type1;
    DoubleType dType2 = (DoubleType) type2;

    BigInteger maxBound = iType1.getMaxBound();
    BigInteger minBound = iType1.getMinBound();
    Double asConstant2 = dType2.asConstant();
    BigInteger asConstant1 = iType1.asConstant();
    switch (operator) {
    case NE:
    case NE_STRICT:
      if (dType2.isIncludeIn(iType1)) {
        if (asConstant1 != null) {
          return FALSE_TYPE;
        }
        return BOOL_NON_NULL_TYPE;
      }
      return TRUE_TYPE;
    case EQ:
    case EQ_STRICT:
      if (dType2.isIncludeIn(iType1)) {
        if (asConstant1 != null) {
          return TRUE_TYPE;
        }
        return BOOL_NON_NULL_TYPE;
      }
      return FALSE_TYPE;
    case LT:
      if (maxBound != null && (maxBound.doubleValue() < asConstant2)) {
        return TRUE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case LTE:
      if (maxBound != null && (maxBound.doubleValue() <= asConstant2)) {
        return TRUE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case GT:
      if (minBound != null && (minBound.doubleValue() > asConstant2)) {
        return TRUE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case GTE:
      if (minBound != null && (minBound.doubleValue() > asConstant2)) {
        return TRUE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;

    case ADD:
    case SUB:
    case MOD:
    case BIT_AND:
    case BIT_OR:
    case BIT_XOR:
      operandIsNonNull(arg1, flowEnv);
      operandIsNonNull(arg2, flowEnv);

      if (asConstant2 != null) {
        switch (operator) {
        case ADD:
          if (asConstant1 != null) {
            return DoubleType.constant(asConstant1.doubleValue() + asConstant2);
          }
          return DOUBLE_NON_NULL_TYPE;
        case SUB:
          if (asConstant1 != null) {
            return DoubleType.constant(asConstant1.doubleValue() - asConstant2);
          }
          return DOUBLE_NON_NULL_TYPE;
        case MOD:
          if (asConstant1 != null) {
            return DoubleType.constant(asConstant1.doubleValue() % asConstant2);
          }
          return DOUBLE_NON_NULL_TYPE;
        case BIT_AND:
          if (asConstant1 != null) {
            return IntType.constant(asConstant1.and(BigInteger.valueOf(asConstant2.longValue())));
          }
          return DOUBLE_NON_NULL_TYPE;
        case BIT_OR:
          if (asConstant1 != null) {
            return IntType.constant(asConstant1.or(BigInteger.valueOf(asConstant2.longValue())));
          }
          return DOUBLE_NON_NULL_TYPE;
        case BIT_XOR:
          if (asConstant1 != null) {
            return IntType.constant(asConstant1.xor(BigInteger.valueOf(asConstant2.longValue())));
          }
          return DOUBLE_NON_NULL_TYPE;
        }
      } else {
        // asConstant2 == null
        return DOUBLE_NON_NULL_TYPE;
      }
    default:
      throw new AssertionError("Binary Expr: " + type1 + " " + operator + " " + type2 + " (" + operator.name() + ") not implemented");
    }
  }

  /*TODO 
  private */static  Type opDoubleInt(Token operator, DartExpression arg1, Type type1, DartExpression arg2, Type type2, FlowEnv flowEnv) {
    DoubleType dType1 = (DoubleType) type1;
    IntType iType2 = (IntType) type2;

    Double asConstant1 = dType1.asConstant();
    BigInteger maxBound = iType2.getMaxBound();
    BigInteger minBound = iType2.getMinBound();
    BigInteger asConstant2 = iType2.asConstant();
    switch (operator) {
    case NE:
    case NE_STRICT:
      if (dType1.isIncludeIn(iType2)) {
        if (asConstant1 != null) {
          return FALSE_TYPE;
        }
        return BOOL_NON_NULL_TYPE;
      }
      return TRUE_TYPE;
    case EQ:
    case EQ_STRICT:
      if (dType1.isIncludeIn(iType2)) {
        if (asConstant1 != null) {
          return TRUE_TYPE;
        }
        return BOOL_NON_NULL_TYPE;
      }
      return FALSE_TYPE;
    case LT:
      if (maxBound != null && (asConstant1 < maxBound.doubleValue())) {
        return TRUE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case LTE:
      if (maxBound != null && (asConstant1 <= maxBound.doubleValue())) {
        return TRUE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case GT:
      if (minBound != null && (asConstant1 > maxBound.doubleValue())) {
        return TRUE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case GTE:
      if (minBound != null && (asConstant1 >= maxBound.doubleValue())) {
        return TRUE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;

    case ADD:
    case SUB:
    case MOD:
    case BIT_AND:
    case BIT_OR:
    case BIT_XOR:
      operandIsNonNull(arg1, flowEnv);
      operandIsNonNull(arg2, flowEnv);

      if (asConstant2 != null) {
        switch (operator) {
        case ADD:
          if (asConstant1 != null) {
            return DoubleType.constant(asConstant1 + asConstant2.doubleValue());
          }
          return DOUBLE_NON_NULL_TYPE;
        case SUB:
          if (asConstant1 != null) {
            return DoubleType.constant(asConstant1 - asConstant2.doubleValue());
          }
          return DOUBLE_NON_NULL_TYPE;
        case MOD:
          if (asConstant1 != null) {
            return DoubleType.constant(asConstant1 % asConstant2.doubleValue());
          }
          return DOUBLE_NON_NULL_TYPE;
        case BIT_AND:
          if (asConstant1 != null) {
            return IntType.constant(asConstant2.and(BigInteger.valueOf(asConstant1.longValue())));
          }
          return DOUBLE_NON_NULL_TYPE;
        case BIT_OR:
          if (asConstant1 != null) {
            return IntType.constant(asConstant2.or(BigInteger.valueOf(asConstant1.longValue())));
          }
          return DOUBLE_NON_NULL_TYPE;
        case BIT_XOR:
          if (asConstant1 != null) {
            return IntType.constant(asConstant2.xor(BigInteger.valueOf(asConstant1.longValue())));
          }
          return DOUBLE_NON_NULL_TYPE;
        }
      } else {
        // asConstant2 == null
        return DOUBLE_NON_NULL_TYPE;
      }
    default:
      throw new AssertionError("Binary Expr: " + type1 + " " + operator + " " + type2 + " (" + operator.name() + ") not implemented");
    }
  }

  /*TODO 
  private */static Type opBoolBool(Token operator, DartExpression arg1, Type type1, DartExpression arg2, Type type2, FlowEnv flowEnv) {
    BoolType bType1 = (BoolType) type1;
    BoolType bType2 = (BoolType) type2;
    Boolean asConstant1 = bType1.asConstant();
    Boolean asConstant2 = bType2.asConstant();

    switch (operator) {
    case NE:
    case NE_STRICT:
      if (bType1.equals(bType2)) {
        return FALSE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case EQ:
    case EQ_STRICT:
      if (bType1.equals(bType2)) {
        return TRUE_TYPE;
      }
      return BOOL_NON_NULL_TYPE;
    case AND:
    case OR:
      operandIsNonNull(arg1, flowEnv);
      operandIsNonNull(arg2, flowEnv);

      if (asConstant1 != null) {
        switch (operator) {
        case AND:
          if (asConstant2 != null) {
            return BoolType.constant(asConstant1 && asConstant2);
          }
          return BOOL_NON_NULL_TYPE;
        case OR:
          if (asConstant2 != null) {
            return BoolType.constant(asConstant1 || asConstant2);
          }
          return BOOL_NON_NULL_TYPE;
        }
      } else {
        // asConstant2 == null
        return BOOL_NON_NULL_TYPE;
      }
    default:
      throw new AssertionError("Binary Expr: " + type1 + " " + operator + " " + type2 + " (" + operator.name() + ") not implemented");
    }
  }

  private Type visitBinaryOp(final DartBinaryExpression node, final Token operator, final DartExpression arg1, final Type type1, 
      final DartExpression arg2, final Type type2, final FlowEnv flowEnv) {

    if (type1 instanceof IntType && type2 instanceof IntType) {
      return opIntInt(operator, arg1, type1, arg2, type2, flowEnv);
    }

    if (type1 instanceof DoubleType && type2 instanceof DoubleType) {
      return opDoubleDouble(operator, arg1, type1, arg2, type2, flowEnv);
    }

    if (type1 instanceof DoubleType && type2 instanceof DoubleType) {
      return opIntDouble(operator, arg1, type1, arg2, type2, flowEnv);
    }

    if (type1 instanceof DoubleType && type2 instanceof DoubleType) {
      return opDoubleInt(operator, arg1, type1, arg2, type2, flowEnv);
    }

    if (type1 instanceof BoolType && type2 instanceof BoolType) {
      return opBoolBool(operator, arg1, type1, arg2, type2, flowEnv);
    }
    
    if (type1 instanceof DynamicType) {
      return Types.widening(type2);
    }
    
    if (type2 instanceof DynamicType) {
      return Types.widening(type1);
    }

    if (type1 instanceof UnionType) {
      type1.map(new TypeMapper() {
        @Override
        public Type transform(Type type) {
          visitBinaryOp(node, operator, arg1, type, arg2, type2, flowEnv);
          return null;
        }
      });
    }

    if (type2 instanceof UnionType) {
      type1.map(new TypeMapper() {
        @Override
        public Type transform(Type type) {
          visitBinaryOp(node, operator, arg1, type1, arg2, type, flowEnv);
          return null;
        }
      });
    }

    // a method call that can be polymorphic
    if (node.getElement() == null) {
      // FIXME A setter with a dynamic parameter will make the field dynamic.
      // (see PropertyAccess2.dart)
      System.err.println("NoSuchMethodException: " + node.getOperator() + " for type: " + type1 + ", " + type2);
      return DYNAMIC_TYPE;
    }
    return typeHelper.asType(true, node.getElement().getFunctionType().getReturnType());
  }

  @Override
  public Type visitUnaryExpression(DartUnaryExpression node, FlowEnv parameter) {
    DartExpression arg = node.getArg();
    return visitUnaryOperation(node, node.getOperator(), arg, accept(arg, parameter), parameter);
  }

  private static Type visitUnaryOperation(DartUnaryExpression node, Token operator, DartExpression arg, Type type, FlowEnv flowEnv) {
    // FIXME Geoffrey, Dart allow to call unary/binary operators on 'num' too
    // (the interface type)
    switch (operator) {
    case INC:
      if (type instanceof IntType) {
        IntType iType = (IntType) type;
        return iType.add(IntType.constant(BigInteger.ONE));
      }
      if (type instanceof DoubleType) {
        DoubleType dType = (DoubleType) type;
        return dType.add(DoubleType.constant(1.));
      }
      // FIXME Geoffrey, unary operator can be overriden
      return DYNAMIC_NON_NULL_TYPE;
    case SUB:
      if (type instanceof IntType) {
        IntType iType = (IntType) type;
        return iType.unarySub();
      }
      if (type instanceof DoubleType) {
        DoubleType dType = (DoubleType) type;
        return dType.unarySub();
      }
      // FIXME Geoffrey, unary operator can be overriden
      return DYNAMIC_NON_NULL_TYPE;
    default:
      throw new UnsupportedOperationException("Unary Expr: " + operator + " (" + operator.name() + ") not implemented for " + type + ".");
    }
  }

  @Override
  public Type visitNewExpression(DartNewExpression node, FlowEnv flowEnv) {
    ArrayList<Type> argumentTypes = new ArrayList<>();
    for (DartExpression argument : node.getArguments()) {
      argumentTypes.add(accept(argument, flowEnv));
    }

    ClassElement element = node.getElement().getConstructorType();
    return typeHelper.findType(false, element);
  }

  @Override
  public Type visitSuperExpression(DartSuperExpression node, FlowEnv parameter) {
    if (parameter.getThisType() == null) {
      return DYNAMIC_TYPE;
    }

    Type type = ((OwnerType) parameter.getThisType()).getSuperType();
    return type;
  }

  @Override
  public Type visitParenthesizedExpression(DartParenthesizedExpression node, FlowEnv parameter) {
    return accept(node.getExpression(), parameter);
  }

  // --- Invocation

  @Override
  public Type visitMethodInvocation(final DartMethodInvocation node, FlowEnv flowEnv) {
    ArrayList<Type> argumentTypes = new ArrayList<>();
    for (DartExpression argument : node.getArguments()) {
      argumentTypes.add(accept(argument, flowEnv));
    }

    Element targetElement = node.getTarget().getElement();
    if (targetElement != null) {
      switch (targetElement.getKind()) {
      case CLASS: // static field or method
      case SUPER: // super field or method
      case LIBRARY: // library call

        NodeElement nodeElement = node.getElement();
        switch (nodeElement.getKind()) {
        case FIELD: // field access
          return Types.getReturnType(typeHelper.asType(true, ((FieldElement) nodeElement).getType()));

        case METHOD: { // statically resolved method call
          /*
           * emulate a call FIXME FlowEnv newFlowEnv = new FlowEnv(null);
           * for(Type argumentType: argumentTypes) {
           * newFlowEnv.register(variable, argumentType); } return new
           * FTVisitor(
           * typeRepository).accept(((MethodNodeElement)element).getNode(),
           * newFlowEnv);
           */
          return typeHelper.asType(true, ((MethodElement) nodeElement).getReturnType());
        }

        default:
          throw new UnsupportedOperationException();
        }

      default: // polymorphic method call
      }
    }

    Type receiverType = accept(node.getTarget(), flowEnv);

    // call on 'null' (statically proven), will never succeed at runtime
    if (receiverType == NULL_TYPE) {
      return DYNAMIC_NON_NULL_TYPE;
    }

    // if the receiver is null, it will raise an exception at runtime
    // so mark it non null after that call
    operandIsNonNull(node.getTarget(), flowEnv);

    // you can call what you want on dynamic
    if (receiverType instanceof DynamicType) {
      operandIsNonNull(node.getTarget(), flowEnv);
      return receiverType;
    }

    return receiverType.map(new TypeMapper() {
      @Override
      public Type transform(Type type) {
        OwnerType ownerType = (OwnerType) type;
        Element element = ownerType.lookupMember(node.getFunctionNameString());

        // element can be null because the receiverType can be an union
        if (element == null) {
          return null;
        }

        // here when you should use the Class Hierarchy analysis and
        // typeflow all overridden methods
        // but let do something simple for now.
        return typeHelper.asType(true, ((MethodElement) element).getReturnType());
      }
    });
  }

  @Override
  public Type visitUnqualifiedInvocation(DartUnqualifiedInvocation node, FlowEnv flowEnv) {
    ArrayList<Type> argumentTypes = new ArrayList<>();
    for (DartExpression argument : node.getArguments()) {
      argumentTypes.add(accept(argument, flowEnv));
    }

    // weird, element is set on target ?
    NodeElement nodeElement = node.getTarget().getElement();
    if (nodeElement == null) {
      // here we are in trouble, I don't know why this can appear
      // for the moment, log the error and say that the result is dynamic
      System.err.println("Unqualified Invocation: Element null: " + node);

      // Element element = ((OwnerType)
      // flowEnv.getThisType()).lookupMember(node.getTarget().getName());
      return DYNAMIC_TYPE;
    }

    // Because of invoke, the parser doesn't set the value of element.
    switch (nodeElement.getKind()) {
    case METHOD: { // polymorphic method call on 'this'
      EnclosingElement enclosingElement = nodeElement.getEnclosingElement();
      if (enclosingElement instanceof ClassElement) {
        Type receiverType = typeHelper.findType(false, (ClassElement) enclosingElement);
        return methodCallResolver.methodCall(node.getObjectIdentifier(), receiverType, argumentTypes, flowEnv.getExpectedType(), true);
      }
      // FIXME should use another method of the methodResolver (but it doesn't
      // exist now)
      return methodCallResolver.functionCall((MethodNodeElement) nodeElement, argumentTypes, flowEnv.getExpectedType());
      // return typeHelper.asType(true, ((MethodElement)
      // nodeElement).getReturnType());
    }

    case FIELD: // function call
    case PARAMETER:
      return Types.getReturnType(typeHelper.asType(true, nodeElement.getType()));
    case VARIABLE:
      return Types.getReturnType(flowEnv.getType((VariableElement) nodeElement));

    default: // FUNCTION_OBJECT ??
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Type visitFunctionObjectInvocation(DartFunctionObjectInvocation node, FlowEnv parameter) {
    // FIXME Geoffrey, a function object is when you have a function stored in
    // a parameter/variable or a result of another call that you call as a
    // function
    // something like :
    // int foo(int i) { return i; }
    // var a = foo;
    // a(); <--- function object invocation
    // so either it's a call to dynamic or a function type (so uses
    // Types.getReturnType())

    // FIXME
    // The test file DartTest/FunctionObject.dart seems to not call
    // visitFunctionObjectInvocation but UnqualifiedInvocation.

    Type targetType = accept(node.getTarget(), parameter);

    // We need to setElement.
    node.setElement(((OwnerType) parameter.getThisType()).getSuperType().getElement());
    // TODO be sure only super is called.
    System.out.println("visitFunctionObjectInvoke: " + node + " : " + node.getElement());

    return null;
  }

  // --- literals

  @Override
  public Type visitIntegerLiteral(DartIntegerLiteral node, FlowEnv unused) {
    return IntType.constant(node.getValue());
  }

  @Override
  public Type visitDoubleLiteral(DartDoubleLiteral node, FlowEnv unused) {
    return DoubleType.constant(node.getValue());
  }

  @Override
  public Type visitBooleanLiteral(DartBooleanLiteral node, FlowEnv unused) {
    return BoolType.constant(node.getValue());
  }

  @Override
  public Type visitStringLiteral(DartStringLiteral node, FlowEnv parameter) {
    return typeHelper.asType(false, node.getType());
  }

  @Override
  public Type visitNullLiteral(DartNullLiteral node, FlowEnv parameter) {
    return NULL_TYPE;
  }

  @Override
  public Type visitArrayLiteral(DartArrayLiteral node, FlowEnv parameter) {
    ArrayList<Type> types = new ArrayList<>();
    for (DartExpression expr : node.getExpressions()) {
      types.add(accept(expr, parameter));
    }
    return ArrayType.constant(types);
  }

  // ---- Access

  /**
   * Returns the correct type of the property, depending of the
   * {@link ElementKind kind}.
   * 
   * @param type
   *          Nullable type of the node.
   * @param kind
   *          Kind of the element to test.
   * @return Type of the property.
   */
  static Type propertyType(Type type, ElementKind kind) {
    switch (kind) {
    case METHOD:
    case CONSTRUCTOR:
      return type.asNonNull();
    default:
      return type;
    }
  }

  @Override
  public Type visitPropertyAccess(final DartPropertyAccess node, FlowEnv parameter) {
    NodeElement nodeElement = node.getElement();
    if (nodeElement != null) {
      return propertyType(typeHelper.asType(true, node.getType()), nodeElement.getKind());
    }
    DartNode qualifier = node.getQualifier();
    Type qualifierType = accept(qualifier, parameter);

    return qualifierType.map(new TypeMapper() {
      @Override
      public Type transform(Type type) {
        if (type instanceof DynamicType) { // you can always qualify dynamic
          return type;
        }
        OwnerType ownerType = (OwnerType) type;
        Element element = ownerType.lookupMember(node.getPropertyName());

        // TypeAnalyzer set some elements.
        // FIXME, don't set the element if we don't needed when generating the
        // bytecode.
        // We need to set the element to compile DartTest/PropertyAcces.dart
        node.setElement(element);

        return propertyType(typeHelper.asType(true, element.getType()), element.getKind());
      }
    });
  }

  @Override
  public Type visitArrayAccess(DartArrayAccess node, FlowEnv parameter) {
    // FIXME Geoffrey, you have to take a look to the parent to know
    // if it represent a[12] or a[12] = ...
    // or perhaps this check should be done in visitBinary for ASSIGN

    Type typeOfArray = accept(node.getTarget(), parameter);
    Type typeOfIndex = accept(node.getKey(), parameter);

    operandIsNonNull(node.getTarget(), parameter);
    // node.getKey -> int32+

    if (!(typeOfIndex instanceof IntType)) {
      return DYNAMIC_NON_NULL_TYPE;
    }
    if (!((IntType) typeOfIndex).isIncludeIn(POSITIVE_INT32_TYPE)) {
      return DYNAMIC_NON_NULL_TYPE;
    }

    if (!(typeOfArray instanceof ArrayType)) {
      if (!(typeOfArray instanceof InterfaceType)) {
        return DYNAMIC_NON_NULL_TYPE;
      }
      InterfaceType interfaceArray = (InterfaceType) typeOfArray;

      Element element = interfaceArray.lookupMember("operator []");
      if (element == null) {
        element = interfaceArray.lookupMember("operator []=");
      }
      if (element == null || !(element instanceof MethodElement)) {
        // the class doesn't provide any operator []
        return DYNAMIC_NON_NULL_TYPE;
      }
      return typeHelper.asType(true, ((MethodElement) element).getReturnType());
    }

    IntType index = (IntType) typeOfIndex;
    ArrayType array = (ArrayType) typeOfArray;

    // is it a constant array ?
    List<Type> constant = array.asConstant();
    if (constant == null) {
      return array.getComponentType();
    }

    int max = index.getMaxBound().intValue();
    BigInteger arrayMaxBound = array.getLength().getMaxBound();
    if (max >= arrayMaxBound.intValue()) {
      // we may access to a index which is bigger than length of the array
      return array.getComponentType();
    }

    int min = index.getMinBound().intValue();
    Type type = constant.get(min);
    for (int i = min + 1; i <= max; i++) {
      type = Types.union(type, constant.get(i));
    }
    return type;
  }
}