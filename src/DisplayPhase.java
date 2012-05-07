import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.ast.ASTVisitor;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartClass;
import com.google.dart.compiler.ast.DartExprStmt;
import com.google.dart.compiler.ast.DartExpression;
import com.google.dart.compiler.ast.DartFunction;
import com.google.dart.compiler.ast.DartFunctionObjectInvocation;
import com.google.dart.compiler.ast.DartIdentifier;
import com.google.dart.compiler.ast.DartIntegerLiteral;
import com.google.dart.compiler.ast.DartMethodDefinition;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartStringLiteral;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.ast.DartUnqualifiedInvocation;
import com.google.dart.compiler.resolver.CoreTypeProvider;

public class DisplayPhase implements DartCompilationPhase {
  @Override
  public DartUnit exec(DartUnit unit, DartCompilerContext context, CoreTypeProvider typeProvider) {
    unit.accept(new DisplayVisitor());
    return unit;
  }

  private class DisplayVisitor extends ASTVisitor<Void> {
    @Override
    public Void visitNode(DartNode node) {
      // System.out.println(node.getObjectIdentifier());
      return super.visitNode(node);
    }

    @Override
    public Void visitIdentifier(DartIdentifier node) {
      System.out.println("Identifier: " + node.getName());
      return super.visitIdentifier(node);
    }

    @Override
    public Void visitClass(DartClass node) {
      System.out.println("New Class: " + node.getClassName());
      return super.visitClass(node);
    }

    @Override
    public Void visitFunction(DartFunction node) {
      System.out.println("Fun ret: " + node.getReturnTypeNode());
      System.out.println("Fun parameters: " + node.getParameters());
      return super.visitFunction(node);
    }

    @Override
    public Void visitMethodDefinition(DartMethodDefinition node) {
      System.out.println("Method: " + node.getName());
      return super.visitMethodDefinition(node);
    }

    @Override
    public Void visitFunctionObjectInvocation(DartFunctionObjectInvocation node) {
      System.out.println("Fun invoke: " + node.getTarget());
      return super.visitFunctionObjectInvocation(node);
    }

    @Override
    public Void visitBlock(DartBlock node) {
      System.out.println("BeginBlock");
      try {
        return visitStatement(node);
      } finally {
        System.out.println("EndBlock");
      }
    }

    @Override
    public Void visitExpression(DartExpression node) {
      System.out.println("Exp: " + node);
      return super.visitExpression(node);
    }

    @Override
    public Void visitExprStmt(DartExprStmt node) {
      System.out.println("ExprStmt: " + node.getExpression());
      return super.visitExprStmt(node);
    }

    @Override
    public Void visitStringLiteral(DartStringLiteral node) {
      System.out.println("Sring literal: " + node);
      return super.visitStringLiteral(node);
    }

    @Override
    public Void visitIntegerLiteral(DartIntegerLiteral node) {
      System.out.println("Integer: " + node.getValue());
      return super.visitIntegerLiteral(node);
    }

    @Override
    public Void visitUnqualifiedInvocation(DartUnqualifiedInvocation node) {
      System.out.println("Unq invoke: " + node);
      return super.visitUnqualifiedInvocation(node);
    }

    @Override
    public Void visitBinaryExpression(DartBinaryExpression node) {
      System.out.println("Binary exp: " + node.getArg1() + " " + node.getOperator() + " " + node.getArg2());
      return super.visitBinaryExpression(node);
    }
  }
}