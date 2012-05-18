package type;

import static type.CoreTypeRepository.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    return ((NullableType) type1).merge((NullableType) type2);
  }

  static boolean isOwnerTypeAssignable(OwnerType type1, OwnerType type2) {
    if (type1.equals(type2)) {
      return true;
    }
    
    InterfaceType superType2 = type2.getSuperType();
    if (superType2 != null && isOwnerTypeAssignable(type1, superType2)) {
      return true;
    }
    
    for(InterfaceType interfaze2: type2.getInterfaces()) {
      if (isOwnerTypeAssignable(type1, interfaze2)) {
        return true;
      }
    }
    return false;
  }

  public static Type getReturnType(Type type) {
    return type.map(RETURN_TYPE_MAPPER);
    
  }// where
  private static final TypeMapper RETURN_TYPE_MAPPER = new TypeMapper() {
    @Override
    public Type transform(Type type) {
      return type.accept(RETURN_TYPE_VISITOR, null);
    }
  };
  // and
  static final TypeVisitor<Type, Void> RETURN_TYPE_VISITOR = 
      new TypeVisitor<Type, Void>() {
        @Override
        public Type visitDynamicType(DynamicType type, Void unused) {
          return type;
        }
        
        @Override
        public Type visitFunctionType(FunctionType type, Void parameter) {
          return type.getReturnType();
        }
      };
}
