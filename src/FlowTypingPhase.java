import java.util.HashMap;
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
import com.google.dart.compiler.ast.DartInvocation;
import com.google.dart.compiler.ast.DartLiteral;
import com.google.dart.compiler.ast.DartMethodInvocation;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartStatement;
import com.google.dart.compiler.ast.DartSuperConstructorInvocation;
import com.google.dart.compiler.ast.DartSwitchMember;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.ast.DartUnqualifiedInvocation;
import com.google.dart.compiler.resolver.CoreTypeProvider;

public class FlowTypingPhase implements DartCompilationPhase {
	@Override
	public DartUnit exec(DartUnit unit, DartCompilerContext context,
			CoreTypeProvider typeProvider) {
		unit.accept(new FTVisitor());
		return null;
	}

	class FTVisitor extends ASTVisitor<Set<Type>> {
		private ASTVisitor2<Set<Type>, HashMap<Variable, Set<Type>>> visitor;
		private HashMap<Variable, Set<Type>> map;

		public Set<Type> visitNode(DartNode node) {
			return visitor.visitNode(node, map);
		}

		public Set<Type> visitDirective(DartDirective node) {
			return visitor.visitDirective(node, map);
		}

		public Set<Type> visitInvocation(DartInvocation node) {
			return visitor.visitInvocation(node, map);
		}

		public Set<Type> visitExpression(DartExpression node)
		{
			return visitor.visitExpression(node, map);
		}

		public Set<Type> visitStatement(DartStatement node) {
			return visitor.visitStatement(node, map);
		}

		public Set<Type> visitLiteral(DartLiteral node) {
			return visitor.visitLiteral(node, map);
		}

		public Set<Type> visitGotoStatement(DartGotoStatement node) {
			return visitor.visitGotoStatement(node, map);
		}

		public Set<Type> visitSwitchMember(DartSwitchMember node) {
			return visitor.visitSwitchMember(node, map);
		}

		public Set<Type> visitDeclaration(DartDeclaration<?> node) {
			return visitor.visitDeclaration(node, map);
		}

		public Set<Type> visitClassMember(DartClassMember<?> node) {
			return visitor.visitClassMember(node, map);
		}

		public Set<Type> visitComment(DartComment node) {
			return visitor.visitComment(node, map);
		}

		public Set<Type> visitArrayAccess(DartArrayAccess node) {
			return visitor.visitArrayAccess(node, map);
		}

		public Set<Type> visitArrayLiteral(DartArrayLiteral node) {
			return visitor.visitArrayLiteral(node, map);
		}

		public Set<Type> visitAssertion(DartAssertion node) {
			return visitor.visitAssertion(node, map);
		}

		public Set<Type> visitBinaryExpression(DartBinaryExpression node) {
			return visitor.visitBinaryExpression(node, map);
		}

		public Set<Type> visitBlock(DartBlock node) {
			return visitor.visitBlock(node, map);
		}

		public Set<Type> visitBooleanLiteral(DartBooleanLiteral node) {
			return visitor.visitBooleanLiteral(node, map);
		}

		public Set<Type> visitBreakStatement(DartBreakStatement node) {
			return visitor.visitBreakStatement(node, map);
		}

		public Set<Type> visitFunctionObjectInvocation(
				DartFunctionObjectInvocation node) {
			return visitor.visitFunctionObjectInvocation(node, map);
		}

		public Set<Type> visitMethodInvocation(DartMethodInvocation node) {
			return visitor.visitMethodInvocation(node, map);
		}

		public Set<Type> visitUnqualifiedInvocation(DartUnqualifiedInvocation node) {
			return visitor.visitUnqualifiedInvocation(node, map);
		}

		public Set<Type> visitSuperConstructorInvocation(
				DartSuperConstructorInvocation node) {
			return visitor.visitSuperConstructorInvocation(node, map);
		}

		public Set<Type> visitCase(DartCase node) {
			return visitor.visitCase(node, map);
		}

		public Set<Type> visitClass(DartClass node) {
			return visitor.visitClass(node, map);
		}

		public Set<Type> visitConditional(DartConditional node) {
			return visitor.visitConditional(node, map);
		}

		public Set<Type> visitContinueStatement(DartContinueStatement node) {
			return visitor.visitContinueStatement(node, map);
		}

		public Set<Type> visitDefault(DartDefault node) {
			return visitor.visitDefault(node, map);
		}

		public Set<Type> visitDoubleLiteral(DartDoubleLiteral node) {
			return visitor.visitDoubleLiteral(node, map);
		}

		public Set<Type> visitDoWhileStatement(DartDoWhileStatement node) {
			return visitor.visitDoWhileStatement(node, map);
		}

		public Set<Type> visitEmptyStatement(DartEmptyStatement node) {
			return visitor.visitEmptyStatement(node, map);
		}

		public Set<Type> visitExprStmt(DartExprStmt node) {
			return visitor.visitExprStmt(node, map);
		}

		public Set<Type> visitField(DartField node) {
			return visitor.visitField(node, map);
		}

		public Set<Type> visitFieldDefinition(DartFieldDefinition node) {
			return visitor.visitFieldDefinition(node, map);
		}

		public Set<Type> visitForInStatement(DartForInStatement node) {
			return visitor.visitForInStatement(node, map);
		}

		public Set<Type> visitForStatement(DartForStatement node) {
			return visitor.visitForStatement(node, map);
		}

		public Set<Type> visitFunction(DartFunction node) {
			return visitor.visitFunction(node, map);
		}

		public Set<Type> visitFunctionExpression(DartFunctionExpression node) {
			return visitor.visitFunctionExpression(node, map);
		}

		public Set<Type> visitFunctionTypeAlias(DartFunctionTypeAlias node) {
			return visitor.visitFunctionTypeAlias(node, map);
		}

		public Set<Type> visitIdentifier(DartIdentifier node) {
			return visitor.visitIdentifier(node, map);
		}

		public Set<Type> visitIfStatement(DartIfStatement node) {
			return visitor.visitIfStatement(node, map);
		}
	}
}
