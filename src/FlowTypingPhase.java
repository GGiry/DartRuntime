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
import com.google.dart.compiler.ast.DartField;
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
import com.google.dart.compiler.ast.DartThrowStatement;
import com.google.dart.compiler.ast.DartTypeNode;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.ast.DartUnqualifiedInvocation;
import com.google.dart.compiler.ast.DartVariable;
import com.google.dart.compiler.ast.DartVariableStatement;
import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.CoreTypeProvider;
import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.VariableElement;
import com.google.dart.compiler.type.FunctionAliasType;

public class FlowTypingPhase implements DartCompilationPhase {
  @Override
  public DartUnit exec(DartUnit unit, DartCompilerContext context, CoreTypeProvider coreTypeProvider) {
    // initialize core type repository
    CoreTypeRepository coreTypeRepository = CoreTypeRepository.initCoreTypeRepository(coreTypeProvider);

    FTVisitor.flowTyping(unit, coreTypeRepository);
    return unit;
  }

  private static class FTVisitor extends ASTVisitor2<Type, FlowEnv> {
    private final TypeRepository typeRepository;
    private final HashMap<DartNode, Type> typeMap = new HashMap<>();

    FTVisitor(CoreTypeRepository coreTypeRepository) {
      this.typeRepository = new TypeRepository(coreTypeRepository);
    }

    FTVisitor(TypeRepository typeRepository) {
      this.typeRepository = typeRepository;
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
          asType(false, functionType.getReturnType()),
              asTypeList(functionType.getParameterTypes()),
              asTypeMap(functionType.getNamedParameterTypes()));
    }
    
    private List<Type> asTypeList(List<com.google.dart.compiler.type.Type> types) {
      ArrayList<Type> typeList = new ArrayList<>(types.size());
      for(com.google.dart.compiler.type.Type type: types) {
        typeList.add(asType(false, type));
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

    public static Type flowTyping(DartNode node, CoreTypeRepository coreTypeRepository) {
      return node.accept(new FTVisitor(coreTypeRepository).asASTVisitor());
    }

    public static Type flowTyping(DartNode node, TypeRepository typeRepository) {
      return node.accept(new FTVisitor(typeRepository).asASTVisitor());
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

    @Override
    public Type visitClass(DartClass node, FlowEnv parameter) {
      FlowEnv env = new FlowEnv(parameter);

      for (DartNode member : node.getMembers()) {
        if (member != null) {
          accept(member, env);
        }
      }
      return null;
    }

    @Override
    public Type visitFieldDefinition(DartFieldDefinition node, FlowEnv parameter) {
      DartTypeNode typeNode = node.getTypeNode();

      if (typeNode != null) {
        return accept(typeNode, parameter);
      }

      for (DartField field : node.getFields()) {
        accept(field, parameter);
      }

      return null;
    }

    @Override
    public Type visitField(DartField node, FlowEnv parameter) {
      return asType(node.getValue() == null, node.getType());
    }

    @Override
    public Type visitMethodDefinition(DartMethodDefinition node, FlowEnv parameter) {
      DartFunction function = node.getFunction();
      if (function != null) {
        accept(function, parameter);
      }
      return null;
    }

    @Override
    public Type visitReturnStatement(DartReturnStatement node, FlowEnv parameter) {
      DartExpression value = node.getValue();
      if (value != null) {
        accept(value, parameter);
      }
      return null;
    }

    @Override
    public Type visitNewExpression(DartNewExpression node, FlowEnv parameter) {
      DartNode constructor = node.getConstructor();
      if (constructor != null) {
        accept(constructor, parameter);
      }
      return null;
    }

    @Override
    public Type visitMethodInvocation(DartMethodInvocation node, FlowEnv parameter) {
      flowTyping(node.getTarget(), typeRepository);
      return accept(node.getTarget(), parameter);
      //return asType(false, node.getType());
    }

    @Override
    public Type visitUnqualifiedInvocation(DartUnqualifiedInvocation node, FlowEnv parameter) {
      flowTyping(node.getTarget().getElement().getNode(), typeRepository);
      return accept(node.getTarget(), parameter);
      //return asType(false, node.getTarget().getType());
    }

    @Override
    public Type visitThrowStatement(DartThrowStatement node, FlowEnv parameter) {
      // TODO
      return null;
    }

    @Override
    public Type visitFunction(DartFunction node, FlowEnv parameter) {
      FlowEnv env = new FlowEnv(parameter);

      for (DartParameter param : node.getParameters()) {
        DartTypeNode typeNode = param.getTypeNode();
        if (typeNode != null) {
          env.register(param.getElement(), accept(typeNode, env));
        }
      }

      DartBlock body = node.getBody();
      if (body != null) {
        accept(body, env);
      }

      System.out.println(node.getParent() + ", " + env);

      return null;
    }

    @Override
    public Type visitBlock(DartBlock node, FlowEnv parameter) {
      for (DartStatement statement : node.getStatements()) {
        accept(statement, parameter);
      }
      return null;
    }

    @Override
    public Type visitVariableStatement(DartVariableStatement node, FlowEnv parameter) {
      for (DartVariable variable : node.getVariables()) {
        accept(variable, parameter);
      }
      return null;
    }

    @Override
    public Type visitVariable(DartVariable node, FlowEnv parameter) {
      DartExpression value = node.getValue();
      if (value == null) {
        return null;
      }
      Type type = accept(value, parameter);
      parameter.register(node.getElement(), type);
      return type;
    }

    @Override
    public Type visitIntegerLiteral(DartIntegerLiteral node, FlowEnv parameter) {
      return IntType.constant(node.getValue());
    }

    @Override
    public Type visitDoubleLiteral(DartDoubleLiteral node, FlowEnv parameter) {
      return DoubleType.constant(node.getValue());
    }

    @Override
    public Type visitBooleanLiteral(DartBooleanLiteral node, FlowEnv parameter) {
      return BoolType.constant(node.getValue());
    }

    @Override
    public Type visitIdentifier(DartIdentifier node, FlowEnv parameter) {
      switch (node.getElement().getKind()) {
      case VARIABLE:
      case PARAMETER:
        return parameter.getType((VariableElement) node.getElement());
      case FIELD:
        return asType(true, node.getElement().getType());
      case CLASS:
        // not sure if this work...
        return accept(node.getElement().getNode(), parameter);
      case METHOD:
        return asType(false, node.getElement().getType());
      default:
        throw new AssertionError("visitIndentifier must be complete for " + node.getElement().getKind());
      }
    }

    @Override
    public Type visitBinaryExpression(DartBinaryExpression node, FlowEnv parameter) {
      DartExpression arg1 = node.getArg1();
      DartExpression arg2 = node.getArg2();
      switch (node.getOperator()) {
      case ASSIGN:
        Element arg1Element = arg1.getElement();
        switch (arg1Element.getKind()) {
        case VARIABLE:
        case PARAMETER:
          parameter.register((VariableElement) arg1Element, Types.union(accept(arg1, parameter), accept(arg2, parameter)));
          break;
        default:
          throw new AssertionError("Binary Expr: " + arg1Element.getKind() + " not implemented");
        }

        break;

      case SUB:
        return Types.union(accept(arg1, parameter), accept(arg2, parameter));

      case EQ_STRICT:
        break;

      default:
        throw new AssertionError("Binary Expr: " + node.getOperator() + " not implemented");
      }
      return null;
    }

    @Override
    public Type visitExprStmt(DartExprStmt node, FlowEnv parameter) {
      DartExpression expression = node.getExpression();
      if (expression != null) {
        return accept(expression, parameter);
      }

      return null;
    }

    @Override
    public Type visitTypeNode(DartTypeNode node, FlowEnv parameter) {
      for (DartTypeNode typeNode : node.getTypeArguments()) {
        accept(typeNode, parameter);
      }
      return asType(true, node.getType());
    }

    // @Override
    // public Type visitIfStatement(DartIfStatement node, FlowEnv parameter) {
    // accept(node.getThenStatement(), parameter);
    // accept(node.getElseStatement(), parameter);
    // return null;
    // }

    @Override
    public Type visitUnit(DartUnit node, FlowEnv unused) {
      System.out.println("Unit:" + node.getSourceName());
      for (DartNode child : node.getTopLevelNodes()) {
        accept(child, null);
      }
      return null;
    }
  }
}
