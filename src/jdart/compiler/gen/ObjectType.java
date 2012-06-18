package jdart.compiler.gen;

public class ObjectType implements JVMType {
  private final String internalName;

  public ObjectType(String internalName) {
    this.internalName = internalName;
  }
  
  @Override
  public int hashCode() {
    return internalName.hashCode();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ObjectType)) {
      return false;
    }
    return internalName.equals(((ObjectType)o).internalName);
  }
  
  public String getInternalName() {
    return internalName;
  }
  
  @Override
  public String toString() {
    return internalName;
  }
  
  @Override
  public boolean isPrimitive() {
    return false;
  }
}
