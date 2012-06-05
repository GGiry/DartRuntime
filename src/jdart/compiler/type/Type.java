package jdart.compiler.type;

public interface Type {
  /**
   * Returns whenever or not the current type allows null.
   * 
   * @return true is the current type is nullable, false otherwise.
   */
  boolean isNullable();

  /**
   * Returns the nullable type of the current type.
   * 
   * @return the nullable type of the current type.
   */
  Type asNullable();

  /**
   * Returns the non null type of the current type.
   * 
   * @return the non null type of the current type.
   */
  Type asNonNull();


  Type asNullable(boolean nullable);

  /**
   * Visitor's accept method (doubble dispatch).
   * 
   * @param visitor
   *          the type visitor.
   * @param parameter
   *          the parameter
   * @return the return value
   */
  <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter);

  /**
   * Returns the constant value or null if the type is not constant. The value
   * {@link NullType#NULL_VALUE} is used to represent the constant value
   * {@code null}.
   * 
   * @return the constant value or null if the type is not constant.
   * 
   * @see #NULL_VALUE
   */
  Object asConstant();

  public static final Object NULL_VALUE = new Object();

  /**
   * Transforms the current type to a new type by applying the mapping
   * specified by the {@link TypeMapper}.
   * If the specified transformation returns {@code null}, then
   * the return type should be the non null dynamic type.
   * 
   * @param typeMapper calculate the transformation from one type to another.
   * @return the new type (never null).
   * 
   * @see UnionType#map(TypeMapper)
   */
  Type map(TypeMapper typeMapper);

  /**
   * Returns common values between this and the other type.
   * 
   * @param type
   *          Type to use to check common values.
   * @return Type containing common values between this and the other type.
   */
  Type commonValuesWith(Type type);

  Type invert();

  Type lessThanOrEqualsValues(Type other, boolean inLoop);

  Type lessThanValues(Type other, boolean inLoop);

  Type exclude(Type other);
}
