package jdart.compiler.gen;

import java.util.Iterator;
import java.util.List;

import jdart.compiler.type.ArrayType;
import jdart.compiler.type.BoolType;
import jdart.compiler.type.DoubleType;
import jdart.compiler.type.DynamicType;
import jdart.compiler.type.FunctionType;
import jdart.compiler.type.IntType;
import jdart.compiler.type.InterfaceType;
import jdart.compiler.type.NullType;
import jdart.compiler.type.Type;
import jdart.compiler.type.TypeVisitor;
import jdart.compiler.type.UnionType;
import jdart.compiler.type.VoidType;

import static jdart.compiler.type.CoreTypeRepository.*;

public class JVMTypes {
  public static final JVMType OBJECT_TYPE = new ObjectType("java/lang/Object");
  public static final JVMType METHODHANDLE_TYPE = new ObjectType("java/lang/invoke/MethodHandle");
  
  public static JVMType convert(Type type) {
    return type.accept(CONVERT_VISITOR, null);
  } // where
  private static final TypeVisitor<JVMType, Void> CONVERT_VISITOR = new TypeVisitor<JVMType, Void>() {
    @Override
    public JVMType visitBoolType(BoolType type, Void parameter) {
      return PrimitiveType.BOOLEAN;
    }
    @Override
    public JVMType visitIntType(IntType type, Void parameter) {
      if (type.isIncludeIn(INT32_TYPE)) {
        return PrimitiveType.INT;
      }
      return PrimitiveType.NUM;
    }
    @Override
    public JVMType visitDoubleType(DoubleType type, Void parameter) {
      return PrimitiveType.DOUBLE;
    }
    @Override
    public JVMType visitDynamicType(DynamicType type, Void parameter) {
      return OBJECT_TYPE;
    }
    @Override
    public JVMType visitVoidType(VoidType type, Void parameter) {
      return PrimitiveType.VOID;
    }
    @Override
    public JVMType visitNullType(NullType type, Void parameter) {
      return OBJECT_TYPE;
    }
    @Override
    public JVMType visitFunctionType(FunctionType type, Void parameter) {
      return METHODHANDLE_TYPE;
    }
    @Override
    public JVMType visitInterfaceType(InterfaceType type, Void parameter) {
      return OBJECT_TYPE;
    }
    @Override
    public JVMType visitArrayType(ArrayType type, Void parameter) {
      return OBJECT_TYPE;
    }
    @Override
    public JVMType visitUnionType(UnionType type, Void parameter) {
      List<JVMType> list = type.collect(this, null);
      Iterator<JVMType> it = list.iterator();
      JVMType jvmType = it.next();
      while(it.hasNext()) {
        JVMType jvmType2 = it.next();
        if (jvmType.equals(jvmType2)) {
          continue;
        }
        if (jvmType == PrimitiveType.NUM && jvmType2 == PrimitiveType.INT) {
          continue; 
        }
        if (jvmType == PrimitiveType.INT && jvmType2 == PrimitiveType.NUM) {
          jvmType = PrimitiveType.NUM;
          continue;
        }
        
        jvmType = OBJECT_TYPE;
      }
      return jvmType;
    }
  };
}
