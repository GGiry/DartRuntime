import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import type.CoreTypeRepository;
import type.FunctionType;
import type.Type;
import type.TypeRepository;

import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.MethodElement;
import com.google.dart.compiler.type.FunctionAliasType;

/**
 * Helper methods to do conversions between Dart compiler type/element and type flow's type.
 */
class TypeHelper {
  private final TypeRepository typeRepository;
  
  TypeHelper(TypeRepository typeRepository) {
    this.typeRepository = typeRepository;
  }
  
  public Type asType(boolean nullable, com.google.dart.compiler.type.Type type) {
    switch (type.getKind()) {
    case VOID:
      return CoreTypeRepository.VOID_TYPE;
    case DYNAMIC:
      return nullable ? CoreTypeRepository.DYNAMIC_TYPE : CoreTypeRepository.DYNAMIC_NON_NULL_TYPE;
    case VARIABLE:
      // return typeRepository.findType(nullable, (ClassElement)
      // type.getElement());
    case INTERFACE:
      return typeRepository.findType(nullable, (ClassElement) type.getElement());
    case FUNCTION:
      return asFunctionType(nullable, (com.google.dart.compiler.type.FunctionType) type);
    case FUNCTION_ALIAS:
      return asFunctionType(nullable, ((FunctionAliasType) type).getElement().getFunctionType());
    case NONE:
    default:
      throw new AssertionError("asType: " + type.getKind() + " must be implemented");
    }
  }

  private FunctionType asFunctionType(boolean nullable, com.google.dart.compiler.type.FunctionType functionType) {
    return typeRepository.findFunction(nullable, asType(true, functionType.getReturnType()), asTypeList(functionType.getParameterTypes()),
        asTypeMap(functionType.getNamedParameterTypes()), null);
  }
  
  public FunctionType asConstantFunctionType(MethodElement element) {
    com.google.dart.compiler.type.FunctionType functionType = element.getFunctionType();
    return typeRepository.findFunction(false, asType(true, functionType.getReturnType()), asTypeList(functionType.getParameterTypes()),
        asTypeMap(functionType.getNamedParameterTypes()), element);
  }

  private List<Type> asTypeList(List<com.google.dart.compiler.type.Type> types) {
    ArrayList<Type> typeList = new ArrayList<>(types.size());
    for (com.google.dart.compiler.type.Type type : types) {
      typeList.add(asType(true, type));
    }
    return typeList;
  }

  private Map<String, Type> asTypeMap(Map<String, com.google.dart.compiler.type.Type> types) {
    LinkedHashMap<String, Type> typeMap = new LinkedHashMap<>(types.size());
    for (Entry<String, com.google.dart.compiler.type.Type> entry : types.entrySet()) {
      typeMap.put(entry.getKey(), asType(false, entry.getValue()));
    }
    return typeMap;
  }

  public Type findType(boolean nullable, ClassElement classElement) {
    return typeRepository.findType(nullable, classElement);
  }
}