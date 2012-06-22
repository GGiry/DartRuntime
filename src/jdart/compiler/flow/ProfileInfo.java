package jdart.compiler.flow;

import java.util.List;
import java.util.Map;

import jdart.compiler.type.Type;

import com.google.dart.compiler.ast.DartNode;

public class ProfileInfo {
  private final Type returnType;
  private final List<Type> parameterTypes;
  private final /*maybenull*/Map<DartNode, Type> typeMap;

  ProfileInfo(Type returnType, List<Type> parameterTypes, /*maybenull*/Map<DartNode, Type> typeMap) {
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
    this.typeMap = typeMap;
  }
  
  public Type getReturnType() {
    return returnType;
  }
  
  public List<Type> getParameterTypes() {
    return parameterTypes;
  }
  
  public /*maybenull*/Map<DartNode, Type> getTypeMap() {
    return typeMap;
  }
}