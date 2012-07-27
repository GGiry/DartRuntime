package jdart.compiler.flow;

import java.util.List;
import java.util.Map;

import jdart.compiler.type.Type;

import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.resolver.VariableElement;

public class ProfileInfo {
  private final Type returnType;
  private final List<Type> parameterTypes;
  private final /*maybenull*/Map<DartNode, Type> typeMap;
  private final /*maybenull*/Map<DartNode, Liveness> livenessMap;
  private final /*maybenull*/Map<DartNode, Map<VariableElement, Type>> phiTableMap;

  ProfileInfo(Type returnType, List<Type> parameterTypes, /*maybenull*/Map<DartNode, Type> typeMap, /*maybenull*/Map<DartNode, Liveness> livenessMap, Map<DartNode, Map<VariableElement, Type>> phiTableMap) {
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
    this.typeMap = typeMap;
    this.livenessMap = livenessMap;
    this.phiTableMap = phiTableMap;
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

  public Map<DartNode, Map<VariableElement, jdart.compiler.type.Type>> getPhiTableMap() {
    return phiTableMap;
  }
}