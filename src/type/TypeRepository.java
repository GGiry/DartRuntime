package type;

import java.util.HashMap;

import com.google.dart.compiler.resolver.ClassElement;

public class TypeRepository {
  private final /*maybenull*/TypeRepository typeRepository;
  final HashMap<ClassElement, Type> map = new HashMap<>();
  
  public TypeRepository(/*maybenull*/TypeRepository typeRepository) {
    this.typeRepository = typeRepository;
  }
  
  public Type findType(boolean nullable, ClassElement element) {
    if (typeRepository != null) {
      Type type = typeRepository.findType(nullable, element);
      if (type != null) {
        return (nullable)? type: type.asNonNull();
      }
    }
    InterfaceType nullableType = new InterfaceType(true, this, element);
    InterfaceType nonNullType = new InterfaceType(false, this, element);
    nullableType.postInitDualType(nonNullType);
    nonNullType.postInitDualType(nullableType);
    map.put(element, nullableType);
    return nullableType;
  }
}
