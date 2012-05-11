package type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.dart.compiler.resolver.ClassElement;

public class TypeRepository {
  private final/* maybenull */TypeRepository typeRepository;
  final HashMap<ClassElement, Type> map = new HashMap<>();
  private final HashMap<FunctionType, FunctionType> functionMap = new HashMap<>();

  public TypeRepository(/* maybenull */TypeRepository typeRepository) {
    this.typeRepository = typeRepository;
  }

  public Type findType(boolean nullable, ClassElement element) {
    if (typeRepository != null) {
      return typeRepository.findType(nullable, element);
    }
    
    Type type = map.get(element);
    if (type != null) {
      return (nullable) ? type : type.asNonNull();
    }
    
    InterfaceType nullableType = createInterfaceType(element);
    return (nullable) ? nullableType : nullableType.asNonNull();
  }

  InterfaceType createInterfaceType(ClassElement element) {
    InterfaceType nullableType = new InterfaceType(true, this, element);
    InterfaceType nonNullType = new InterfaceType(false, this, element);
    nullableType.postInitDualType(nonNullType);
    nonNullType.postInitDualType(nullableType);
    map.put(element, nullableType);
    return nullableType;
  }

  public FunctionType findFunction(boolean nullable, Type returnType, List<Type> parameterTypes, Map<String , Type> namedParameterTypes) {
    FunctionType key = new FunctionType(true, returnType, parameterTypes, namedParameterTypes);
    return findFunction(nullable, key);
  }
  
  private FunctionType findFunction(boolean nullable, FunctionType key) {
    if (typeRepository != null) {
      FunctionType functionType = typeRepository.findFunction(nullable, key);
      if (functionType != null) {
        return functionType;
      }
    }

    FunctionType functionType = functionMap.get(key);
    if (functionType != null) {
      return (nullable)? functionType: functionType.asNonNull();
    }
    
    functionMap.put(key, key);
    FunctionType dualType = new FunctionType(false, key.getReturnType(), key.getParameterTypes(), key.getNamedParameterTypes());
    key.postInitDualType(dualType);
    dualType.postInitDualType(key);
    return key;
  }
}
