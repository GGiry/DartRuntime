package jdart.compiler.type;

import java.util.ArrayList;
import java.util.List;

public class ArrayType extends NullableType {
  private final ArrayList<Integer> dimensionSize = new ArrayList<>();
  private final ArrayList<Type> values = new ArrayList<>();

  public ArrayType(boolean isNullable, List<Type> types) {
    super(isNullable);
    values.addAll(types);
  }

  @Override
  public Object asConstant() {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public String toString() {
    return "array" + super.toString();
  }
  
  @Override
  public NullableType asNullable() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NullableType asNonNull() {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    // TODO Auto-generated method stub
    return null;
  }  
}
