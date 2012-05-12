package type;

import java.util.List;

/**
 * Common interface for all types that have a super class and interfaces.
 * 
 */
public abstract class OwnerType extends AbstractType {
  OwnerType(boolean nullable) {
    super(nullable);
  }
  
  /**
   * Returns the super type of the current type.
   * 
   * @return the super type of the current type or null otherwise.
   */
  public abstract InterfaceType getSuperType();

  /**
   * Returns the interfaces of the current type.
   * 
   * @return the interfaces of the current type or an empty list.
   */
  public abstract List<InterfaceType> getInterfaces();
  
  @Override
  AbstractType merge(AbstractType type) {
    if (!(type instanceof OwnerType)) {
      return super.merge(type);
    }
    OwnerType ownerType = (OwnerType) type;
    if (Types.isAssignable(this, ownerType)) {
      return type;
    }
    if (Types.isAssignable(ownerType, this)) {
      return this;
    }
    return super.merge(type);
  }
}
