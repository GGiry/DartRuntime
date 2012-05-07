import java.util.HashMap;

import type.CoreTypeRepository;
import type.Type;
import type.TypeRepository;
import visitor.ASTVisitor2;

import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.resolver.CoreTypeProvider;

public class FlowTypingPhase implements DartCompilationPhase {
	@Override
	public DartUnit exec(DartUnit unit, DartCompilerContext context,
			CoreTypeProvider coreTypeProvider) {
		
		System.err.println(coreTypeProvider);
		
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
		public Type visitBinaryExpression(DartBinaryExpression node,
				FlowEnv flowEnv) {
			System.out.println("DartBinaryExp: " + node);
			switch (node.getOperator()) {
			case ASSIGN:
			case ASSIGN_ADD:
			case ASSIGN_SUB:
			case ASSIGN_MUL:
			case ASSIGN_DIV:
				// TODO handle other assign
				
				System.out.println("binary expression assign");
				
				break;
			}

			return null;
		}
		
		@Override
		public Type visitUnit(DartUnit node, FlowEnv parameter) {
			System.out.println(node);
			System.out.println("return: " + typeMap.get(node));
			return typeMap.get(node);
		}
	}
}
