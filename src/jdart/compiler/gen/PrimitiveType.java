package jdart.compiler.gen;

public enum PrimitiveType implements JVMType {
  BOOLEAN,
  INT,
  DOUBLE,
  NUM,
  VOID
  ;
  
  @Override
  public boolean isPrimitive() {
    return true;
  }
}
