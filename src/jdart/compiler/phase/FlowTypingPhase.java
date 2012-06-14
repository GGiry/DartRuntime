package jdart.compiler.phase;

import static jdart.compiler.type.CoreTypeRepository.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jdart.compiler.type.ArrayType;
import jdart.compiler.type.BoolType;
import jdart.compiler.type.CoreTypeRepository;
import jdart.compiler.type.DoubleType;
import jdart.compiler.type.DynamicType;
import jdart.compiler.type.FunctionType;
import jdart.compiler.type.IntType;
import jdart.compiler.type.InterfaceType;
import jdart.compiler.type.OwnerType;
import jdart.compiler.type.Type;
import jdart.compiler.type.TypeMapper;
import jdart.compiler.type.TypeRepository;
import jdart.compiler.type.TypeVisitor;
import jdart.compiler.type.Types;
import jdart.compiler.type.UnionType;
import jdart.compiler.visitor.ASTVisitor2;

import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.ast.DartArrayAccess;
import com.google.dart.compiler.ast.DartArrayLiteral;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartBooleanLiteral;
import com.google.dart.compiler.ast.DartClass;
import com.google.dart.compiler.ast.DartDoWhileStatement;
import com.google.dart.compiler.ast.DartDoubleLiteral;
import com.google.dart.compiler.ast.DartEmptyStatement;
import com.google.dart.compiler.ast.DartExprStmt;
import com.google.dart.compiler.ast.DartExpression;
import com.google.dart.compiler.ast.DartFieldDefinition;
import com.google.dart.compiler.ast.DartForStatement;
import com.google.dart.compiler.ast.DartFunction;
import com.google.dart.compiler.ast.DartFunctionObjectInvocation;
import com.google.dart.compiler.ast.DartIdentifier;
import com.google.dart.compiler.ast.DartIfStatement;
import com.google.dart.compiler.ast.DartIntegerLiteral;
import com.google.dart.compiler.ast.DartMethodDefinition;
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
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.ast.DartUnqualifiedInvocation;
import com.google.dart.compiler.ast.DartVariable;
import com.google.dart.compiler.ast.DartVariableStatement;
import com.google.dart.compiler.ast.DartWhileStatement;
import com.google.dart.compiler.ast.Modifiers;
import com.google.dart.compiler.parser.Token;
import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.CoreTypeProvider;
import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.ElementKind;
import com.google.dart.compiler.resolver.EnclosingElement;
import com.google.dart.compiler.resolver.FieldElement;
import com.google.dart.compiler.resolver.MethodElement;
import com.google.dart.compiler.resolver.MethodNodeElement;
import com.google.dart.compiler.resolver.NodeElement;
import com.google.dart.compiler.resolver.VariableElement;

public class FlowTypingPhase implements DartCompilationPhase {
  @Override
  public DartUnit exec(DartUnit unit, DartCompilerContext context, CoreTypeProvider coreTypeProvider) {
    // initialize core type repository
    CoreTypeRepository coreTypeRepository = CoreTypeRepository.initCoreTypeRepository(coreTypeProvider);

    TypeRepository typeRepository = new TypeRepository(coreTypeRepository);
    TypeHelper typeHelper = new TypeHelper(typeRepository);
    IntraProcedualMethodCallResolver methodCallResolver = new IntraProcedualMethodCallResolver(typeHelper);
    new DefinitionVisitor(typeHelper, methodCallResolver).typeFlow(unit);
    return unit;
  }

  static class IntraProcedualMethodCallResolver implements MethodCallResolver {
    final TypeHelper typeHelper;
    
    IntraProcedualMethodCallResolver(TypeHelper typeHelper) {
      this.typeHelper = typeHelper;
    }
    
    @Override
    public Type methodCall(final String methodName, Type receiverType, List<Type> argumentType, final Type expectedType, boolean virtual) {
      Type returnType = receiverType.accept(new TypeVisitor<Type, Void>() {
          @Override
          protected Type visitOwnerType(OwnerType type, Void parameter) {
            Element member = type.lookupMember(methodName);
            if (!(member instanceof MethodElement)) {
              throw new AssertionError();
            }
            MethodElement methodElement = (MethodElement) member;
            return typeHelper.asType(true, methodElement.getReturnType());
          }
          @Override
          public Type visitDynamicType(DynamicType type, Void parameter) {
            return expectedType;
          }
        }, null);
      return (returnType instanceof DynamicType)? expectedType: returnType;
    }
    
    @Override
    public Type functionCall(MethodElement nodeElement, List<Type> argumentTypes, Type expectedType) {
      return typeHelper.asType(true, nodeElement.getReturnType());
    }
  }
  
  static class DefinitionVisitor extends ASTVisitor2<Type, FlowEnv> {
    private final TypeHelper typeHelper;
    private final MethodCallResolver methodCallResolver;

    DefinitionVisitor(TypeHelper typeHelper, MethodCallResolver methodCallResolver) {
      this.typeHelper = typeHelper;
      this.methodCallResolver = methodCallResolver;
    }

    // entry point
    public void typeFlow(DartUnit unit) {
      accept(unit, null);
    }

    @Override
    public Type visitUnit(DartUnit node, FlowEnv unused) {
      // TODO Temporary display.
      System.out.println("Unit: " + node.getSourceName());
      for (DartNode child : node.getTopLevelNodes()) {
        accept(child, null);
      }
      return null;
    }

    @Override
    public Type visitClass(DartClass node, FlowEnv unused) {
      for (DartNode member : node.getMembers()) {
        if (member != null) {
          accept(member, null);
        }
      }
      return null;
    }

    @Override
    public Type visitFieldDefinition(DartFieldDefinition node, FlowEnv unused) {
      // do nothing, at least for now,
      // field as already been resolved by Dart compiler resolver
      return null;
    }

    @Override
    public Type visitMethodDefinition(DartMethodDefinition node, FlowEnv unused) {
      DartFunction function2 = node.getFunction();
      DartFunction function = function2;
      if (function == null) {
        // native function use declared return type
        return typeHelper.asType(true, node.getType());
      }

      // We should allow to propagate the type of 'this' in the flow env
      // to be more precise, but currently we don't specialize method call,
      // but only function call

      Type thisType = null;
      Modifiers modifiers = node.getModifiers();
      MethodElement element = node.getElement();
      if (!modifiers.isStatic() && !modifiers.isFactory()) {
        if (element.getEnclosingElement() instanceof ClassElement) {
          thisType = typeHelper.findType(false, (ClassElement) element.getEnclosingElement());
        } else {
          thisType = DYNAMIC_TYPE;
        }
      }

      // extract return type info from function type
      Type returnType = ((FunctionType) typeHelper.asType(false, element.getType())).getReturnType();

      FTVisitor flowTypeVisitor = new FTVisitor(typeHelper, methodCallResolver);
      FlowEnv flowEnv = new FlowEnv(new FlowEnv(thisType), returnType, VOID_TYPE, false);
      for (DartParameter parameter : function.getParameters()) {
        Type parameterType = flowTypeVisitor.typeFlow(parameter, null);
        flowEnv.register(parameter.getElement(), parameterType);
      }

      DartBlock body = function.getBody();
      if (body != null) {
        flowTypeVisitor.typeFlow(body, flowEnv);
      }

      // TODO test display, to remove.
      System.out.println(flowEnv);
      
      return null;
    }
  }

  public static class FTVisitor extends ASTVisitor2<Type, FlowEnv> {
    private final TypeHelper typeHelper;
    private final HashMap<DartNode, Type> typeMap = new HashMap<>();
    private final MethodCallResolver methodCallResolver;
    private Type inferredReturnType;

    public FTVisitor(TypeHelper typeHelper, MethodCallResolver methodCallResolver) {
      this.typeHelper = typeHelper;
      this.methodCallResolver = methodCallResolver;
    }

    /**
     * Collect inferred return type
     * @param type an inferred return type.
     */
    private void addInferredReturnType(Type type) {
      if (inferredReturnType == null) {
        inferredReturnType = type;
        return;
      }
      inferredReturnType = Types.union(inferredReturnType, type);
    }
    
    public Type getInferredReturnType(Type declaredReturnType) {
      return (inferredReturnType == null)? declaredReturnType: inferredReturnType;
    }
    
    // entry point
    public Type typeFlow(DartNode node, FlowEnv flowEnv) {
      return accept(node, flowEnv);
    }

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

    @Override
    public Type visitBlock(DartBlock node, FlowEnv flowEnv) {
      // each instruction should be compatible with void
      for (DartStatement statement : node.getStatements()) {
        accept(statement, flowEnv.expectedType(VOID_TYPE));
      }
      return null;
    }

    // --- statements

    @Override
    public Type visitReturnStatement(DartReturnStatement node, FlowEnv flowEnv) {
      DartExpression value = node.getValue();
      Type type;
      if (value != null) {
        // return should return a value compatible with
        // the function declared return type
        type = accept(value, flowEnv.expectedType(flowEnv.getReturnType()));
      } else {
        type = VOID_TYPE;
      }
      addInferredReturnType(type);
      return null;
    }

    @Override
    public Type visitThrowStatement(DartThrowStatement node, FlowEnv flowEnv) {
      if (node.getException() == null) {
        // TODO correctly handle the error?
        System.err.println("Throw statement: null exception");
        throw null;
      }

      accept(node.getException(), flowEnv);
      return null;
    }

    @Override
    public Type visitVariableStatement(DartVariableStatement node, FlowEnv flowEnv) {
      for (DartVariable variable : node.getVariables()) {
        accept(variable, flowEnv);
      }
      return null;
    }

    @Override
    public Type visitVariable(DartVariable node, FlowEnv flowEnv) {
      DartExpression value = node.getValue();
      if (value == null) {
        // variable is not initialized, in Dart variables are initialized
        // with null by default
        flowEnv.register(node.getElement(), NULL_TYPE);
        return NULL_TYPE;
      }
      // the type is the type of the initialization expression
      VariableElement element = node.getElement();
      Type declaredType = typeHelper.asType(true, element.getType());
      Type type = accept(value, flowEnv.expectedType(declaredType));
      flowEnv.register(element, type);
      return null;
    }

    @Override
    public Type visitExprStmt(DartExprStmt node, FlowEnv flowEnv) {
      DartExpression expression = node.getExpression();
      if (expression != null) {
        // statement expression expression should return void
        return accept(expression, flowEnv.expectedType(VOID_TYPE));
      }
      return null;
    }
    
    static class ConditionType {
      private final Type trueType;
      private final Type falseType;
      
      public ConditionType(Type trueType, Type falseType) {
        super();
        this.trueType = trueType;
        this.falseType = falseType;
      }

      /**
       * @return the trueType
       */
      public Type getTrueType() {
        return trueType;
      }

      /**
       * @return the falseType
       */
      public Type getFalseType() {
        return falseType;
      }
    }

    static class ConditionVisitor extends ASTVisitor2<ConditionType, FlowEnv> {
      private final FTVisitor visitor;
      final static int TRUE_POSITION = 0;
      final static int FALSE_POSITION = 1;

      public ConditionVisitor(FTVisitor visitor) {
        this.visitor = visitor;
      }

      @Override
      protected ConditionType accept(DartNode node, FlowEnv parameter) {
        return super.accept(node, parameter);
      }

      @Override
      public ConditionType visitBinaryExpression(DartBinaryExpression node, FlowEnv parameter) {
        DartExpression arg1 = node.getArg1();
        DartExpression arg2 = node.getArg2();
        Type type1 = visitor.accept(arg1, parameter);
        Type type2 = visitor.accept(arg2, parameter);
        Token operator = node.getOperator();
        Type typeTrue = null;
        Type typeFalse = null;
        switch (operator) {
        case EQ_STRICT:
        case EQ:
          typeTrue = type1.commonValuesWith(type2);
          typeFalse = type1.exclude(type2);
          break;
        case NE_STRICT:
        case NE:
          typeTrue = type1.exclude(type2);
          typeFalse = type1.commonValuesWith(type2);
          break;
        case LTE:
          typeTrue = type1.lessThanOrEqualsValues(type2, parameter.inLoop());
          typeFalse = type1.greaterThanValues(type2, parameter.inLoop());
          break;
        case GTE:
          typeTrue = type1.greaterThanOrEqualsValues(type2, parameter.inLoop());
          typeFalse = type1.lessThanValues(type2, parameter.inLoop());
          break;
        case LT:
          typeTrue = type1.lessThanValues(type2, parameter.inLoop());
          typeFalse = type1.greaterThanOrEqualsValues(type2, parameter.inLoop());
          break;
        case GT:
          typeTrue = type1.greaterThanValues(type2, parameter.inLoop());
          typeFalse = type1.lessThanOrEqualsValues(type2, parameter.inLoop());
          break;

        case AND: {
          ConditionType cType1 = accept(arg1, parameter);
          ConditionType cType2 = accept(arg2, parameter);

          typeTrue = cType1.getTrueType().commonValuesWith(cType2.getTrueType());
          typeFalse = cType1.getFalseType().commonValuesWith(cType2.getFalseType());
          break;
        }

        default:
          throw new IllegalStateException("You have to implement ConditionVisitor.visitBinaryExpression() for " + operator + " (" + operator.name() + ")");
        }
        return new ConditionType(typeTrue, typeFalse);
      }

      @Override
      public ConditionType visitBooleanLiteral(DartBooleanLiteral node, FlowEnv parameter) {
        return null;
      }
    }

    private void changeOperandsTypes(Type type, DartBinaryExpression node, FlowEnv parameter) {
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
    public Type visitIfStatement(DartIfStatement node, FlowEnv parameter) {
      DartExpression condition = node.getCondition();
      Type conditionType = accept(condition, parameter);

      ConditionVisitor conditionVisitor = new ConditionVisitor(this);
      ConditionType types = conditionVisitor.accept(condition, parameter);
      
      FlowEnv envThen = new FlowEnv(parameter, parameter.getReturnType(), parameter.getExpectedType(), parameter.inLoop());
      FlowEnv envElse = new FlowEnv(parameter, parameter.getReturnType(), parameter.getExpectedType(), parameter.inLoop());

      if (conditionType != FALSE_TYPE) {
        if (types != null) {
          changeOperandsTypes(types.getTrueType(), (DartBinaryExpression) condition, envThen);
        }
        accept(node.getThenStatement(), envThen);
        parameter.merge(envThen);
      }
      if (conditionType != TRUE_TYPE && node.getElseStatement() != null) {
        if (types != null) {
          changeOperandsTypes(types.getFalseType(), (DartBinaryExpression) condition, envElse);
        }
        accept(node.getElseStatement(), envElse);
        parameter.merge(envElse);
      }
      return null;
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

    private void computeLoop(DartExpression condition, DartStatement body, DartStatement /* maybe null */ init, DartExpression /* maybe null */ increment, FlowEnv parameter) {
      FlowEnv env = new FlowEnv(parameter, parameter.getReturnType(), parameter.getExpectedType(), true);
      if (init != null) {
        accept(init, env);
      }

      accept(condition, env);
      ConditionVisitor conditionVisitor = new ConditionVisitor(this);

      LoopVisitor loopVisitor = new LoopVisitor();
      Set<VariableElement> list = loopVisitor.accept(body, parameter);
      System.out.println(list);

      do {
        env = new FlowEnv(env, env.getReturnType(), env.getExpectedType(), true);
        ConditionType types = conditionVisitor.accept(condition, env);
        changeOperandsTypes(types.getTrueType(), (DartBinaryExpression) condition, env);

        accept(body, env);
        if (increment != null) {
          accept(increment, env);
        }
        accept(condition, env);
      } while(!env.isStable());

      parameter.update(env);
    }

    @Override
    public Type visitForStatement(DartForStatement node, FlowEnv parameter) {
      computeLoop(node.getCondition(), node.getBody(), node.getInit(), node.getIncrement(), parameter);
      return null;
    }

    @Override
    public Type visitWhileStatement(DartWhileStatement node, FlowEnv parameter) {
      computeLoop(node.getCondition(), node.getBody(), null, null, parameter);
      return null;
    }

    @Override
    public Type visitDoWhileStatement(DartDoWhileStatement node, FlowEnv parameter) {
      computeLoop(node.getCondition(), node.getBody(), null, null, parameter);
      return null;
    }

    @Override
    public Type visitEmptyStatement(DartEmptyStatement node, FlowEnv parameter) {
      return null;
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

    private Type visitBinaryOp(DartBinaryExpression node, Token operator, DartExpression arg1, Type type1, DartExpression arg2, Type type2, FlowEnv flowEnv) {
      switch (operator) {
      // TODO finish binary op
      case NE:
      case NE_STRICT: {
        Object constant1 = type1.asConstant();
        Object constant2 = type2.asConstant();
        if (constant1 != null && constant2 != null) {
          return constant1.equals(constant2) ? FALSE_TYPE : TRUE_TYPE;
        }

        if (type1 instanceof IntType && type2 instanceof IntType) {
          IntType iType1 = (IntType) type1;
          IntType iType2 = (IntType) type2;
          return iType1.hasCommonValuesWith(iType2) ? BOOL_NON_NULL_TYPE : TRUE_TYPE;
        }

        if (type1 instanceof UnionType) {
          return ((UnionType) type1).commonValuesWith(type2);
        }

        if (type2 instanceof UnionType) {
          return ((UnionType) type2).commonValuesWith(type1);
        }

        throw new AssertionError("BinaryOp not implemented for: " + type1 + " " + operator + " " + type2);
      }
      case EQ:
      case EQ_STRICT: {
        Object constant1 = type1.asConstant();
        Object constant2 = type2.asConstant();
        if (constant1 != null && constant2 != null) {
          return type1.equals(type2) ? TRUE_TYPE : FALSE_TYPE;
        }

        if (type1 instanceof IntType && type2 instanceof IntType) {
          IntType iType1 = (IntType) type1;
          IntType iType2 = (IntType) type2;
          return iType1.hasCommonValuesWith(iType2) ? BOOL_NON_NULL_TYPE : FALSE_TYPE;
        }
        throw new AssertionError("BinaryOp not implemented for: " + type1 + " " + operator + " " + type2);
      }
      case LT: {
        if (type1 instanceof IntType && type2 instanceof IntType) {
          IntType iType1 = (IntType) type1;
          IntType iType2 = (IntType) type2;

          return iType1.isStrictLT(iType2) ? TRUE_TYPE : BOOL_NON_NULL_TYPE;
        }
        throw new AssertionError("BinaryOp not implemented for: " + type1 + " " + operator + " " + type2);
      }
      case LTE: {
        if (type1 instanceof IntType && type2 instanceof IntType) {
          IntType iType1 = (IntType) type1;
          IntType iType2 = (IntType) type2;

          return iType1.isStrictLTE(iType2) ? TRUE_TYPE : BOOL_NON_NULL_TYPE;
        }
        throw new AssertionError("BinaryOp not implemented for: " + type1 + " " + operator + " " + type2);
      }
      case GT: {
        if (type1 instanceof IntType && type2 instanceof IntType) {
          IntType iType1 = (IntType) type1;
          IntType iType2 = (IntType) type2;

          return iType2.isStrictLT(iType1) ? TRUE_TYPE : BOOL_NON_NULL_TYPE;
        }
        throw new AssertionError("BinaryOp not implemented for: " + type1 + " " + operator + " " + type2);
      }
      case GTE: {
        if (type1 instanceof IntType && type2 instanceof IntType) {
          IntType iType1 = (IntType) type1;
          IntType iType2 = (IntType) type2;

          return iType2.isStrictLTE(iType1) ? TRUE_TYPE : BOOL_NON_NULL_TYPE;
        }
        throw new AssertionError("BinaryOp not implemented for: " + type1 + " " + operator + " " + type2);
      }
      case AND:
        if (type1 == TRUE_TYPE && type2 == TRUE_TYPE) {
          return TRUE_TYPE;
        }
        return BOOL_TYPE;
      case OR:
        if (type1 == TRUE_TYPE || type2 == TRUE_TYPE) {
          return TRUE_TYPE;
        } 
        return BOOL_TYPE;

      case ADD:
      case SUB:
      case MOD:
      case BIT_AND:
      case BIT_OR:
        operandIsNonNull(arg1, flowEnv);
        if (type1 instanceof IntType && type2 instanceof IntType) {
          operandIsNonNull(arg2, flowEnv);
          IntType iType1 = (IntType) type1;
          IntType iType2 = (IntType) type2;

          switch (operator) {
          case ADD:
            return iType1.add(iType2);
          case SUB:
            return iType1.sub(iType2);
          case MOD:
            return iType1.mod(iType2);
          case BIT_AND:
            return iType1.bitAnd(iType2);
          case BIT_OR:
            return iType1.bitOr(iType2);
          }
        }
        if (type1 instanceof UnionType) {
          UnionType utype = (UnionType) type1;
          switch (operator) {
          case ADD:
            return utype.add(type2);
          case SUB:
            return utype.sub(type2);
          case MOD:
            return utype.mod(type2);
          }
        }
        if (type2 instanceof UnionType) {
          UnionType utype = (UnionType) type2;
          switch (operator) {
          case ADD:
            return utype.add(type1);
          case SUB:
            return utype.sub(type1);
          case MOD:
            return utype.mod(type1);
          }
        }
        if (type1 instanceof DoubleType || type2 instanceof DoubleType) {
          operandIsNonNull(arg2, flowEnv);
          DoubleType dtype1, dtype2;
          if (type1 instanceof IntType) {
            IntType iType1 = (IntType) type1;
            dtype1 = iType1.asDouble();
            dtype2 = (DoubleType) type2;
          } else if (type2 instanceof IntType) {
            IntType iType2 = (IntType) type2;
            dtype1 = (DoubleType) type1;
            dtype2 = iType2.asDouble();
          } else {
            dtype1 = (DoubleType) type1;
            dtype2 = (DoubleType) type2;
          }
          switch (operator) {
          case ADD:
            return dtype1.add(dtype2);
          case SUB:
            return dtype1.sub(dtype2);
          case MOD:
            return dtype1.mod(dtype2);
          }
        }

        // it's not a primitive operation, so it's a method call
        break;
      default:
        throw new AssertionError("Binary Expr: " + operator + " (" + operator.name() + ") not implemented");
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

    private Type visitUnaryOperation(DartUnaryExpression node, Token operator, DartExpression arg, Type type, FlowEnv flowEnv) {
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
      case SUB:
        if (type instanceof IntType) {
          IntType iType = (IntType) type;
          return iType.unarySub();
        }
        if (type instanceof DoubleType) {
          DoubleType dType = (DoubleType) type;
          return dType.unarySub();
        }
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
          Type receiverType = typeHelper.findType(false, (ClassElement)enclosingElement);
          return methodCallResolver.methodCall(node.getObjectIdentifier(), receiverType, argumentTypes, flowEnv.getExpectedType(), true);
        }
        // FIXME should use another method of the methodResolver (but it doesn't exist now)
        return methodCallResolver.functionCall((MethodNodeElement)nodeElement, argumentTypes, flowEnv.getExpectedType());
        //return typeHelper.asType(true, ((MethodElement) nodeElement).getReturnType());
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
    private Type propertyType(Type type, ElementKind kind) {
      switch (kind) {
      case METHOD:
      case CONSTRUCTOR:
        type = type.asNonNull();
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
}
