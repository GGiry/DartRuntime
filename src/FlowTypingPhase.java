import java.util.HashMap;

import type.CoreTypeRepository;
import type.IntType;
import type.Type;
import type.TypeRepository;
import type.Types;
import visitor.ASTVisitor2;

import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartExprStmt;
import com.google.dart.compiler.ast.DartFunction;
import com.google.dart.compiler.ast.DartIdentifier;
import com.google.dart.compiler.ast.DartIntegerLiteral;
import com.google.dart.compiler.ast.DartMethodDefinition;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartParameter;
import com.google.dart.compiler.ast.DartStatement;
import com.google.dart.compiler.ast.DartTypeNode;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.ast.DartVariable;
import com.google.dart.compiler.ast.DartVariableStatement;
import com.google.dart.compiler.resolver.CoreTypeProvider;

public class FlowTypingPhase implements DartCompilationPhase {
  @Override
  public DartUnit exec(DartUnit unit, DartCompilerContext context, CoreTypeProvider coreTypeProvider) {

    System.err.println("CoreTypeProvider: " + coreTypeProvider);

    // initialize core type repository
    CoreTypeRepository coreTypeRepository = CoreTypeRepository.initCoreTypeRepository(coreTypeProvider);

    unit.accept(new FTVisitor(coreTypeRepository).asASTVisitor());
    return unit;
  }

  private static class FTVisitor extends ASTVisitor2<Type, FlowEnv> {
    private final TypeRepository typeRepository;
    private final HashMap<DartNode, Type> typeMap = new HashMap<>();

    FTVisitor(CoreTypeRepository coreTypeRepository) {
      this.typeRepository = new TypeRepository(coreTypeRepository);
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
    public Type visitMethodDefinition(DartMethodDefinition node, FlowEnv parameter) {
      accept(node.getFunction(), parameter);
      return null;
    }

    @Override
    public Type visitFunction(DartFunction node, FlowEnv parameter) {
      FlowEnv env = new FlowEnv(parameter);

      for (DartParameter param : node.getParameters()) {
        parameter.register(param.getElement(), accept(param.getTypeNode(), parameter));
      }

      accept(node.getBody(), env);

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
      Type type = accept(node.getValue(), parameter);
      parameter.register(node.getElement(), type);
      return type;
    }

    @Override
    public Type visitIntegerLiteral(DartIntegerLiteral node, FlowEnv parameter) {
      return IntType.constant(node.getValue());
    }

    @Override
    public Type visitIdentifier(DartIdentifier node, FlowEnv parameter) {

      return null;
    }

    @Override
    public Type visitBinaryExpression(DartBinaryExpression node, FlowEnv parameter) {
      System.out.println("visitBinaryNode: " + node);
      switch (node.getOperator()) {
      case ASSIGN:
        System.out.println(Types.union(accept(node.getArg1(), parameter), accept(node.getArg2(), parameter)));

        break;

      default:
        break;
      }
      return null;
    }

    @Override
    public Type visitExprStmt(DartExprStmt node, FlowEnv parameter) {
      accept(node.getExpression(), parameter);
      return null;
    }

    @Override
    public Type visitTypeNode(DartTypeNode node, FlowEnv parameter) {
      System.out.println("visitTypeNode: " + node + ", " + node.getTypeArguments());
      for (DartTypeNode dtn : node.getTypeArguments()) {
        System.out.println("visitTypeNode: " + dtn);
      }
      return null;
    }

    @Override
    public Type visitUnit(DartUnit node, FlowEnv unused) {
      for (DartNode child : node.getTopLevelNodes()) {
        accept(child, null);
      }
      return null;
    }
  }
}
