package jdart.compiler.type;

import java.util.ArrayList;
import java.util.List;

public class ArrayType extends NullableType {
  private final ArrayList<Type> values = new ArrayList<>();

  public ArrayType(boolean isNullable, List<Type> types) {
    super(isNullable);
    values.addAll(types);
  }

  @Override
  public List<Type> asConstant() {
    return values;
  }
  
  @Override
  public String toString() {
    return "array" + super.toString() + ' ' + values;
  }
  
  @Override
  public NullableType asNullable() {
    return isNullable() ? this : new ArrayType(false, values);
  }

  @Override
  public NullableType asNonNull() {
    return isNullable() ? new ArrayType(false, values) : this;
  }
  
  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    visitor.visitArrayType(this, parameter);
    return null;
  }

  public Type getType(int min) {
    return values.get(min);
  }
  
  public int getSize() {
    return values.size();
  }
}
