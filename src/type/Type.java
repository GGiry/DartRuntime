package type;

public interface Type {
  /**
   * Returns whenever or not the current type allows null. 
   * @return true is the current type is nullable, false otherwise. 
   */
	boolean isNullable();
	
	/** 
	 * Returns the nullable type of the current type.
	 * @return the nullable type of the current type.
	 */
	Type asNullable();
	
	/** 
   * Returns the non null type of the current type.
   * @return the non null type of the current type.
   */
	Type asNonNull();
	
	/**
	 * Visitor's accept method (doubble dispatch).
	 * 
	 * @param visitor the type visitor.
	 * @param parameter the parameter
	 * @return the return value
	 */
	<R,P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter);
	
	/**
   * Returns the constant value or null if the type is not constant.
   * The value {@link NullType#NULL_VALUE} is used to represent the constant value {@code null}.
   * 
   * @return the constant value or null if the type is not constant.
   * 
   * @see #NULL_VALUE
   */
  Object asConstant();
	
	public static final Object NULL_VALUE = new Object();
}
