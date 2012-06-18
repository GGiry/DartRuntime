package jdart.compiler.flow;

import java.util.List;

import jdart.compiler.type.Type;

import com.google.dart.compiler.resolver.MethodElement;

public interface MethodCallResolver {
  public Type methodCall(String methodName, Type receiverType, List<Type> argumentType, Type expectedType, boolean virtual);

  public Type functionCall(MethodElement nodeElement, List<Type> argumentTypes, Type expectedType);

}