package jdart.compiler.phase;

import static jdart.compiler.type.CoreTypeRepository.DYNAMIC_TYPE;
import static jdart.compiler.type.CoreTypeRepository.VOID_TYPE;

import java.util.HashMap;
import java.util.List;

import jdart.compiler.phase.FlowTypingPhase.FTVisitor;
import jdart.compiler.type.DynamicType;
import jdart.compiler.type.FunctionType;
import jdart.compiler.type.OwnerType;
import jdart.compiler.type.Type;
import jdart.compiler.type.TypeVisitor;
import jdart.compiler.type.Types;

import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartFunction;
import com.google.dart.compiler.ast.DartMethodDefinition;
import com.google.dart.compiler.ast.DartParameter;
import com.google.dart.compiler.ast.Modifiers;
import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.MethodElement;
import com.google.dart.compiler.resolver.MethodNodeElement;

public class InterProceduralMethodCallResolver implements MethodCallResolver {
  final TypeHelper typeHelper;
  private final HashMap<MethodNodeElement, Signatures> methodMap = new HashMap<>();
  
  static class Signatures {
    private final HashMap<List<Type>, FunctionType> signatureMap =
        new HashMap<>();
  }
  
  public InterProceduralMethodCallResolver(TypeHelper typeHelper) {
    this.typeHelper = typeHelper;
  }

  //FIXME cache value
  private Type actualCall(DartMethodDefinition node, OwnerType receiverType, List<Type> argumentTypes, Type expectedType) {
    System.out.println("call "+node.getName()+" receiver "+receiverType+" "+argumentTypes);
    
    DartFunction function = node.getFunction();
    if (function == null) {
      // native function use declared return type
      return typeHelper.asType(true, node.getType());
    }
    
    // We should allow to propagate the type of 'this' in the flow env
    // to be more precise, but currently we don't specialize method call,
    // but only function call
    
    Type thisType = null;
    Modifiers modifiers = node.getModifiers();
    MethodElement element = node.getElement();
    if (!modifiers.isStatic() && !modifiers.isFactory()) {
      if (element.getEnclosingElement() instanceof ClassElement) {
        thisType = receiverType;
      } else {
        thisType = DYNAMIC_TYPE;  //FIXME, is it really necessary ??
      }
    }

    // extract return type info from function type
    Type returnType = ((FunctionType) typeHelper.asType(false, element.getType())).getReturnType();

    FTVisitor flowTypeVisitor = new FTVisitor(typeHelper, this);
    FlowEnv flowEnv = new FlowEnv(new FlowEnv(thisType), returnType, VOID_TYPE, false);
    List<DartParameter> parameters = function.getParameters();
    for(int i=0; i<parameters.size(); i++) {
      DartParameter parameter = parameters.get(i);
      flowEnv.register(parameter.getElement(), argumentTypes.get(i));
    }

    DartBlock body = function.getBody();
    if (body != null) {
      flowTypeVisitor.typeFlow(body, flowEnv);
      
      // DEBUG
      System.out.println("flow env"+flowEnv);
      
      Type inferredReturnType = flowTypeVisitor.getInferredReturnType(returnType);
      
      System.out.println("inferred return type "+inferredReturnType);
      return inferredReturnType;
    }
    
    return returnType;
  }
  
  Type directCall(MethodNodeElement element, OwnerType receiverType, List<Type> argumentType, Type expectedType, boolean virtual) {
    if (!virtual) {
      return actualCall((DartMethodDefinition)element.getNode(), receiverType, argumentType, expectedType);
    }
    
    ClassElement classElement = (ClassElement)element.getEnclosingElement();
    List<DartMethodDefinition> overridingMethods = ClassHierarchyAnalysisPhase.getInstance().getOverridingMethods(classElement, element.getName());
    Type resultType = null;
    for(DartMethodDefinition methodDefinition: overridingMethods) {
      OwnerType ownerType = (OwnerType)typeHelper.findType(false, classElement);
      Type returnType = actualCall(methodDefinition, ownerType, argumentType, expectedType);
      if (resultType == null) {
        resultType = returnType;
      } else {
        resultType = Types.union(resultType, returnType);
      }
    }
    
    assert resultType != null;
    return resultType;
  }
  
  

  @Override
  public Type methodCall(final String methodName, final Type receiverType, final List<Type> argumentType, final Type expectedType, final boolean virtual) {
    Type returnType = receiverType.accept(new TypeVisitor<Type, Void>() {
      @Override
      protected Type visitOwnerType(OwnerType type, Void parameter) {
        Element member = type.lookupMember(methodName);
        return directCall((MethodNodeElement)member, type, argumentType, expectedType, virtual);
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
    MethodNodeElement methodNodeElement = (MethodNodeElement)nodeElement;
    return actualCall((DartMethodDefinition)methodNodeElement.getNode(), null, argumentTypes, expectedType);
  }
}
