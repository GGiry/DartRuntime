package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;
import static jdart.compiler.type.CoreTypeRepository.DYNAMIC_TYPE;
import static jdart.compiler.type.CoreTypeRepository.INT32_TYPE;
import static jdart.compiler.type.CoreTypeRepository.INT_NON_NULL_TYPE;
import static jdart.compiler.type.CoreTypeRepository.NEGATIVE_INT32_TYPE;
import static jdart.compiler.type.CoreTypeRepository.NULL_TYPE;
import static jdart.compiler.type.CoreTypeRepository.POSITIVE_INT32_TYPE;

import java.util.Objects;

public class Types {
  public static Type union(Type type1, Type type2) {
    Objects.requireNonNull(type1);
    Objects.requireNonNull(type2);
    if (type1 == VOID_TYPE || type2 == VOID_TYPE) {
      throw new IllegalArgumentException("void can not be a component of an union");
    }

    if (type1 == DYNAMIC_TYPE || type2 == DYNAMIC_TYPE) {
      return DYNAMIC_TYPE;
    }
    if (type1 == DYNAMIC_NON_NULL_TYPE) {
      return (type2.isNullable()) ? DYNAMIC_TYPE : DYNAMIC_NON_NULL_TYPE;
    }
    if (type2 == DYNAMIC_NON_NULL_TYPE) {
      return (type1.isNullable()) ? DYNAMIC_TYPE : DYNAMIC_NON_NULL_TYPE;
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

    for (InterfaceType interfaze2 : type2.getInterfaces()) {
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
  static final TypeVisitor<Type, Void> RETURN_TYPE_VISITOR = new TypeVisitor<Type, Void>() {
    @Override
    public Type visitDynamicType(DynamicType type, Void unused) {
      return type;
    }

    @Override
    public Type visitFunctionType(FunctionType type, Void parameter) {
      return type.getReturnType();
    }
  };

  
  public static Type widening(Type type) {
    return type.accept(WIDENING_VISITOR, null);
  } // where
  private static final TypeVisitor<Type, Void> WIDENING_VISITOR = new TypeVisitor<Type, Void>() {
    @Override
    public Type visitIntType(IntType type, Void unused) {
      boolean nullable = type.isNullable();
      if (type.isIncludeIn(POSITIVE_INT32_TYPE)) {
        return POSITIVE_INT32_TYPE.asNullable(nullable);
      }
      if (type.isIncludeIn(NEGATIVE_INT32_TYPE)) {
        return NEGATIVE_INT32_TYPE.asNullable(nullable);
      }
      if (type.isIncludeIn(INT32_TYPE)) {
        return INT32_TYPE;
      }
      return INT_NON_NULL_TYPE.asNullable(nullable);
    }
    
    @Override
    public Type visitDoubleType(DoubleType type, Void unused) {
      return DOUBLE_NON_NULL_TYPE.asNullable(type.isNullable());
    }
    
    @Override
    public Type visitUnionType(UnionType type, Void unused) {
      return type.map(new TypeMapper() {
        @Override
        public Type transform(Type type) {
          return widening(type);
        }
      });
    }
  };
}
