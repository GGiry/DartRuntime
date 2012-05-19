package jdart.visitor;

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

class BridgeASTVisitor<R, P> extends ASTVisitor<R> {
  private final ASTVisitor2<R, P> visitor2;

  BridgeASTVisitor(ASTVisitor2<R, P> visitor2) {
    this.visitor2 = visitor2;
  }

  P parameter;

  @Override
  public R visitNode(DartNode node) {
    return visitor2.visitNode(node, parameter);
  }

  @Override
  public R visitDirective(DartDirective node) {
    return visitor2.visitDirective(node, parameter);
  }

  @Override
  public R visitInvocation(DartInvocation node) {
    return visitor2.visitInvocation(node, parameter);
  }

  @Override
  public R visitExpression(DartExpression node) {
    return visitor2.visitExpression(node, parameter);
  }

  @Override
  public R visitStatement(DartStatement node) {
    return visitor2.visitStatement(node, parameter);
  }

  @Override
  public R visitLiteral(DartLiteral node) {
    return visitor2.visitLiteral(node, parameter);
  }

  @Override
  public R visitGotoStatement(DartGotoStatement node) {
    return visitor2.visitGotoStatement(node, parameter);
  }

  @Override
  public R visitSwitchMember(DartSwitchMember node) {
    return visitor2.visitSwitchMember(node, parameter);
  }

  @Override
  public R visitDeclaration(DartDeclaration<?> node) {
    return visitor2.visitDeclaration(node, parameter);
  }

  @Override
  public R visitClassMember(DartClassMember<?> node) {
    return visitor2.visitClassMember(node, parameter);
  }

  @Override
  public R visitComment(DartComment node) {
    return visitor2.visitComment(node, parameter);
  }

  @Override
  public R visitArrayAccess(DartArrayAccess node) {
    return visitor2.visitArrayAccess(node, parameter);
  }

  @Override
  public R visitArrayLiteral(DartArrayLiteral node) {
    return visitor2.visitArrayLiteral(node, parameter);
  }

  @Override
  public R visitAssertion(DartAssertion node) {
    return visitor2.visitAssertion(node, parameter);
  }

  @Override
  public R visitBinaryExpression(DartBinaryExpression node) {
    return visitor2.visitBinaryExpression(node, parameter);
  }

  @Override
  public R visitBlock(DartBlock node) {
    return visitor2.visitBlock(node, parameter);
  }

  @Override
  public R visitBooleanLiteral(DartBooleanLiteral node) {
    return visitor2.visitBooleanLiteral(node, parameter);
  }

  @Override
  public R visitBreakStatement(DartBreakStatement node) {
    return visitor2.visitBreakStatement(node, parameter);
  }

  @Override
  public R visitFunctionObjectInvocation(DartFunctionObjectInvocation node) {
    return visitor2.visitFunctionObjectInvocation(node, parameter);
  }

  @Override
  public R visitMethodInvocation(DartMethodInvocation node) {
    return visitor2.visitMethodInvocation(node, parameter);
  }

  @Override
  public R visitUnqualifiedInvocation(DartUnqualifiedInvocation node) {
    return visitor2.visitUnqualifiedInvocation(node, parameter);
  }

  @Override
  public R visitSuperConstructorInvocation(DartSuperConstructorInvocation node) {
    return visitor2.visitSuperConstructorInvocation(node, parameter);
  }

  @Override
  public R visitCase(DartCase node) {
    return visitor2.visitCase(node, parameter);
  }

  @Override
  public R visitClass(DartClass node) {
    return visitor2.visitClass(node, parameter);
  }

  @Override
  public R visitConditional(DartConditional node) {
    return visitor2.visitConditional(node, parameter);
  }

  @Override
  public R visitContinueStatement(DartContinueStatement node) {
    return visitor2.visitContinueStatement(node, parameter);
  }

  @Override
  public R visitDefault(DartDefault node) {
    return visitor2.visitDefault(node, parameter);
  }

  @Override
  public R visitDoubleLiteral(DartDoubleLiteral node) {
    return visitor2.visitDoubleLiteral(node, parameter);
  }

  @Override
  public R visitDoWhileStatement(DartDoWhileStatement node) {
    return visitor2.visitDoWhileStatement(node, parameter);
  }

  @Override
  public R visitEmptyStatement(DartEmptyStatement node) {
    return visitor2.visitEmptyStatement(node, parameter);
  }

  @Override
  public R visitExprStmt(DartExprStmt node) {
    return visitor2.visitExprStmt(node, parameter);
  }

  @Override
  public R visitField(DartField node) {
    return visitor2.visitField(node, parameter);
  }

  @Override
  public R visitFieldDefinition(DartFieldDefinition node) {
    return visitor2.visitFieldDefinition(node, parameter);
  }

  @Override
  public R visitForInStatement(DartForInStatement node) {
    return visitor2.visitForInStatement(node, parameter);
  }

  @Override
  public R visitForStatement(DartForStatement node) {
    return visitor2.visitForStatement(node, parameter);
  }

  @Override
  public R visitFunction(DartFunction node) {
    return visitor2.visitFunction(node, parameter);
  }

  @Override
  public R visitFunctionExpression(DartFunctionExpression node) {
    return visitor2.visitFunctionExpression(node, parameter);
  }

  @Override
  public R visitFunctionTypeAlias(DartFunctionTypeAlias node) {
    return visitor2.visitFunctionTypeAlias(node, parameter);
  }

  @Override
  public R visitIdentifier(DartIdentifier node) {
    return visitor2.visitIdentifier(node, parameter);
  }

  @Override
  public R visitIfStatement(DartIfStatement node) {
    return visitor2.visitIfStatement(node, parameter);
  }

  @Override
  public R visitImportDirective(DartImportDirective node) {
    return visitor2.visitImportDirective(node, parameter);
  }

  @Override
  public R visitInitializer(DartInitializer node) {
    return visitor2.visitInitializer(node, parameter);
  }

  @Override
  public R visitIntegerLiteral(DartIntegerLiteral node) {
    return visitor2.visitIntegerLiteral(node, parameter);
  }

  @Override
  public R visitLabel(DartLabel node) {
    return visitor2.visitLabel(node, parameter);
  }

  @Override
  public R visitLibraryDirective(DartLibraryDirective node) {
    return visitor2.visitLibraryDirective(node, parameter);
  }

  @Override
  public R visitMapLiteral(DartMapLiteral node) {
    return visitor2.visitMapLiteral(node, parameter);
  }

  @Override
  public R visitMapLiteralEntry(DartMapLiteralEntry node) {
    return visitor2.visitMapLiteralEntry(node, parameter);
  }

  @Override
  public R visitMethodDefinition(DartMethodDefinition node) {
    return visitor2.visitMethodDefinition(node, parameter);
  }

  @Override
  public R visitNativeDirective(DartNativeDirective node) {
    return visitor2.visitNativeDirective(node, parameter);
  }

  @Override
  public R visitNewExpression(DartNewExpression node) {
    return visitor2.visitNewExpression(node, parameter);
  }

  @Override
  public R visitNullLiteral(DartNullLiteral node) {
    return visitor2.visitNullLiteral(node, parameter);
  }

  @Override
  public R visitParameter(DartParameter node) {
    return visitor2.visitParameter(node, parameter);
  }

  @Override
  public R visitParameterizedTypeNode(DartParameterizedTypeNode node) {
    return visitor2.visitParameterizedTypeNode(node, parameter);
  }

  @Override
  public R visitParenthesizedExpression(DartParenthesizedExpression node) {
    return visitor2.visitParenthesizedExpression(node, parameter);
  }

  @Override
  public R visitPropertyAccess(DartPropertyAccess node) {
    return visitor2.visitPropertyAccess(node, parameter);
  }

  @Override
  public R visitTypeNode(DartTypeNode node) {
    return visitor2.visitTypeNode(node, parameter);
  }

  @Override
  public R visitResourceDirective(DartResourceDirective node) {
    return visitor2.visitResourceDirective(node, parameter);
  }

  @Override
  public R visitReturnStatement(DartReturnStatement node) {
    return visitor2.visitReturnStatement(node, parameter);
  }

  @Override
  public R visitSourceDirective(DartSourceDirective node) {
    return visitor2.visitSourceDirective(node, parameter);
  }

  @Override
  public R visitStringLiteral(DartStringLiteral node) {
    return visitor2.visitStringLiteral(node, parameter);
  }

  @Override
  public R visitStringInterpolation(DartStringInterpolation node) {
    return visitor2.visitStringInterpolation(node, parameter);
  }

  @Override
  public R visitSuperExpression(DartSuperExpression node) {
    return visitor2.visitSuperExpression(node, parameter);
  }

  @Override
  public R visitSwitchStatement(DartSwitchStatement node) {
    return visitor2.visitSwitchStatement(node, parameter);
  }

  @Override
  public R visitSyntheticErrorExpression(DartSyntheticErrorExpression node) {
    return visitor2.visitSyntheticErrorExpression(node, parameter);
  }

  @Override
  public R visitSyntheticErrorIdentifier(DartSyntheticErrorIdentifier node) {
    return visitor2.visitSyntheticErrorIdentifier(node, parameter);
  }

  @Override
  public R visitSyntheticErrorStatement(DartSyntheticErrorStatement node) {
    return visitor2.visitSyntheticErrorStatement(node, parameter);
  }

  @Override
  public R visitThisExpression(DartThisExpression node) {
    return visitor2.visitThisExpression(node, parameter);
  }

  @Override
  public R visitThrowStatement(DartThrowStatement node) {
    return visitor2.visitThrowStatement(node, parameter);
  }

  @Override
  public R visitCatchBlock(DartCatchBlock node) {
    return visitor2.visitCatchBlock(node, parameter);
  }

  @Override
  public R visitTryStatement(DartTryStatement node) {
    return visitor2.visitTryStatement(node, parameter);
  }

  @Override
  public R visitUnaryExpression(DartUnaryExpression node) {
    return visitor2.visitUnaryExpression(node, parameter);
  }

  @Override
  public R visitUnit(DartUnit node) {
    return visitor2.visitUnit(node, parameter);
  }

  @Override
  public R visitVariable(DartVariable node) {
    return visitor2.visitVariable(node, parameter);
  }

  @Override
  public R visitVariableStatement(DartVariableStatement node) {
    return visitor2.visitVariableStatement(node, parameter);
  }

  @Override
  public R visitWhileStatement(DartWhileStatement node) {
    return visitor2.visitWhileStatement(node, parameter);
  }

  @Override
  public R visitNamedExpression(DartNamedExpression node) {
    return visitor2.visitNamedExpression(node, parameter);
  }

  @Override
  public R visitTypeExpression(DartTypeExpression node) {
    return visitor2.visitTypeExpression(node, parameter);
  }

  @Override
  public R visitTypeParameter(DartTypeParameter node) {
    return visitor2.visitTypeParameter(node, parameter);
  }

  @Override
  public R visitNativeBlock(DartNativeBlock node) {
    return visitor2.visitNativeBlock(node, parameter);
  }

  @Override
  public R visitRedirectConstructorInvocation(DartRedirectConstructorInvocation node) {
    return visitor2.visitRedirectConstructorInvocation(node, parameter);
  }

}
