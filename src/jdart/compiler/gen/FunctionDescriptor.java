package jdart.compiler.gen;

import java.util.List;

import org.objectweb.asm.Type;

class FunctionDescriptor {
  private final Type returnType;
  private final List<Type> parameterTypes;
  
  public FunctionDescriptor(Type returnType, List<Type> parameterTypes) {
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
  }
  
  @Override
  public int hashCode() {
    return returnType.hashCode() ^ parameterTypes.hashCode();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FunctionDescriptor)) {
      return false;
    }
    FunctionDescriptor signature = (FunctionDescriptor)o;
    return returnType.equals(signature.returnType) &&
        parameterTypes.equals(signature.parameterTypes);
  }
  
  @Override
  public String toString() {
    return parameterTypes + " -> " + returnType;
  }
  
  public Type getReturnType() {
    return returnType;
  }
  
  public List<Type> getParameterTypes() {
    return parameterTypes;
  }
  
  public String getDescriptor() {
    return Type.getMethodDescriptor(returnType, parameterTypes.toArray(new Type[parameterTypes.size()]));
  }
}
