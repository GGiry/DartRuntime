package jdart.compiler.gen;

public enum PrimitiveType implements JVMType {
  BOOLEAN,
  INT,
  BIGINT,
  DOUBLE,
  VOID
  ;
  
  @Override
  public boolean isPrimitive() {
    return true;
  }
}
