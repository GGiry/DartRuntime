import java.util.HashMap;

import type.BoolType;
import type.CoreTypeRepository;
import type.DoubleType;
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
import com.google.dart.compiler.resolver.VariableElement;

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
      case FUNCTION:
      case FUNCTION_ALIAS:
        return CoreTypeRepository.FUNCTION_TYPE;
      case VARIABLE:
        return typeRepository.findType(nullable, (ClassElement) type.getElement());
      case INTERFACE:
        return CoreTypeRepository.INTERFACE_TYPE;
      case NONE:
      default:
        throw new AssertionError("asType: " + type.getKind() + " must be implemented");
      }
    }

    public static void flowTyping(DartNode node, CoreTypeRepository coreTypeRepository) {
      node.accept(new FTVisitor(coreTypeRepository).asASTVisitor());
    }

    public static void flowTyping(DartNode node, TypeRepository typeRepository) {
      node.accept(new FTVisitor(typeRepository).asASTVisitor());
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
      return null;
    }

    @Override
    public Type visitUnqualifiedInvocation(DartUnqualifiedInvocation node, FlowEnv parameter) {
      flowTyping(node.getTarget().getElement().getNode(), typeRepository);
      return null;
    }

    @Override
    public Type visitThrowStatement(DartThrowStatement node, FlowEnv parameter) {
      // TODO nothing to do?
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

      System.out.println(env);

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
        return accept(node.getElement().getNode(), parameter);
      default:
        throw new AssertionError("visitIndentifier must be complete for " + node.getElement().getKind());
      }
    }

    @Override
    public Type visitBinaryExpression(DartBinaryExpression node, FlowEnv parameter) {
      switch (node.getOperator()) {
      case ASSIGN:
        switch (node.getArg1().getElement().getKind()) {
        case VARIABLE:
        case PARAMETER:
          parameter.register((VariableElement) node.getArg1().getElement(), Types.union(accept(node.getArg1(), parameter), accept(node.getArg2(), parameter)));
          break;
        default:
          throw new AssertionError("Binary Expr: " + node.getArg1().getElement().getKind() + " not implemented");
        }

        break;

      case SUB:
        return Types.union(accept(node.getArg1(), parameter), accept(node.getArg2(), parameter));

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
