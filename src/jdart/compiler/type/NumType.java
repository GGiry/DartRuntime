package jdart.compiler.type;

public interface NumType extends Type {
  /**
   * Returns the result of the operation add on this type and the specified type.
   * 
   * @param other Type to add.
   * @return The result or null if other is not computable.
   */
  Type add(Type other);

  /**
   * Returns the result of the operation subtract on this type and the specified type.
   * 
   * @param other Type to subtract.
   * @return The result or null if other is not computable.
   */
  Type sub(Type other);

  /**
   * Returns the result of the operation modulo on this type and the specified type.
   * 
   * @param other Type to compute.
   * @return The result or null if other is not computable.
   */
  Type mod(Type other);
}
