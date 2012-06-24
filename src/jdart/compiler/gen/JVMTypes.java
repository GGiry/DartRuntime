package jdart.compiler.gen;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Type;

import jdart.compiler.type.ArrayType;
import jdart.compiler.type.BoolType;
import jdart.compiler.type.CoreTypeRepository;
import jdart.compiler.type.DoubleType;
import jdart.compiler.type.DynamicType;
import jdart.compiler.type.FunctionType;
import jdart.compiler.type.IntType;
import jdart.compiler.type.InterfaceType;
import jdart.compiler.type.NullType;
import jdart.compiler.type.TypeVisitor;
import jdart.compiler.type.UnionType;
import jdart.compiler.type.VoidType;
import jdart.runtime.BigInt;

import static org.objectweb.asm.Type.*;

class JVMTypes {
  static final Type OBJECT_TYPE = Type.getType(Object.class);
  static final Type BIGINT_TYPE = Type.getType(BigInt.class);
  static final Type MIXEDINT_TYPE = Type.getType(long.class);  // fake type, but should occupy two slots
  static final Type BOXED_BOOLEAN_TYPE = Type.getType(Boolean.class);
  static final Type BOXED_DOUBLE_TYPE = Type.getType(Double.class);
  static final Type FUNCTION_TYPE = Type.getType(MethodHandle.class);
  
  /*
  public static jdart.compiler.type.Type unconvert(Type type) {
    switch(type.getSort()) {
    case VOID:
      return CoreTypeRepository.VOID_TYPE;
    case INT:
      return CoreTypeRepository.INT32_TYPE;
    case BOOLEAN:
      return CoreTypeRepository.BOOL_NON_NULL_TYPE;
    default:  // objects
      if (type == BIGINT_TYPE) {
        return CoreTypeRepository.INT_TYPE;
      }
      if (type == BOXED_BOOLEAN_TYPE) {
        return CoreTypeRepository.BOOL_TYPE;
      }
      if (type == BOXED_DOUBLE_TYPE) {
        return CoreTypeRepository.DOUBLE_TYPE;
      }
      return CoreTypeRepository.DYNAMIC_TYPE;
    }
  }*/
  
  public enum TypeContext {
    PARAMETER_TYPE,
    RETURN_TYPE,
    VAR_TYPE
  }
  
  public static List<Type> asJVMTypes(List<jdart.compiler.type.Type> parameterTypes, TypeContext typeContext) {
    ArrayList<Type> typeList = new ArrayList<>(parameterTypes.size());
    for(jdart.compiler.type.Type type: parameterTypes) {
      typeList.add(asJVMType(type, typeContext));
    }
    return typeList;
  }
  public static Type asJVMType(jdart.compiler.type.Type type, final TypeContext typeContext) {
    return type.accept(new TypeVisitor<Type, Void>() {
      @Override
      public Type visitBoolType(BoolType type, Void unused) {
        if (type.isNullable())
          return BOXED_BOOLEAN_TYPE;
        return BOOLEAN_TYPE;
      }
      @Override
      public Type visitIntType(IntType type, Void unused) {
        if (type.isIncludeIn(CoreTypeRepository.INT32_TYPE)) {
          return INT_TYPE;
        }
        if (type.hasCommonValuesWith(CoreTypeRepository.INT32_TYPE)) {
          if (typeContext == TypeContext.RETURN_TYPE) {
            return INT_TYPE;
          }
          if (typeContext == TypeContext.PARAMETER_TYPE) {
            return BIGINT_TYPE;
          }
          return MIXEDINT_TYPE;
        }
        return BIGINT_TYPE;
      }
      @Override
      public Type visitDoubleType(DoubleType type, Void unused) {
        if (type.isNullable())
          return BOXED_DOUBLE_TYPE;
        return DOUBLE_TYPE;
      }
      @Override
      public Type visitDynamicType(DynamicType type, Void unused) {
        return OBJECT_TYPE;
      }
      @Override
      public Type visitVoidType(VoidType type, Void unused) {
        return VOID_TYPE;
      }
      @Override
      public Type visitNullType(NullType type, Void unused) {
        return OBJECT_TYPE;
      }
      @Override
      public Type visitFunctionType(FunctionType type, Void unused) {
        return FUNCTION_TYPE;
      }
      @Override
      public Type visitInterfaceType(InterfaceType type, Void unused) {
        return OBJECT_TYPE;
      }
      @Override
      public Type visitArrayType(ArrayType type, Void unused) {
        return OBJECT_TYPE;
      }
      @Override
      public Type visitUnionType(UnionType unionType, Void unused) {
        List<Type> list = unionType.collect(this, null);
        Iterator<Type> it = list.iterator();
        Type type = it.next();
        while(it.hasNext()) {
          Type type2 = it.next();
          if (type.equals(type2)) {
            continue;
          }
          type = OBJECT_TYPE;
        }
        return type;
      }
    }, null);
  }
}
