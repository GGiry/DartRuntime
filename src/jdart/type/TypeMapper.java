package jdart.type;

/**
 * Transform a type to another type.
 * 
 * @see Type#map(TypeMapper)
 */
public interface TypeMapper {
  /**
   * Transform a type to another one.
   * 
   * @param type the type to transform.
   * @return a type to transform or null.
   * 
   * @see Type#map(TypeMapper)
   */
  public /*maybenull*/Type transform(Type type);
}
