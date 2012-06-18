package jdart.compiler.flow;

import java.util.Map;

import jdart.compiler.type.Type;

import com.google.dart.compiler.ast.DartNode;

public class ProfileInfo {
  private final Type returnType;
  private final /*maybenull*/Map<DartNode, Type> typeMap;

  ProfileInfo(Type returnType, /*maybenull*/Map<DartNode, Type> typeMap) {
    this.returnType = returnType;
    this.typeMap = typeMap;
  }
  
  public Type getReturnType() {
    return returnType;
  }
  
  public /*maybenull*/Map<DartNode, Type> getTypeMap() {
    return typeMap;
  }
}