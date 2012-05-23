package jdart.compiler.phase;

import static jdart.compiler.type.CoreTypeRepository.*;
import static jdart.compiler.type.CoreTypeRepository.DYNAMIC_NON_NULL_TYPE;
import static jdart.compiler.type.CoreTypeRepository.DYNAMIC_TYPE;
import static jdart.compiler.type.CoreTypeRepository.NULL_TYPE;
import static jdart.compiler.type.CoreTypeRepository.POSITIVE_INT32;
import static jdart.compiler.type.CoreTypeRepository.VOID_TYPE;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import jdart.compiler.type.Types;
import jdart.compiler.visitor.ASTVisitor2;

import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.ast.DartArrayAccess;
import com.google.dart.compiler.ast.DartArrayLiteral;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartBooleanLiteral;
import com.google.dart.compiler.ast.DartClass;
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
import com.google.dart.compiler.ast.Modifiers;
import com.google.dart.compiler.parser.Token;
import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.CoreTypeProvider;
import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.ElementKind;
import com.google.dart.compiler.resolver.FieldElement;
import com.google.dart.compiler.resolver.MethodElement;
import com.google.dart.compiler.resolver.NodeElement;
import com.google.dart.compiler.resolver.VariableElement;

public class FlowTypingPhase implements DartCompilationPhase {
  @Override
  public DartUnit exec(DartUnit unit, DartCompilerContext context, CoreTypeProvider coreTypeProvider) {
    // initialize core type repository
    CoreTypeRepository coreTypeRepository = CoreTypeRepository.initCoreTypeRepository(coreTypeProvider);

    TypeRepository typeRepository = new TypeRepository(coreTypeRepository);
    TypeHelper typeHelper = new TypeHelper(typeRepository);
    new DefinitionVisitor(typeHelper).typeFlow(unit);
    return unit;
  }

  static class DefinitionVisitor extends ASTVisitor2<Type, FlowEnv> {
    private final TypeHelper typeHelper;

    DefinitionVisitor(TypeHelper typeHelper) {
      this.typeHelper = typeHelper;
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

      FlowEnv flowEnv = new FlowEnv(thisType);
      DartFunction function = node.getFunction();
      if (function != null) {
        new FTVisitor(typeHelper).typeFlow(function, flowEnv);
      }
      return null;
    }
  }

  static class FTVisitor extends ASTVisitor2<Type, FlowEnv> {
    private final TypeHelper typeHelper;
    private final HashMap<DartNode, Type> typeMap = new HashMap<>();
    private Type inferredReturnType;

    FTVisitor(TypeHelper typeHelper) {
      this.typeHelper = typeHelper;
    }

    // entry point
    public Type typeFlow(DartFunction node, FlowEnv flowEnv) {
      return visitFunction(node, flowEnv);
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
      // function element is not initialized, we use the parent element here
      Element element = node.getParent().getElement();
      Type returnType = ((FunctionType) typeHelper.asType(false, element.getType())).getReturnType();

      // propagate thisType or null
      FlowEnv env = new FlowEnv(flowEnv, returnType, VOID_TYPE);
      for (DartParameter parameter : node.getParameters()) {
        Type parameterType = accept(parameter, null);
        env.register(parameter.getElement(), parameterType);
      }

      DartBlock body = node.getBody();
      if (body != null) {
        accept(body, env);
      }

      // TODO test display, to remove.
      System.out.println(env);
      return (inferredReturnType != null) ? inferredReturnType : returnType;
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
      if (value != null) {
        // return should return a value compatible with
        // the function declared return type
        accept(value, flowEnv.expectedType(flowEnv.getReturnType()));
      }
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

    /**
     * Change the type of variables used in condition, depending of the
     * condition and it's trueness.
     * 
     * //TODO find a better name...
     * 
     * @param isTrue
     *          Trueness of the condition
     * @param node
     *          Condition to use
     * @param parameter
     *          Used to modify variable types
     */
    private void magicChangeType(boolean isTrue, DartBinaryExpression node, FlowEnv parameter) {
      // TODO Auto-generated method stub
      DartExpression arg1 = node.getArg1();
      DartExpression arg2 = node.getArg2();
      Type type1 = accept(arg1, parameter);
      Type type2 = accept(arg2, parameter);
      Token operator = node.getOperator();

      switch (operator) {
      case EQ:
        // Il faut regarder quel type est inclus dans l'autre pour savoir que type doit prendre la valeur de l'autre type.
        if (isTrue) {
          if (type1 instanceof IntType) {
            IntType iType1 = (IntType) type1;
            IntType iType2 = (IntType) type2;
            if (iType1.isIncludeIn(iType2)) {
              parameter.register((VariableElement) arg2.getElement(), iType1);
            }
          }
        }
      }
    }

    @Override
    public Type visitIfStatement(DartIfStatement node, FlowEnv parameter) {
      // FIXME IfStatement doesn't work well.
      System.out.println("If:");
      boolean isTrue = (boolean) accept(node.getCondition(), parameter).asConstant();

      if (isTrue) {
        FlowEnv envThen = new FlowEnv(parameter, parameter.getReturnType(), parameter.getExpectedType());
        magicChangeType(isTrue, (DartBinaryExpression) node.getCondition(), envThen);
        System.out.println("Then:");
        System.out.println(envThen);
        accept(node.getThenStatement(), envThen);
        parameter.merge(envThen);
      } else if (node.getElseStatement() != null) {
        FlowEnv envElse = new FlowEnv(parameter, parameter.getReturnType(), parameter.getExpectedType());
        magicChangeType(!isTrue, (DartBinaryExpression) node.getCondition(), envElse);
        accept(node.getElseStatement(), envElse);
        parameter.merge(envElse);
      }

      return null;
    }

    @Override
    // FIXME ForStatement doesn't work well.
    public Type visitForStatement(DartForStatement node, FlowEnv parameter) {
      accept(node.getInit(), parameter);
      FlowEnv env = parameter;
      do {
        env = new FlowEnv(env, env.getReturnType(), env.getExpectedType());
        accept(node.getCondition(), env);
        accept(node.getBody(), env);
        accept(node.getIncrement(), env);
      } while (!env.isStable());

      parameter.update(env);

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
      case NE_STRICT:
        // TODO known the difference between != and !==.
        return type1.equals(type2) ? FALSE : TRUE;
      case EQ:
      case EQ_STRICT:
        // TODO known the difference between == and ===.
        return type1.equals(type2) ? TRUE : FALSE;
      case LT:
      case LTE:
      case GT:
      case GTE:
      case AND:
      case OR:
        return BOOL_NON_NULL_TYPE;

      case ADD:
      case SUB:
        operandIsNonNull(arg1, flowEnv);
        if (type1 instanceof IntType && type2 instanceof IntType) {
          operandIsNonNull(arg2, flowEnv);
          IntType itype1 = (IntType) type1;
          IntType itype2 = (IntType) type2;
          switch (operator) {
          case ADD:
            return itype1.add(itype2);
          case SUB:
            return itype1.sub(itype2);
          }
        }
        if (type1 instanceof DoubleType || type2 instanceof DoubleType) {
          operandIsNonNull(arg2, flowEnv);
          DoubleType dtype1, dtype2;
          if (type1 instanceof IntType) {
            dtype1 = ((IntType) type1).asDouble();
            dtype2 = (DoubleType) type2;
          } else if (type2 instanceof IntType) {
            dtype1 = (DoubleType) type1;
            dtype2 = ((IntType) type2).asDouble();
          } else {
            dtype1 = (DoubleType) type1;
            dtype2 = (DoubleType) type2;
          }
          switch (operator) {
          case ADD:
            return dtype1.add(dtype2);
          case SUB:
            return dtype1.sub(dtype2);
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
        System.err.println("NoSuchMethodExcpetion: " + node.getOperator() + " for type: " + type1 + ", " + type2);
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

    // --- Invocation

    @Override
    public Type visitMethodInvocation(final DartMethodInvocation node, FlowEnv flowEnv) {
      ArrayList<Type> argumentTypes = new ArrayList<>();
      for (DartExpression argument : node.getArguments()) {
        argumentTypes.add(accept(argument, flowEnv));
      }

      switch (node.getTarget().getElement().getKind()) {
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
      for (DartExpression argument : node.getArguments()) {
        accept(argument, flowEnv);
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
      case METHOD: // polymorphic method call on 'this'
        return typeHelper.asType(true, ((MethodElement) nodeElement).getReturnType());

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
      //FIXME Geoffrey, you have to take a look to the parent to know
      // if it represent a[12] or a[12] = ...
      // or perhaps this check should be done in visitBinary for ASSIGN
      
      operandIsNonNull(node.getTarget(), parameter);
      
      Type typeOfIndex = accept(node.getKey(), parameter);
      if (!(typeOfIndex instanceof IntType)) {
        return DYNAMIC_NON_NULL_TYPE;
      }
      if (!((IntType) typeOfIndex).isIncludeIn(POSITIVE_INT32)) {
        return DYNAMIC_NON_NULL_TYPE;
      }

      Type typeOfArray = accept(node.getTarget(), parameter);
      if (!(typeOfArray instanceof ArrayType)) {
        if (!(typeOfArray instanceof InterfaceType)) {
          return DYNAMIC_NON_NULL_TYPE;  
        }
        InterfaceType interfaceArray = (InterfaceType) typeOfArray;

        Element element = interfaceArray.lookupMember("operator []");
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
