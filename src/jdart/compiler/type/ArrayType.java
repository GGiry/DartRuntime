package jdart.compiler.type;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

public class ArrayType extends NullableType {
  private final Type componentType;
  private final IntType length;
  private final/* maybenull */List<Type> constantItemTypes;

  ArrayType(boolean isNullable, Type componentType, IntType length, /* maybenull */List<Type> constantItemTypes) {
    super(isNullable);
    this.componentType = Objects.requireNonNull(componentType);
    this.length = Objects.requireNonNull(length);
    this.constantItemTypes = constantItemTypes;
  }

  /**
   * Creates an array of type from a list of item's type.
   * 
   * @param constantItemTypes
   *          a list of type of array components.
   * @return an array type
   */
  public static ArrayType constant(List<Type> constantItemTypes) {
    if (constantItemTypes.isEmpty()) {
      throw new IllegalArgumentException("empty constant items");
    }
    assert constantItemTypes instanceof RandomAccess;
    Type componentType = constantItemTypes.get(0);
    int length = constantItemTypes.size();
    for (int i = 1; i < length; i++) {
      componentType = Types.union(componentType, constantItemTypes.get(i));
    }
    return new ArrayType(false, componentType, IntType.constant(BigInteger.valueOf(length)), constantItemTypes);
  }

  /**
   * Creates a type representing an array of type with a component type and a
   * length.
   * 
   * @param componentType
   *          the component type of this array
   * @param length
   *          the length of this array
   * @return an array of type
   */
  public static ArrayType arrayType(NullableType componentType, IntType length) {
    return new ArrayType(false, componentType, length, null);
  }

  @Override
  public int hashCode() {
    return (isNullable() ? 1 : 0) ^ length.hashCode() ^ Objects.hashCode(constantItemTypes) ^ Integer.rotateLeft(componentType.hashCode(), 16);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ArrayType)) {
      return false;
    }
    ArrayType arrayType = (ArrayType) obj;
    return isNullable() == arrayType.isNullable() && componentType.equals(arrayType.componentType) && Objects.equals(length, arrayType.length)
        && Objects.equals(constantItemTypes, arrayType.constantItemTypes);
  }

  /**
   * Returns the component type of this array.
   * 
   * @return the component type of this array.
   */
  public Type getComponentType() {
    return componentType;
  }

  /**
   * Returns the length of this array.
   * 
   * @return the length of this array.
   */
  public IntType getLength() {
    return length;
  }

  @Override
  public String toString() {
    if (constantItemTypes == null) {
      return componentType.toString() + '[' + length + ']' + super.toString();
    }
    return componentType.toString() + constantItemTypes + super.toString();
  }

  @Override
  public NullableType asNullable() {
    return isNullable() ? this : new ArrayType(true, componentType, length, constantItemTypes);
  }

  @Override
  public NullableType asNonNull() {
    return isNullable() ? new ArrayType(false, componentType, length, constantItemTypes) : this;
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitArrayType(this, parameter);
  }

  @Override
  public List<Type> asConstant() {
    return constantItemTypes;
  }

  @Override
  public Type commonValuesWith(Type type) {
    if (type instanceof ArrayType) {
      return equals(type) ? this : null;
    }

    if (type instanceof UnionType) {
      return ((UnionType) type).commonValuesWith(this);
    }

    return null;
  }

  @Override
  public Type invert() {
    return null;
  }
  
  @Override
  public Type exclude(Type other) {
    return null;
  }

  @Override
  public Type LTEValues(Type other) {
    return null;
  }

  @Override
  public Type LTValues(Type other) {
    return null;
  }
}
