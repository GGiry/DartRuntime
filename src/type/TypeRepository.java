package type;

import java.util.HashMap;

import com.google.dart.compiler.resolver.ClassElement;

public class TypeRepository {
  private final/* maybenull */TypeRepository typeRepository;
  final HashMap<ClassElement, Type> map = new HashMap<>();

  public TypeRepository(/* maybenull */TypeRepository typeRepository) {
    this.typeRepository = typeRepository;
  }

  public Type findType(boolean nullable, ClassElement element) {
    if (typeRepository != null) {
      Type type = typeRepository.findType(nullable, element);
      if (type != null) {
        return (nullable) ? type : type.asNonNull();
      }
    }
    InterfaceType  nullableType = createInterfaceType(element);
    return (nullable)? nullableType: nullableType.asNonNull();
  }
  
  InterfaceType createInterfaceType(ClassElement element) {
    InterfaceType nullableType = new InterfaceType(true, this, element);
    InterfaceType nonNullType = new InterfaceType(false, this, element);
    nullableType.postInitDualType(nonNullType);
    nonNullType.postInitDualType(nullableType);
    map.put(element, nullableType);
    return nullableType;
  }

  public Type findFunction(com.google.dart.compiler.type.FunctionType type) {
    if (typeRepository != null) {
      Type functionType = typeRepository.findFunction(type);
      if (type != null) {
        return functionType;
      }
    }
    
    System.err.println(type.getElement().getName());
    
    Type functionType = new FunctionType(type.getReturnType(), type.getParameterTypes(), type.getNamedParameterTypes());
    map.put(type.getElement(), functionType);
    return functionType;
  }
}
