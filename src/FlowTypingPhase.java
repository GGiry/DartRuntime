import java.util.HashMap;
import java.util.List;
import java.util.Set;

import type.Type;
import variable.Variable;
import visitor.ASTVisitor2;

import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
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
import com.google.dart.compiler.resolver.CoreTypeProvider;

public class FlowTypingPhase implements DartCompilationPhase {
	@Override
	public DartUnit exec(DartUnit unit, DartCompilerContext context,
			CoreTypeProvider typeProvider) {
		unit.accept(new FTVisitor());
		return unit;
	}

	private class FTVisitor extends ASTVisitor<Set<Type>> {
		private final ASTVisitor2<Set<Type>, HashMap<Variable, Set<Type>>> visitor = new ASTVisitor2<>();
		private HashMap<Variable, Set<Type>> map = new HashMap<>();

		@Override
		public Set<Type> visitNode(DartNode node) {
			return visitor.visitNode(node, map);
		}

		@Override
		public Set<Type> visitDirective(DartDirective node) {
			return visitor.visitDirective(node, map);
		}

		@Override
		public Set<Type> visitInvocation(DartInvocation node) {
			return visitor.visitInvocation(node, map);
		}

		@Override
		public Set<Type> visitExpression(DartExpression node) {
			return visitor.visitExpression(node, map);
		}

		@Override
		public Set<Type> visitStatement(DartStatement node) {
			return visitor.visitStatement(node, map);
		}

		@Override
		public Set<Type> visitLiteral(DartLiteral node) {
			return visitor.visitLiteral(node, map);
		}

		@Override
		public Set<Type> visitGotoStatement(DartGotoStatement node) {
			return visitor.visitGotoStatement(node, map);
		}

		@Override
		public Set<Type> visitSwitchMember(DartSwitchMember node) {
			return visitor.visitSwitchMember(node, map);
		}

		@Override
		public Set<Type> visitDeclaration(DartDeclaration<?> node) {
			return visitor.visitDeclaration(node, map);
		}

		@Override
		public Set<Type> visitClassMember(DartClassMember<?> node) {
			return visitor.visitClassMember(node, map);
		}

		@Override
		public Set<Type> visitComment(DartComment node) {
			return visitor.visitComment(node, map);
		}

		@Override
		public Set<Type> visitArrayAccess(DartArrayAccess node) {
			return visitor.visitArrayAccess(node, map);
		}

		@Override
		public Set<Type> visitArrayLiteral(DartArrayLiteral node) {
			return visitor.visitArrayLiteral(node, map);
		}

		@Override
		public Set<Type> visitAssertion(DartAssertion node) {
			return visitor.visitAssertion(node, map);
		}

		@Override
		public Set<Type> visitBinaryExpression(DartBinaryExpression node) {
			return visitor.visitBinaryExpression(node, map);
		}

		@Override
		public Set<Type> visitBlock(DartBlock node) {
			return visitor.visitBlock(node, map);
		}

		@Override
		public Set<Type> visitBooleanLiteral(DartBooleanLiteral node) {
			return visitor.visitBooleanLiteral(node, map);
		}

		@Override
		public Set<Type> visitBreakStatement(DartBreakStatement node) {
			return visitor.visitBreakStatement(node, map);
		}

		@Override
		public Set<Type> visitFunctionObjectInvocation(
				DartFunctionObjectInvocation node) {
			return visitor.visitFunctionObjectInvocation(node, map);
		}

		@Override
		public Set<Type> visitMethodInvocation(DartMethodInvocation node) {
			return visitor.visitMethodInvocation(node, map);
		}

		@Override
		public Set<Type> visitUnqualifiedInvocation(
				DartUnqualifiedInvocation node) {
			return visitor.visitUnqualifiedInvocation(node, map);
		}

		@Override
		public Set<Type> visitSuperConstructorInvocation(
				DartSuperConstructorInvocation node) {
			return visitor.visitSuperConstructorInvocation(node, map);
		}

		@Override
		public Set<Type> visitCase(DartCase node) {
			return visitor.visitCase(node, map);
		}

		@Override
		public Set<Type> visitClass(DartClass node) {
			return visitor.visitClass(node, map);
		}

		@Override
		public Set<Type> visitConditional(DartConditional node) {
			return visitor.visitConditional(node, map);
		}

		@Override
		public Set<Type> visitContinueStatement(DartContinueStatement node) {
			return visitor.visitContinueStatement(node, map);
		}

		@Override
		public Set<Type> visitDefault(DartDefault node) {
			return visitor.visitDefault(node, map);
		}

		@Override
		public Set<Type> visitDoubleLiteral(DartDoubleLiteral node) {
			return visitor.visitDoubleLiteral(node, map);
		}

		@Override
		public Set<Type> visitDoWhileStatement(DartDoWhileStatement node) {
			return visitor.visitDoWhileStatement(node, map);
		}

		@Override
		public Set<Type> visitEmptyStatement(DartEmptyStatement node) {
			return visitor.visitEmptyStatement(node, map);
		}

		@Override
		public Set<Type> visitExprStmt(DartExprStmt node) {
			return visitor.visitExprStmt(node, map);
		}

		@Override
		public Set<Type> visitField(DartField node) {
			return visitor.visitField(node, map);
		}

		@Override
		public Set<Type> visitFieldDefinition(DartFieldDefinition node) {
			return visitor.visitFieldDefinition(node, map);
		}

		@Override
		public Set<Type> visitForInStatement(DartForInStatement node) {
			return visitor.visitForInStatement(node, map);
		}

		@Override
		public Set<Type> visitForStatement(DartForStatement node) {
			return visitor.visitForStatement(node, map);
		}

		@Override
		public Set<Type> visitFunction(DartFunction node) {
			return visitor.visitFunction(node, map);
		}

		@Override
		public Set<Type> visitFunctionExpression(DartFunctionExpression node) {
			return visitor.visitFunctionExpression(node, map);
		}

		@Override
		public Set<Type> visitFunctionTypeAlias(DartFunctionTypeAlias node) {
			return visitor.visitFunctionTypeAlias(node, map);
		}

		@Override
		public Set<Type> visitIdentifier(DartIdentifier node) {
			return visitor.visitIdentifier(node, map);
		}

		@Override
		public Set<Type> visitIfStatement(DartIfStatement node) {
			return visitor.visitIfStatement(node, map);
		}

		@Override
		public Set<Type> visitUnit(DartUnit node) {
			return visitor.visitUnit(node, map);
		}

		@Override
		public Set<Type> visitImportDirective(DartImportDirective node) {
			return visitor.visitImportDirective(node, map);
		}

		@Override
		public Set<Type> visitInitializer(DartInitializer node) {
			return visitor.visitInitializer(node, map);
		}

		@Override
		public Set<Type> visitIntegerLiteral(DartIntegerLiteral node) {
			return visitor.visitIntegerLiteral(node, map);
		}

		@Override
		public Set<Type> visitLabel(DartLabel node) {
			return visitor.visitLabel(node, map);
		}

		@Override
		public Set<Type> visitLibraryDirective(DartLibraryDirective node) {
			return visitor.visitLibraryDirective(node, map);
		}

		@Override
		public Set<Type> visitMapLiteral(DartMapLiteral node) {
			return visitor.visitMapLiteral(node, map);
		}

		@Override
		public Set<Type> visitMapLiteralEntry(DartMapLiteralEntry node) {
			return visitor.visitMapLiteralEntry(node, map);
		}

		@Override
		public Set<Type> visitMethodDefinition(DartMethodDefinition node) {
			return visitor.visitMethodDefinition(node, map);
		}

		@Override
		public Set<Type> visitNativeDirective(DartNativeDirective node) {
			return visitor.visitNativeDirective(node, map);
		}

		@Override
		public Set<Type> visitNewExpression(DartNewExpression node) {
			return visitor.visitNewExpression(node, map);
		}

		@Override
		public Set<Type> visitNullLiteral(DartNullLiteral node) {
			return visitor.visitNullLiteral(node, map);
		}

		@Override
		public Set<Type> visitParameter(DartParameter node) {
			return visitor.visitParameter(node, map);
		}

		@Override
		public Set<Type> visitParameterizedTypeNode(
				DartParameterizedTypeNode node) {
			return visitor.visitParameterizedTypeNode(node, map);
		}

		@Override
		public Set<Type> visitParenthesizedExpression(
				DartParenthesizedExpression node) {
			return visitor.visitParenthesizedExpression(node, map);
		}

		@Override
		public Set<Type> visitPropertyAccess(DartPropertyAccess node) {
			return visitor.visitPropertyAccess(node, map);
		}

		@Override
		public Set<Type> visitTypeNode(DartTypeNode node) {
			return visitor.visitTypeNode(node, map);
		}

		@Override
		public Set<Type> visitResourceDirective(DartResourceDirective node) {
			return visitor.visitResourceDirective(node, map);
		}

		@Override
		public Set<Type> visitReturnStatement(DartReturnStatement node) {
			return visitor.visitReturnStatement(node, map);
		}

		@Override
		public Set<Type> visitSourceDirective(DartSourceDirective node) {
			return visitor.visitSourceDirective(node, map);
		}

		@Override
		public Set<Type> visitStringLiteral(DartStringLiteral node) {
			return visitor.visitStringLiteral(node, map);
		}

		@Override
		public Set<Type> visitStringInterpolation(DartStringInterpolation node) {
			return visitor.visitStringInterpolation(node, map);
		}

		@Override
		public Set<Type> visitSuperExpression(DartSuperExpression node) {
			return visitor.visitSuperExpression(node, map);
		}

		@Override
		public Set<Type> visitSwitchStatement(DartSwitchStatement node) {
			return visitor.visitSwitchStatement(node, map);
		}

		@Override
		public Set<Type> visitSyntheticErrorExpression(
				DartSyntheticErrorExpression node) {
			return visitor.visitSyntheticErrorExpression(node, map);
		}

		@Override
		public Set<Type> visitSyntheticErrorIdentifier(
				DartSyntheticErrorIdentifier node) {
			return visitor.visitSyntheticErrorIdentifier(node, map);
		}

		@Override
		public Set<Type> visitSyntheticErrorStatement(
				DartSyntheticErrorStatement node) {
			return visitor.visitSyntheticErrorStatement(node, map);
		}

		@Override
		public Set<Type> visitThisExpression(DartThisExpression node) {
			return visitor.visitThisExpression(node, map);
		}

		@Override
		public Set<Type> visitThrowStatement(DartThrowStatement node) {
			return visitor.visitThrowStatement(node, map);
		}

		@Override
		public Set<Type> visitCatchBlock(DartCatchBlock node) {
			return visitor.visitCatchBlock(node, map);
		}

		@Override
		public Set<Type> visitTryStatement(DartTryStatement node) {
			return visitor.visitTryStatement(node, map);
		}

		@Override
		public Set<Type> visitUnaryExpression(DartUnaryExpression node) {
			return visitor.visitUnaryExpression(node, map);
		}

		@Override
		public Set<Type> visitVariable(DartVariable node) {
			return visitor.visitVariable(node, map);
		}

		@Override
		public Set<Type> visitVariableStatement(DartVariableStatement node) {
			return visitor.visitVariableStatement(node, map);
		}

		@Override
		public Set<Type> visitWhileStatement(DartWhileStatement node) {
			return visitor.visitWhileStatement(node, map);
		}

		@Override
		public void visit(List<? extends DartNode> nodes) {
			visitor.visit(nodes, map);
		}

		@Override
		public Set<Type> visitNamedExpression(DartNamedExpression node) {
			return visitor.visitNamedExpression(node, map);
		}

		@Override
		public Set<Type> visitTypeExpression(DartTypeExpression node) {
			return visitor.visitTypeExpression(node, map);
		}

		@Override
		public Set<Type> visitTypeParameter(DartTypeParameter node) {
			return visitor.visitTypeParameter(node, map);
		}

		@Override
		public Set<Type> visitNativeBlock(DartNativeBlock node) {
			return visitor.visitNativeBlock(node, map);
		}

		@Override
		public Set<Type> visitRedirectConstructorInvocation(
				DartRedirectConstructorInvocation node) {
			return visitor.visitRedirectConstructorInvocation(node, map);
		}
	}
}
