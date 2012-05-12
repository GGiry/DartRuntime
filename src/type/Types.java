package type;

import static type.CoreTypeRepository.*;

import java.util.Objects;

public class Types {
  public static Type union(Type type1, Type type2) {
    Objects.requireNonNull(type1);
    Objects.requireNonNull(type2);
    
    if (type1 == DYNAMIC_TYPE || type2 == DYNAMIC_TYPE) {
      return DYNAMIC_TYPE;
    }
    if (type1 == DYNAMIC_NON_NULL_TYPE) {
      return (type2.isNullable())? DYNAMIC_TYPE: DYNAMIC_NON_NULL_TYPE;
    }
    if (type2 == DYNAMIC_NON_NULL_TYPE) {
      return (type1.isNullable())? DYNAMIC_TYPE: DYNAMIC_NON_NULL_TYPE;
    }
    
    if (type1 == NULL_TYPE) {
      return type2.asNullable();
    }
    if (type2 == NULL_TYPE) {
      return type1.asNullable();
    }
    return ((AbstractType) type1).merge((AbstractType) type2);
  }

  static boolean isAssignable(OwnerType type1, OwnerType type2) {
    if (type1.equals(type2)) {
      return true;
    }
    
    InterfaceType superType2 = type2.getSuperType();
    if (superType2 != null && isAssignable(type1, superType2)) {
      return true;
    }
    
    for(InterfaceType interfaze2: type2.getInterfaces()) {
      if (isAssignable(type1, interfaze2)) {
        return true;
      }
    }
    return false;
  }
}
