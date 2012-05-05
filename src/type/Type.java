package type;

public interface Type {
	boolean isNullable();
	
	/**
	 * Returns the constant value or null if the type is not constant.
	 * The value {@link NullType#NULL_VALUE} is used to represent the constant value {@code null}.
	 * 
	 * @return the constant value or null if the type is not constant.
	 * 
	 * @see #NULL_VALUE
	 */
	Object asConstant();
	
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
	
	public static final Object NULL_VALUE = new Object();
}
