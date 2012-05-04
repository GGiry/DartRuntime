import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
import com.google.dart.compiler.type.Void;

public class FlowTypingPhase implements DartCompilationPhase {
	@Override
	public DartUnit exec(DartUnit unit, DartCompilerContext context,
			CoreTypeProvider typeProvider) {
		unit.accept(new FTVisitor());
		return null;
	}

	class FTVisitor extends ASTVisitor<Void> {
		ASTVisitor2<Void, HashMap<K, V>> visitor;
		private HashMap<K, Set<Type>> map;

		public Void visitNode(DartNode node) {
			return visitor.visitNode(node, map);
		}

		public Void visitDirective(DartDirective node) {
			return visitor.visitDirective(node, map);
		}

		public Void visitInvocation(DartInvocation node) {
			return visitor.visitInvocation(node, map);
		}

		public Void visitExpression(DartExpression node)
		{
			return visitor.visitExpression(node, map);
		}

		public Void visitStatement(DartStatement node) {
			return visitor.visitStatement(node, map);
		}

		public Void visitLiteral(DartLiteral node) {
			return visitor.visitLiteral(node, map);
		}

		public Void visitGotoStatement(DartGotoStatement node) {
			return visitor.visitGotoStatement(node, map);
		}

		public Void visitSwitchMember(DartSwitchMember node) {
			return visitor.visitSwitchMember(node, map);
		}

		public Void visitDeclaration(DartDeclaration<?> node) {
			return visitor.visitDeclaration(node, map);
		}

		public Void visitClassMember(DartClassMember<?> node) {
			return visitor.visitClassMember(node, map);
		}

		public Void visitComment(DartComment node) {
			return visitor.visitComment(node, map);
		}

		public Void visitArrayAccess(DartArrayAccess node) {
			return visitor.visitArrayAccess(node, map);
		}

		public Void visitArrayLiteral(DartArrayLiteral node) {
			return visitor.visitArrayLiteral(node, map);
		}

		public Void visitAssertion(DartAssertion node) {
			return visitor.visitAssertion(node, map);
		}

		public Void visitBinaryExpression(DartBinaryExpression node) {
			return visitor.visitBinaryExpression(node, map);
		}

		public Void visitBlock(DartBlock node) {
			return visitor.visitBlock(node, map);
		}

		public Void visitBooleanLiteral(DartBooleanLiteral node) {
			return visitor.visitBooleanLiteral(node, map);
		}

		public Void visitBreakStatement(DartBreakStatement node) {
			return visitor.visitBreakStatement(node, map);
		}

		public Void visitFunctionObjectInvocation(
				DartFunctionObjectInvocation node, map) {
			return visitor.visitFunctionObjectInvocation(node);
		}

		public Void visitMethodInvocation(DartMethodInvocation node) {
			return visitor.visitMethodInvocation(node, map);
		}

		public Void visitUnqualifiedInvocation(DartUnqualifiedInvocation node) {
			return visitor.visitUnqualifiedInvocation(node, map);
		}

		public Void visitSuperConstructorInvocation(
				DartSuperConstructorInvocation node, map) {
			return visitor.visitSuperConstructorInvocation(node);
		}

		public Void visitCase(DartCase node) {
			return visitor.visitCase(node, map);
		}

		public Void visitClass(DartClass node) {
			return visitor.visitClass(node, map);
		}

		public Void visitConditional(DartConditional node) {
			return visitor.visitConditional(node, map);
		}

		public Void visitContinueStatement(DartContinueStatement node) {
			return visitor.visitContinueStatement(node, map);
		}

		public Void visitDefault(DartDefault node) {
			return visitor.visitDefault(node, map);
		}

		public Void visitDoubleLiteral(DartDoubleLiteral node) {
			return visitor.visitDoubleLiteral(node, map);
		}

		public Void visitDoWhileStatement(DartDoWhileStatement node) {
			return visitor.visitDoWhileStatement(node, map);
		}

		public Void visitEmptyStatement(DartEmptyStatement node) {
			return visitor.visitEmptyStatement(node, map);
		}

		public Void visitExprStmt(DartExprStmt node) {
			return visitor.visitExprStmt(node, map);
		}

		public Void visitField(DartField node) {
			return visitor.visitField(node, map);
		}

		public Void visitFieldDefinition(DartFieldDefinition node) {
			return visitor.visitFieldDefinition(node, map);
		}

		public Void visitForInStatement(DartForInStatement node) {
			return visitor.visitForInStatement(node, map);
		}

		public Void visitForStatement(DartForStatement node) {
			return visitor.visitForStatement(node, map);
		}

		public Void visitFunction(DartFunction node) {
			return visitor.visitFunction(node, map);
		}

		public Void visitFunctionExpression(DartFunctionExpression node) {
			return visitor.visitFunctionExpression(node, map);
		}

		public Void visitFunctionTypeAlias(DartFunctionTypeAlias node) {
			return visitor.visitFunctionTypeAlias(node, map);
		}

		public Void visitIdentifier(DartIdentifier node) {
			return visitor.visitIdentifier(node, map);
		}

		public Void visitIfStatement(DartIfStatement node) {
			return visitor.visitIfStatement(node, map);
		}
	}
}
