package jdart.compiler.flow;

import java.util.List;
import java.util.Map;

import jdart.compiler.type.Type;

import com.google.dart.compiler.ast.DartNode;

public class ProfileInfo {
  private final Type returnType;
  private final List<Type> parameterTypes;
  private final /*maybenull*/Map<DartNode, Type> typeMap;
  private final /*maybenull*/Map<DartNode, Liveness> livenessMap;

  ProfileInfo(Type returnType, List<Type> parameterTypes, /*maybenull*/Map<DartNode, Type> typeMap, /*maybenull*/Map<DartNode, Liveness> livenessMap) {
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
    this.typeMap = typeMap;
    this.livenessMap = livenessMap;
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
  
  public /*maybenull*/Map<DartNode, Liveness> getLivenessMap() {
    return livenessMap;
  }
}