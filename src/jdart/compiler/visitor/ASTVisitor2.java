package jdart.compiler.visitor;

import com.google.dart.compiler.ast.ASTVisitor;
import com.google.dart.compiler.ast.DartArrayAccess;
import com.google.dart.compiler.ast.DartArrayLiteral;
import com.google.dart.compiler.ast.DartAssertion;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartBooleanLiteral;
import com.google.dart.compiler.ast.DartBreakStatement;
import com.google.dart.compiler.ast.DartCase;
import com.google.dart.compiler.ast.DartCatchBlock;
import com.google.dart.compiler.ast.DartClass;
import com.google.dart.compiler.ast.DartClassMember;
import com.google.dart.compiler.ast.DartComment;
import com.google.dart.compiler.ast.DartConditional;
import com.google.dart.compiler.ast.DartContinueStatement;
import com.google.dart.compiler.ast.DartDeclaration;
import com.google.dart.compiler.ast.DartDefault;
import com.google.dart.compiler.ast.DartDirective;
import com.google.dart.compiler.ast.DartDoWhileStatement;
import com.google.dart.compiler.ast.DartDoubleLiteral;
import com.google.dart.compiler.ast.DartEmptyStatement;
import com.google.dart.compiler.ast.DartExprStmt;
import com.google.dart.compiler.ast.DartExpression;
import com.google.dart.compiler.ast.DartField;
import com.google.dart.compiler.ast.DartFieldDefinition;
import com.google.dart.compiler.ast.DartForInStatement;
import com.google.dart.compiler.ast.DartForStatement;
import com.google.dart.compiler.ast.DartFunction;
import com.google.dart.compiler.ast.DartFunctionExpression;
import com.google.dart.compiler.ast.DartFunctionObjectInvocation;
import com.google.dart.compiler.ast.DartFunctionTypeAlias;
import com.google.dart.compiler.ast.DartGotoStatement;
import com.google.dart.compiler.ast.DartIdentifier;
import com.google.dart.compiler.ast.DartIfStatement;
import com.google.dart.compiler.ast.DartImportDirective;
import com.google.dart.compiler.ast.DartInitializer;
import com.google.dart.compiler.ast.DartIntegerLiteral;
import com.google.dart.compiler.ast.DartInvocation;
import com.google.dart.compiler.ast.DartLabel;
import com.google.dart.compiler.ast.DartLibraryDirective;
import com.google.dart.compiler.ast.DartLiteral;
import com.google.dart.compiler.ast.DartMapLiteral;
import com.google.dart.compiler.ast.DartMapLiteralEntry;
import com.google.dart.compiler.ast.DartMethodDefinition;
import com.google.dart.compiler.ast.DartMethodInvocation;
import com.google.dart.compiler.ast.DartNamedExpression;
import com.google.dart.compiler.ast.DartNativeBlock;
import com.google.dart.compiler.ast.DartNativeDirective;
import com.google.dart.compiler.ast.DartNewExpression;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartNullLiteral;
import com.google.dart.compiler.ast.DartParameter;
import com.google.dart.compiler.ast.DartParameterizedTypeNode;
import com.google.dart.compiler.ast.DartParenthesizedExpression;
import com.google.dart.compiler.ast.DartPropertyAccess;
import com.google.dart.compiler.ast.DartRedirectConstructorInvocation;
import com.google.dart.compiler.ast.DartResourceDirective;
import com.google.dart.compiler.ast.DartReturnStatement;
import com.google.dart.compiler.ast.DartSourceDirective;
import com.google.dart.compiler.ast.DartStatement;
import com.google.dart.compiler.ast.DartStringInterpolation;
import com.google.dart.compiler.ast.DartStringLiteral;
import com.google.dart.compiler.ast.DartSuperConstructorInvocation;
import com.google.dart.compiler.ast.DartSuperExpression;
import com.google.dart.compiler.ast.DartSwitchMember;
import com.google.dart.compiler.ast.DartSwitchStatement;
import com.google.dart.compiler.ast.DartSyntheticErrorExpression;
import com.google.dart.compiler.ast.DartSyntheticErrorIdentifier;
import com.google.dart.compiler.ast.DartSyntheticErrorStatement;
import com.google.dart.compiler.ast.DartThisExpression;
import com.google.dart.compiler.ast.DartThrowStatement;
import com.google.dart.compiler.ast.DartTryStatement;
import com.google.dart.compiler.ast.DartTypeExpression;
import com.google.dart.compiler.ast.DartTypeNode;
import com.google.dart.compiler.ast.DartTypeParameter;
import com.google.dart.compiler.ast.DartUnaryExpression;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.ast.DartUnqualifiedInvocation;
import com.google.dart.compiler.ast.DartVariable;
import com.google.dart.compiler.ast.DartVariableStatement;
import com.google.dart.compiler.ast.DartWhileStatement;

public class ASTVisitor2<R, P> {
  private final BridgeASTVisitor<R, P> bridgeASTVisitor;

  public ASTVisitor2() {
    bridgeASTVisitor = new BridgeASTVisitor<>(this);
  }

  public ASTVisitor<R> asASTVisitor() {
    return bridgeASTVisitor;
  }

  protected R accept(DartNode node, P parameter) {
    bridgeASTVisitor.parameter = parameter;
    try {
      return node.accept(bridgeASTVisitor);
    } finally {
      bridgeASTVisitor.parameter = null;
    }
  }
  
  protected void acceptChildren(DartNode node) {
    node.visitChildren(bridgeASTVisitor);
  }

  public R visitNode(DartNode node, P parameter) {
    throw new AssertionError("visit on " + node.getClass().getName() + " not implemented");
  }

  public R visitDirective(DartDirective node, P parameter) {
    return visitNode(node, parameter);
  }

  public R visitInvocation(DartInvocation node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitExpression(DartExpression node, P parameter) {
    return visitNode(node, parameter);
  }

  public R visitStatement(DartStatement node, P parameter) {
    return visitNode(node, parameter);
  }

  public R visitLiteral(DartLiteral node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitGotoStatement(DartGotoStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitSwitchMember(DartSwitchMember node, P parameter) {
    return visitNode(node, parameter);
  }

  public R visitDeclaration(DartDeclaration<?> node, P parameter) {
    return visitNode(node, parameter);
  }

  public R visitClassMember(DartClassMember<?> node, P parameter) {
    return visitDeclaration(node, parameter);
  }

  public R visitComment(DartComment node, P parameter) {
    return visitNode(node, parameter);
  }

  public R visitArrayAccess(DartArrayAccess node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitArrayLiteral(DartArrayLiteral node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitAssertion(DartAssertion node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitBinaryExpression(DartBinaryExpression node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitBlock(DartBlock node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitBooleanLiteral(DartBooleanLiteral node, P parameter) {
    return visitLiteral(node, parameter);
  }

  public R visitBreakStatement(DartBreakStatement node, P parameter) {
    return visitGotoStatement(node, parameter);
  }

  public R visitFunctionObjectInvocation(DartFunctionObjectInvocation node, P parameter) {
    return visitInvocation(node, parameter);
  }

  public R visitMethodInvocation(DartMethodInvocation node, P parameter) {
    return visitInvocation(node, parameter);
  }

  public R visitUnqualifiedInvocation(DartUnqualifiedInvocation node, P parameter) {
    return visitInvocation(node, parameter);
  }

  public R visitSuperConstructorInvocation(DartSuperConstructorInvocation node, P parameter) {
    return visitInvocation(node, parameter);
  }

  public R visitCase(DartCase node, P parameter) {
    return visitSwitchMember(node, parameter);
  }

  public R visitClass(DartClass node, P parameter) {
    return visitDeclaration(node, parameter);
  }

  public R visitConditional(DartConditional node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitContinueStatement(DartContinueStatement node, P parameter) {
    return visitGotoStatement(node, parameter);
  }

  public R visitDefault(DartDefault node, P parameter) {
    return visitSwitchMember(node, parameter);
  }

  public R visitDoubleLiteral(DartDoubleLiteral node, P parameter) {
    return visitLiteral(node, parameter);
  }

  public R visitDoWhileStatement(DartDoWhileStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitEmptyStatement(DartEmptyStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitExprStmt(DartExprStmt node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitField(DartField node, P parameter) {
    return visitClassMember(node, parameter);
  }

  public R visitFieldDefinition(DartFieldDefinition node, P parameter) {
    return visitNode(node, parameter);
  }

  public R visitForInStatement(DartForInStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitForStatement(DartForStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitFunction(DartFunction node, P parameter) {
    return visitNode(node, parameter);
  }

  public R visitFunctionExpression(DartFunctionExpression node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitFunctionTypeAlias(DartFunctionTypeAlias node, P parameter) {
    return visitDeclaration(node, parameter);
  }

  public R visitIdentifier(DartIdentifier node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitIfStatement(DartIfStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitImportDirective(DartImportDirective node, P parameter) {
    return visitDirective(node, parameter);
  }

  public R visitInitializer(DartInitializer node, P parameter) {
    return visitNode(node, parameter);
  }

  public R visitIntegerLiteral(DartIntegerLiteral node, P parameter) {
    return visitLiteral(node, parameter);
  }

  public R visitLabel(DartLabel node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitLibraryDirective(DartLibraryDirective node, P parameter) {
    return visitDirective(node, parameter);
  }

  public R visitMapLiteral(DartMapLiteral node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitMapLiteralEntry(DartMapLiteralEntry node, P parameter) {
    return visitNode(node, parameter);
  }

  public R visitMethodDefinition(DartMethodDefinition node, P parameter) {
    return visitClassMember(node, parameter);
  }

  public R visitNativeDirective(DartNativeDirective node, P parameter) {
    return visitDirective(node, parameter);
  }

  public R visitNewExpression(DartNewExpression node, P parameter) {
    return visitInvocation(node, parameter);
  }

  public R visitNullLiteral(DartNullLiteral node, P parameter) {
    return visitLiteral(node, parameter);
  }

  public R visitParameter(DartParameter node, P parameter) {
    return visitDeclaration(node, parameter);
  }

  public R visitParameterizedTypeNode(DartParameterizedTypeNode node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitParenthesizedExpression(DartParenthesizedExpression node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitPropertyAccess(DartPropertyAccess node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitTypeNode(DartTypeNode node, P parameter) {
    return visitNode(node, parameter);
  }

  public R visitResourceDirective(DartResourceDirective node, P parameter) {
    return visitDirective(node, parameter);
  }

  public R visitReturnStatement(DartReturnStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitSourceDirective(DartSourceDirective node, P parameter) {
    return visitDirective(node, parameter);
  }

  public R visitStringLiteral(DartStringLiteral node, P parameter) {
    return visitLiteral(node, parameter);
  }

  public R visitStringInterpolation(DartStringInterpolation node, P parameter) {
    return visitLiteral(node, parameter);
  }

  public R visitSuperExpression(DartSuperExpression node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitSwitchStatement(DartSwitchStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitSyntheticErrorExpression(DartSyntheticErrorExpression node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitSyntheticErrorIdentifier(DartSyntheticErrorIdentifier node, P parameter) {
    return visitIdentifier(node, parameter);
  }

  public R visitSyntheticErrorStatement(DartSyntheticErrorStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitThisExpression(DartThisExpression node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitThrowStatement(DartThrowStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitCatchBlock(DartCatchBlock node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitTryStatement(DartTryStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitUnaryExpression(DartUnaryExpression node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitUnit(DartUnit node, P parameter) {
    return visitNode(node, parameter);
  }

  public R visitVariable(DartVariable node, P parameter) {
    return visitDeclaration(node, parameter);
  }

  public R visitVariableStatement(DartVariableStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitWhileStatement(DartWhileStatement node, P parameter) {
    return visitStatement(node, parameter);
  }

  public R visitNamedExpression(DartNamedExpression node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitTypeExpression(DartTypeExpression node, P parameter) {
    return visitExpression(node, parameter);
  }

  public R visitTypeParameter(DartTypeParameter node, P parameter) {
    return visitDeclaration(node, parameter);
  }

  public R visitNativeBlock(DartNativeBlock node, P parameter) {
    return visitBlock(node, parameter);
  }

  public R visitRedirectConstructorInvocation(DartRedirectConstructorInvocation node, P parameter) {
    return visitInvocation(node, parameter);
  }
}
