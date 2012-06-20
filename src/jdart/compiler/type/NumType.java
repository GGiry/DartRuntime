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

  /**
   * Returns this as DoubleType
   * @return This as DoubleType
   */
  Type asDouble();

  /**
   * Returns invert of this.
   * double[5] will return double[-5]
   * int[4, 10] will return int[-4,10]
   * @return Invert of this.
   */
  Type unarySub();
}
