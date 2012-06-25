package jdart.compiler.flow;

import static jdart.compiler.type.CoreTypeRepository.DYNAMIC_TYPE;
import static jdart.compiler.type.CoreTypeRepository.VOID_TYPE;
import jdart.compiler.type.FunctionType;
import jdart.compiler.type.Type;
import jdart.compiler.visitor.ASTVisitor2;

import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartClass;
import com.google.dart.compiler.ast.DartFieldDefinition;
import com.google.dart.compiler.ast.DartFunction;
import com.google.dart.compiler.ast.DartFunctionTypeAlias;
import com.google.dart.compiler.ast.DartMethodDefinition;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartParameter;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.ast.Modifiers;
import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.MethodElement;

class DefinitionVisitor extends ASTVisitor2<Void, FlowEnv> {
  private final TypeHelper typeHelper;
  private final MethodCallResolver methodCallResolver;

  DefinitionVisitor(TypeHelper typeHelper, MethodCallResolver methodCallResolver) {
    this.typeHelper = typeHelper;
    this.methodCallResolver = methodCallResolver;
  }

  // entry point
  public void typeFlow(DartUnit unit) {
    accept(unit, null);
  }

  @Override
  public Void visitUnit(DartUnit node, FlowEnv unused) {
    // TODO Temporary display.
    System.out.println("Unit: " + node.getSourceName());
    for (DartNode child : node.getTopLevelNodes()) {
      accept(child, null);
    }
    return null;
  }

  @Override
  public Void visitClass(DartClass node, FlowEnv unused) {
    for (DartNode member : node.getMembers()) {
      if (member != null) {
        accept(member, null);
      }
    }
    return null;
  }

  @Override
  public Void visitFieldDefinition(DartFieldDefinition node, FlowEnv unused) {
    // do nothing, at least for now,
    // field as already been resolved by Dart compiler resolver
    return null;
  }

  @Override
  public Void visitMethodDefinition(DartMethodDefinition node, FlowEnv unused) {
    DartFunction function = node.getFunction();

    // We should allow to propagate the type of 'this' in the flow env
    // to be more precise, but currently we don't specialize method call,
    // but only function call

    Type thisType = null;
    Modifiers modifiers = node.getModifiers();
    MethodElement element = node.getElement();
    if (!modifiers.isStatic() && !modifiers.isFactory()) {
      if (element.getEnclosingElement() instanceof ClassElement) {
        thisType = typeHelper.findType(false, (ClassElement) element.getEnclosingElement());
      } else {
        thisType = DYNAMIC_TYPE;
      }
    }

    // extract return type info from function type
    Type returnType = ((FunctionType) typeHelper.asType(false, element.getType())).getReturnType();

    FTVisitor flowTypeVisitor = new FTVisitor(typeHelper, methodCallResolver);
    FlowEnv flowEnv = new FlowEnv(new FlowEnv(thisType), returnType, VOID_TYPE, false);
    for (DartParameter parameter : function.getParameters()) {
      Type parameterType = flowTypeVisitor.typeFlow(parameter, null);
      flowEnv.register(parameter.getElement(), parameterType);
    }

    DartBlock body = function.getBody();
    if (body != null) {
      flowTypeVisitor.liveness(body, flowEnv);
    }

    // TODO test display, to remove.
    System.out.println(flowEnv);

    return null;
  }
  
  @Override
  public Void visitFunctionTypeAlias(DartFunctionTypeAlias node, FlowEnv parameter) {
    // TODO nothing to do ? 
    return null;
  }
}