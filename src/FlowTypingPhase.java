import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import type.BoolType;
import type.CoreTypeRepository;
import type.DoubleType;
import type.FunctionType;
import type.IntType;
import type.Type;
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
import com.google.dart.compiler.ast.DartIdentifier;
import com.google.dart.compiler.ast.DartIntegerLiteral;
import com.google.dart.compiler.ast.DartMethodDefinition;
import com.google.dart.compiler.ast.DartMethodInvocation;
import com.google.dart.compiler.ast.DartNewExpression;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartParameter;
import com.google.dart.compiler.ast.DartReturnStatement;
import com.google.dart.compiler.ast.DartStatement;
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
import com.google.dart.compiler.resolver.FieldElement;
import com.google.dart.compiler.resolver.MethodElement;
import com.google.dart.compiler.resolver.MethodNodeElement;
import com.google.dart.compiler.resolver.NodeElement;
import com.google.dart.compiler.resolver.VariableElement;
import com.google.dart.compiler.type.FunctionAliasType;

import static type.CoreTypeRepository.*;

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
        return CoreTypeRepository.DYNAMIC_TYPE;
      case VARIABLE:
        // return typeRepository.findType(nullable, (ClassElement)
        // type.getElement());
      case INTERFACE:
        return typeRepository.findType(nullable, (ClassElement) type.getElement());
      case FUNCTION:
        System.err.println("FUNCTION:");
        return asFunctionType(nullable, (com.google.dart.compiler.type.FunctionType) type);
      case FUNCTION_ALIAS:
        System.err.println("FUNCTION_ALIAS:");
        return asFunctionType(nullable, ((FunctionAliasType) type).getElement().getFunctionType());
      case NONE:
      default:
        throw new AssertionError("asType: " + type.getKind() + " must be implemented");
      }
    }
    
    private FunctionType asFunctionType(boolean nullable, com.google.dart.compiler.type.FunctionType functionType) {
      return typeRepository.findFunction(nullable,
          asType(true, functionType.getReturnType()),
              asTypeList(functionType.getParameterTypes()),
              asTypeMap(functionType.getNamedParameterTypes()));
    }
    
    private List<Type> asTypeList(List<com.google.dart.compiler.type.Type> types) {
      ArrayList<Type> typeList = new ArrayList<>(types.size());
      for(com.google.dart.compiler.type.Type type: types) {
        typeList.add(asType(true, type));
      }
      return typeList;
    }
    
    private Map<String, Type> asTypeMap(Map<String, com.google.dart.compiler.type.Type> types) {
      LinkedHashMap<String, Type> typeMap = new LinkedHashMap<>(types.size());
      for(Entry<String, com.google.dart.compiler.type.Type> entry: types.entrySet()) {
        typeMap.put(entry.getKey(), asType(false, entry.getValue()));
      }
      return typeMap;
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

    // Don't implement the visitor of DartTypeNode, it should be never visited.
    // The type of the corresponding Element should be used instead
    @Override
    public Type visitTypeNode(DartTypeNode node, FlowEnv unused) {
      throw new AssertionError("this method should never be called"); 
    }
    
    @Override
    public Type visitUnit(DartUnit node, FlowEnv unused) {
      System.out.println("Unit:" + node.getSourceName());
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
      if (!modifiers.isStatic() && !modifiers.isFactory() && element instanceof ClassElement) {
        thisType = typeRepository.findType(false, (ClassElement)element.getEnclosingElement());
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
      Type returnType = ((FunctionType)asType(false, element.getType())).getReturnType();
      
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

      System.out.println(node.getParent() + ", " + env);
      return null;
    }
    
    @Override
    public Type visitParameter(DartParameter node, FlowEnv unused) {
      return asType(true, node.getElement().getType());
    }

    @Override
    public Type visitBlock(DartBlock node, FlowEnv flowEnv) {
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
      if (value == null) {   // variable not initialized
        return NULL_TYPE;
      }
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
        return asType(false, node.getElement().getType());
      default:
        throw new UnsupportedOperationException();
      }
    }
    
    @Override
    public Type visitThisExpression(DartThisExpression node, FlowEnv flowEnv) {
      return flowEnv.getThisType();
    }

    @Override
    public Type visitBinaryExpression(DartBinaryExpression node, FlowEnv parameter) {
      DartExpression arg1 = node.getArg1();
      DartExpression arg2 = node.getArg2();
      Type type1 = accept(arg1, parameter);
      Type type2 = accept(arg2, parameter);
      Token operator = node.getOperator();
      
      // is it an assignment ?
      if (operator.isAssignmentOperator()) {
        switch (operator) {
        case ASSIGN:
          Element element1 = arg1.getElement();
          switch (element1.getKind()) {
          case VARIABLE:
          case PARAMETER:
            parameter.register((VariableElement) element1, type2);
            return type2;
          case FIELD:
            return type2;
          default:
            throw new AssertionError("Binary Expr: " + element1.getKind() + " not implemented");
          }

        default:
        }
        throw new UnsupportedOperationException();
      }

      // it's an operation
      switch (operator) {      
      case SUB:
        //FIXME integer range are not substracted
        return Types.union(type1, type2);

      case EQ_STRICT:
        return BOOL_NON_NULL_TYPE;

      default:
        throw new AssertionError("Binary Expr: " + operator + " not implemented");
      }
    }

    @Override
    public Type visitNewExpression(DartNewExpression node, FlowEnv flowEnv) {
      ArrayList<Type> argumentTypes = new ArrayList<>();
      for(DartExpression argument: node.getArguments()) {
        argumentTypes.add(accept(argument, flowEnv));
      }
      
      ClassElement element = node.getElement().getConstructorType();
      return typeRepository.findType(false, element);
    }

    @Override
    public Type visitMethodInvocation(DartMethodInvocation node, FlowEnv flowEnv) {
      ArrayList<Type> argumentTypes = new ArrayList<>();
      for(DartExpression argument: node.getArguments()) {
        argumentTypes.add(accept(argument, flowEnv));
      }
      
      NodeElement element = node.getElement();
      switch (node.getTarget().getElement().getKind()) {
      case CLASS:   // static field or method
      case SUPER:   // super field or method
      case LIBRARY: // library call
        
        switch(element.getKind()) {
        case FIELD:     // field access
          return Types.getReturnType(asType(true, ((FieldElement)element).getType()));
        
        case METHOD: {  // statically resolved method call
          /* emulate a call FIXME
          FlowEnv newFlowEnv = new FlowEnv(null);
          for(Type argumentType: argumentTypes) {
            newFlowEnv.register(variable, argumentType);
          }
          return new FTVisitor(typeRepository).accept(((MethodNodeElement)element).getNode(), newFlowEnv);
          */
          return asType(true, ((MethodElement)element).getReturnType());
        }
        
        default:
          throw new UnsupportedOperationException();
        }
        
      default:  // polymorphic method call  
        Type receiverType = accept(node.getTarget(), flowEnv);
        
        // FIXME
        // because method call can be dynamic, fallback to the declared return type
        return asType(true, ((MethodElement)element).getReturnType());
      }
    }

    @Override
    public Type visitUnqualifiedInvocation(DartUnqualifiedInvocation node, FlowEnv flowEnv) {
      for(DartExpression argument: node.getArguments()) {
        accept(argument, flowEnv);
      }
      
      // weird, element is set on target ?
      NodeElement element = node.getTarget().getElement();
      switch(element.getKind()) {
      case METHOD: // polymorphic method call on 'this'
        return asType(true, ((MethodElement)element).getReturnType());
        
      case FIELD:  // function call
      case PARAMETER:
      case VARIABLE:
        return Types.getReturnType(asType(true, element.getType()));
      
      default:  //FUNCTION_OBJECT ??
        throw new UnsupportedOperationException();
      }
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
  }
}
