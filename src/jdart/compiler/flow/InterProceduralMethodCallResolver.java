package jdart.compiler.flow;

import static jdart.compiler.type.CoreTypeRepository.DYNAMIC_TYPE;
import static jdart.compiler.type.CoreTypeRepository.VOID_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jdart.compiler.cha.ClassHierarchyAnalysisPhase;
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
  private final HashMap<DartMethodDefinition, Profiles> methodMap = new HashMap<>();
  
  public static class Profiles {
    final LinkedHashMap<List<Type>, Type> profileMap = new LinkedHashMap<>();

    public Map<List<Type>, Type> getSignatureMap() {
      return profileMap;
    }
    
    @Override
    public String toString() {
      return profileMap.toString();
    }
    
    public Type lookupForACompatibleSignature(List<Type> argumentTypes) {
      Type returnType = profileMap.get(argumentTypes);
      if (returnType != null) {
        return returnType;
      }
      
      for(Entry<List<Type>, Type> entry: profileMap.entrySet()) {
       if (isCompatible(entry.getKey(), argumentTypes)) {
          return entry.getValue();
        }
      }
      return null;
    }
  }
  
  public InterProceduralMethodCallResolver(TypeHelper typeHelper) {
    this.typeHelper = typeHelper;
  }
  
  public Map<DartMethodDefinition, Profiles> getMethodMap() {
    return methodMap;
  }
  
  static boolean isCompatible(Type parameterType, Type argumentType) {
    return argumentType.isIncludeIn(parameterType);
  }
  
  static boolean isCompatible(List<Type> parameterTypes, List<Type> argumentTypes) {
    assert parameterTypes.size() == argumentTypes.size();
    
    for(int i=0; i<parameterTypes.size(); i++) {
      if (!isCompatible(parameterTypes.get(i), argumentTypes.get(i))) {
        return false;
      }
      
      /*if (!parameterType.equals(argumentType)) {
        return false;
      }*/
    }
    return true;
  }

  private Type actualCall(DartMethodDefinition node, /*maybenull*/OwnerType receiverType, List<Type> argumentTypes, Type expectedType) {
    System.out.println("actual call "+receiverType+'.'+node.getName()+argumentTypes);
    
    Profiles profiles = methodMap.get(node);
    if (profiles == null) {
      // try to not widen argument if the method is called once
      methodMap.put(node, profiles = new Profiles());  
    } else {
      Type signatureReturnType = profiles.lookupForACompatibleSignature(argumentTypes);
      if (signatureReturnType != null) {
        return signatureReturnType;
      }
      
      // already a signature, try to generalize the signature by widening it
      // FIXME, you try to do an union of type if parameters are different
      List<Type> windenedArgumentTypes = new ArrayList<>(argumentTypes.size());
      for(Type argumentType: argumentTypes) {
        windenedArgumentTypes.add(Types.widening(argumentType));
      }
      
      signatureReturnType = profiles.lookupForACompatibleSignature(windenedArgumentTypes);
      if (signatureReturnType != null) {
        return signatureReturnType;
      }
      
      argumentTypes = windenedArgumentTypes;
    }
    
    Type returnType = doActualCall(node, receiverType, argumentTypes, profiles);
    
    // try to remove signatures too specific
    /* FIXME, don't work because with recursive calls, we try to compare with something
       that is a temporary signature.
       
    if (signatures.signatureMap.size() > 1) {
      signatures.signatureMap.remove(argumentTypes);
      System.out.println("map before cleaning " +signatures.signatureMap);
      
      for(Iterator<Entry<List<Type>,Type>> it = signatures.signatureMap.entrySet().iterator(); it.hasNext();) {
        Entry<List<Type>, Type> entry = it.next();
        
        System.out.println("  check rtypes "+returnType+" "+entry.getValue());
        System.out.println("  check argtypes "+argumentTypes+" "+entry.getKey());
        
        if (isCompatible(returnType, entry.getValue()) && isCompatible(argumentTypes, entry.getKey())) {
          System.out.println("--> clean up "+entry);
          it.remove();   
        }
      }
      
      System.out.println("clean map " +signatures.signatureMap);
    }*/
    
    profiles.profileMap.put(argumentTypes, returnType);
    
    if (returnType instanceof DynamicType) {
      return expectedType;
    }
    return returnType;
  }
  
  private Type doActualCall(DartMethodDefinition node, /*maybenull*/OwnerType receiverType, List<Type> argumentTypes, Profiles profiles) {
    //System.out.println("call "+node.getName()+" receiver "+receiverType+" "+argumentTypes);
    
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

    // register temporary signature with declared return type for recursive function 
    profiles.profileMap.put(argumentTypes, returnType);
    
    FTVisitor flowTypeVisitor = new FTVisitor(typeHelper, this);
    FlowEnv flowEnv = new FlowEnv(new FlowEnv(thisType), returnType, VOID_TYPE, false);
    List<DartParameter> parameters = function.getParameters();
    for(int i=0; i<parameters.size(); i++) {
      DartParameter parameter = parameters.get(i);
      flowEnv.register(parameter.getElement(), argumentTypes.get(i));
    }

    DartBlock body = function.getBody();
    if (body != null) {
      flowTypeVisitor.liveness(body, flowEnv);
      return flowTypeVisitor.getInferredReturnType(returnType);
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
