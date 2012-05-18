import static type.CoreTypeRepository.*;
import static type.CoreTypeRepository.NULL_TYPE;
import static type.CoreTypeRepository.VOID_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import type.BoolType;
import type.CoreTypeRepository;
import type.DoubleType;
import type.DynamicType;
import type.FunctionType;
import type.IntType;
import type.OwnerType;
import type.Type;
import type.TypeMapper;
import type.TypeRepository;
import type.Types;
import visitor.ASTVisitor2;

import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartBooleanLiteral;
import com.google.dart.compiler.ast.DartClass;
import com.google.dart.compiler.ast.DartDoubleLiteral;
import com.google.dart.compiler.ast.DartExprStmt;
import com.google.dart.compiler.ast.DartExpression;
import com.google.dart.compiler.ast.DartFieldDefinition;
import com.google.dart.compiler.ast.DartFunction;
import com.google.dart.compiler.ast.DartFunctionObjectInvocation;
import com.google.dart.compiler.ast.DartIdentifier;
import com.google.dart.compiler.ast.DartIntegerLiteral;
import com.google.dart.compiler.ast.DartMethodDefinition;
import com.google.dart.compiler.ast.DartMethodInvocation;
import com.google.dart.compiler.ast.DartNewExpression;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartParameter;
import com.google.dart.compiler.ast.DartPropertyAccess;
import com.google.dart.compiler.ast.DartReturnStatement;
import com.google.dart.compiler.ast.DartStatement;
import com.google.dart.compiler.ast.DartStringLiteral;
import com.google.dart.compiler.ast.DartSuperExpression;
import com.google.dart.compiler.ast.DartThisExpression;
import com.google.dart.compiler.ast.DartThrowStatement;
import com.google.dart.compiler.ast.DartTypeNode;
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
import com.google.dart.compiler.resolver.MethodNodeElement;
import com.google.dart.compiler.resolver.NodeElement;
import com.google.dart.compiler.resolver.VariableElement;
import com.google.dart.compiler.type.FunctionAliasType;

public class FlowTypingPhase implements DartCompilationPhase {
  @Override
  public DartUnit exec(DartUnit unit, DartCompilerContext context, CoreTypeProvider coreTypeProvider) {
    // initialize core type repository
    CoreTypeRepository coreTypeRepository = CoreTypeRepository.initCoreTypeRepository(coreTypeProvider);

    TypeRepository typeRepository = new TypeRepository(coreTypeRepository);
    new FTVisitor(typeRepository).flowTyping(unit);
    return unit;
  }

  private static class FTVisitor extends ASTVisitor2<Type, FlowEnv> {
    private final TypeRepository typeRepository;
    private final HashMap<DartNode, Type> typeMap = new HashMap<>();

    FTVisitor(TypeRepository typeRepository) {
      this.typeRepository = typeRepository;
    }

    // entry point
    public Type flowTyping(DartNode node) {
      return accept(node, null);
    }

    private Type asType(boolean nullable, com.google.dart.compiler.type.Type type) {
      switch (type.getKind()) {
      case VOID:
        return CoreTypeRepository.VOID_TYPE;
      case DYNAMIC:
        return nullable ? CoreTypeRepository.DYNAMIC_TYPE : CoreTypeRepository.DYNAMIC_NON_NULL_TYPE;
      case VARIABLE:
        // return typeRepository.findType(nullable, (ClassElement)
        // type.getElement());
      case INTERFACE:
        return typeRepository.findType(nullable, (ClassElement) type.getElement());
      case FUNCTION:
        return asFunctionType(nullable, (com.google.dart.compiler.type.FunctionType) type);
      case FUNCTION_ALIAS:
        return asFunctionType(nullable, ((FunctionAliasType) type).getElement().getFunctionType());
      case NONE:
      default:
        throw new AssertionError("asType: " + type.getKind() + " must be implemented");
      }
    }

    private FunctionType asFunctionType(boolean nullable, com.google.dart.compiler.type.FunctionType functionType) {
      return typeRepository.findFunction(nullable, asType(true, functionType.getReturnType()), asTypeList(functionType.getParameterTypes()),
          asTypeMap(functionType.getNamedParameterTypes()), null);
    }
    
    private FunctionType asConstantFunctionType(MethodElement element) {
      com.google.dart.compiler.type.FunctionType functionType = element.getFunctionType();
      return typeRepository.findFunction(false, asType(true, functionType.getReturnType()), asTypeList(functionType.getParameterTypes()),
          asTypeMap(functionType.getNamedParameterTypes()), element);
    }

    private List<Type> asTypeList(List<com.google.dart.compiler.type.Type> types) {
      ArrayList<Type> typeList = new ArrayList<>(types.size());
      for (com.google.dart.compiler.type.Type type : types) {
        typeList.add(asType(true, type));
      }
      return typeList;
    }

    private Map<String, Type> asTypeMap(Map<String, com.google.dart.compiler.type.Type> types) {
      LinkedHashMap<String, Type> typeMap = new LinkedHashMap<>(types.size());
      for (Entry<String, com.google.dart.compiler.type.Type> entry : types.entrySet()) {
        typeMap.put(entry.getKey(), asType(false, entry.getValue()));
      }
      return typeMap;
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
    public Type visitUnit(DartUnit node, FlowEnv unused) {
      // TODO
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
      MethodNodeElement element = node.getElement();
      if (!modifiers.isStatic() && !modifiers.isFactory()) {
        if (element instanceof ClassElement) {
          thisType = typeRepository.findType(false, (ClassElement) element.getEnclosingElement());
        } else {
          thisType = asType(false, node.getElement().getEnclosingElement().getType());
        }
      }

      FlowEnv flowEnv = new FlowEnv(thisType);
      DartFunction function = node.getFunction();
      if (function != null) {
        accept(function, flowEnv);
      }
      return null;
    }

    @Override
    public Type visitFunction(DartFunction node, FlowEnv flowEnv) {
      // function element is not initialized, we use the parent element here
      Element element = node.getParent().getElement();
      Type returnType = ((FunctionType) asType(false, element.getType())).getReturnType();

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
      return null;
    }

    @Override
    public Type visitParameter(DartParameter node, FlowEnv unused) {
      // use the declared type of the parameter
      return asType(true, node.getElement().getType());
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
      // TODO
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
        return NULL_TYPE;
      }
      // the type is the type of the initialization expression
      VariableElement element = node.getElement();
      Type declaredType = asType(true, element.getType());
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

    // @Override
    // public Type visitIfStatement(DartIfStatement node, FlowEnv parameter) {
    // accept(node.getThenStatement(), parameter);
    // accept(node.getElseStatement(), parameter);
    // return null;
    // }

    // --- expressions

    @Override
    public Type visitIdentifier(DartIdentifier node, FlowEnv flowEnv) {
      switch (node.getElement().getKind()) {
      case VARIABLE:
      case PARAMETER:
        return flowEnv.getType((VariableElement) node.getElement());
      case FIELD:
        return asType(true, node.getElement().getType());
      case METHOD:
        // reference a method by name
        return asType(false, node.getElement().getType());
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
      case EQ_STRICT:
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
        throw new AssertionError("Binary Expr: " + operator + " not implemented");
      }

      // a method call that can be polymorphic
      return asType(true, node.getElement().getFunctionType().getReturnType());
    }

    @Override
    public Type visitNewExpression(DartNewExpression node, FlowEnv flowEnv) {
      ArrayList<Type> argumentTypes = new ArrayList<>();
      for (DartExpression argument : node.getArguments()) {
        argumentTypes.add(accept(argument, flowEnv));
      }

      ClassElement element = node.getElement().getConstructorType();
      return typeRepository.findType(false, element);
    }

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
          return Types.getReturnType(asType(true, ((FieldElement) nodeElement).getType()));

        case METHOD: { // statically resolved method call
          /*
           * emulate a call FIXME FlowEnv newFlowEnv = new FlowEnv(null);
           * for(Type argumentType: argumentTypes) {
           * newFlowEnv.register(variable, argumentType); } return new
           * FTVisitor(
           * typeRepository).accept(((MethodNodeElement)element).getNode(),
           * newFlowEnv);
           */
          return asType(true, ((MethodElement) nodeElement).getReturnType());
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

      // TODO Geoffrey, remove this test when you agree with me,
      // node element can be null here but we don't care
      if (node.getElement() == null) {
        System.out.println("Method Invocation: Element null: " + node);
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
          return asType(true, ((MethodElement) element).getReturnType());
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
        return asType(true, ((MethodElement) nodeElement).getReturnType());

      case FIELD: // function call
      case PARAMETER:
        return Types.getReturnType(asType(true, nodeElement.getType()));
      case VARIABLE:
        return Types.getReturnType(flowEnv.getType((VariableElement) nodeElement));

      default: // FUNCTION_OBJECT ??
        throw new UnsupportedOperationException();
      }
    }

    @Override
    public Type visitFunctionObjectInvocation(DartFunctionObjectInvocation node, FlowEnv parameter) {
      // FIXME Geoffrey, a function object is when you have a function stored in
      // a parameter/variable or a result of another call that you call as a function
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
      return asType(false, node.getType());
    }

    // ----

    /**
     * Returns the correct type of the property, depending of the
     * {@link ElementKind kind} of element.
     * 
     * @param type
     *          Nullable type of the node.
     * @param element
     *          Element to test.
     * @return Type of the property.
     */
    private Type propertyType(Type type, Element element) {
      switch (element.getKind()) {
      case METHOD:
      case CONSTRUCTOR:
        type = type.asNonNull();
      default:
        return type;
      }
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
    public Type visitPropertyAccess(final DartPropertyAccess node, FlowEnv parameter) {
      NodeElement nodeElement = node.getElement();
      if (nodeElement != null) {
        // FIXME Geoffrey, doesn't work if the type of the field is a function
        // type
        // it should be nullable
        // you have to do a switch on the element.kind

        return propertyType(asType(true, node.getType()), nodeElement);
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
          // FIXME, don't set the element if we don't needed when generating the bytecode.
          // We need to set the element to compile DartTest/PropertyAcces.dart
          node.setElement(element);

          // FIXME Geoffrey, again here, you can access to a field which is
          // typed as a function type, in that case it should be nullable
          return propertyType(asType(true, element.getType()), element);
        }
      });
    }
  }
}
