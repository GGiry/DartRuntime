package jdart.compiler.gen;

import org.objectweb.asm.Type;

public class Var {
  private final Type type;
  private final int slot;

  public Var(Type type, int slot) {
    this.type = type;
    this.slot = slot;
  }
  
  public Type getType() {
    return type;
  }
  public int getSlot() {
    return slot;
  }
  
  @Override
  public String toString() {
    return "[" + type.getClassName() + ", " + slot + "]";
  }
}
