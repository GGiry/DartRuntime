package type;

import java.util.List;

/**
 * Common interface for all types that have a super class and interfaces.
 * 
 */
public interface OwnerType extends Type {
  /**
   * Returns the super type of the current type.
   * 
   * @return the super type of the current type or null otherwise.
   */
  public InterfaceType getSuperType();

  /**
   * Returns the super interfaces of the current type.
   * 
   * @return the super interfaces of the current type or null otherwise.
   */
  public List<InterfaceType> getInterfaces();
}
