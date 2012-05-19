package jdart.phase;
import java.util.HashMap;

import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.ast.ASTVisitor;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartExpression;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.parser.Token;
import com.google.dart.compiler.resolver.CoreTypeProvider;

public class InterpretorPhase implements DartCompilationPhase {

  private static HashMap<String, DartExpression> map = new HashMap<>();

  @Override
  public DartUnit exec(DartUnit unit, DartCompilerContext context, CoreTypeProvider typeProvider) {
    unit.accept(new InterpretorVisitor());
    return unit;
  }

  private class InterpretorVisitor extends ASTVisitor<Void> {
    @Override
    public Void visitBlock(DartBlock node) {
      HashMap<String, DartExpression> old = (HashMap<String, DartExpression>) map.clone();
      try {
        return super.visitBlock(node);
      } finally {
        System.out.println(map);
        map = old;
      }
    }

    @Override
    public Void visitBinaryExpression(DartBinaryExpression node) {
      if (node.getOperator() == Token.ASSIGN) {
        map.put(node.getArg1().toString(), node.getArg2());
      }
      return super.visitBinaryExpression(node);
    }
  }
}
