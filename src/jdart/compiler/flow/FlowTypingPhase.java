package jdart.compiler.flow;

import java.util.List;

import jdart.compiler.type.CoreTypeRepository;
import jdart.compiler.type.DynamicType;
import jdart.compiler.type.OwnerType;
import jdart.compiler.type.Type;
import jdart.compiler.type.TypeRepository;
import jdart.compiler.type.TypeVisitor;

import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.resolver.CoreTypeProvider;
import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.MethodElement;

public class FlowTypingPhase implements DartCompilationPhase {
  @Override
  public DartUnit exec(DartUnit unit, DartCompilerContext context, CoreTypeProvider coreTypeProvider) {
    // initialize core type repository
    CoreTypeRepository coreTypeRepository = CoreTypeRepository.initCoreTypeRepository(coreTypeProvider);

    TypeRepository typeRepository = new TypeRepository(coreTypeRepository);
    TypeHelper typeHelper = new TypeHelper(typeRepository);
    IntraProcedualMethodCallResolver methodCallResolver = new IntraProcedualMethodCallResolver(typeHelper);
    new DefinitionVisitor(typeHelper, methodCallResolver).typeFlow(unit);
    return unit;
  }

  static class IntraProcedualMethodCallResolver implements MethodCallResolver {
    final TypeHelper typeHelper;

    IntraProcedualMethodCallResolver(TypeHelper typeHelper) {
      this.typeHelper = typeHelper;
    }

    @Override
    public Type methodCall(final String methodName, Type receiverType, List<Type> argumentType, final Type expectedType, boolean virtual) {
      Type returnType = receiverType.accept(new TypeVisitor<Type, Void>() {
        @Override
        protected Type visitOwnerType(OwnerType type, Void parameter) {
          Element member = type.lookupMember(methodName);
          if (!(member instanceof MethodElement)) {
            throw new AssertionError();
          }
          MethodElement methodElement = (MethodElement) member;
          return typeHelper.asType(true, methodElement.getReturnType());
        }
        @Override
        public Type visitDynamicType(DynamicType type, Void parameter) {
          return expectedType;
        }
      }, null);
      return (returnType instanceof DynamicType)? expectedType: returnType;
    }

    @Override
    public Type functionCall(MethodElement nodeElement, List<Type> argumentTypes, Type expectedType) {
      return typeHelper.asType(true, nodeElement.getReturnType());
    }
  }
}
