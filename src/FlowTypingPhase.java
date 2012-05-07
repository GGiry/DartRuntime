import java.util.HashMap;

import type.CoreTypeRepository;
import type.IntType;
import type.Type;
import type.TypeRepository;
import visitor.ASTVisitor2;

import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartFunction;
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
  public DartUnit exec(DartUnit unit, DartCompilerContext context,
      CoreTypeProvider coreTypeProvider) {

    System.err.println(coreTypeProvider);

    // initialize core type repository
    CoreTypeRepository coreTypeRepository = CoreTypeRepository
        .initCoreTypeRepository(coreTypeProvider);

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
    public Type visitMethodDefinition(DartMethodDefinition node,
        FlowEnv parameter) {
      accept(node.getFunction(), parameter);
      return null;
    }

    @Override
    public Type visitFunction(DartFunction node, FlowEnv parameter) {
      FlowEnv old = parameter;
      parameter = new FlowEnv();

      for (DartParameter param : node.getParameters()) {
        parameter.register(param.getElement(),
            accept(param.getTypeNode(), parameter));
      }

      accept(node.getBody(), parameter);

      parameter = old;
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
    public Type visitVariableStatement(DartVariableStatement node,
        FlowEnv parameter) {
      for (DartVariable variable : node.getVariables()) {
        accept(variable, parameter);
      }
      return null;
    }

    @Override
    public Type visitVariable(DartVariable node, FlowEnv parameter) {
      Type type = accept(node.getValue(), parameter);
      System.out.println(parameter);
      parameter.register(node.getElement(), type);
      return type;
    }

    @Override
    public Type visitIntegerLiteral(DartIntegerLiteral node, FlowEnv parameter) {
      return IntType.constant(node.getValue());
    }

    @Override
    public Type visitTypeNode(DartTypeNode node, FlowEnv parameter) {
      for (DartTypeNode dtn : node.getTypeArguments()) {
        System.out.println("visitTypeNode: " + dtn);
      }
      return null;
    }

    @Override
    public Type visitUnit(DartUnit node, FlowEnv parameter) {
      for (DartNode child : node.getTopLevelNodes()) {
        accept(child, parameter);
      }
      return null;
    }
  }
}
