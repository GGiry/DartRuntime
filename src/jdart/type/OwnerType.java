package jdart.type;

import java.util.List;

import com.google.dart.compiler.resolver.Element;

/**
 * Common abstract class for all types that have a super class and interfaces.
 * 
 */
public abstract class OwnerType extends NullableType {
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
  
  
  public abstract Element localLookupMember(String name);
  
  
  public Element lookupMember(String name) {
    Element element = localLookupMember(name);
    if (element != null) {
      return element;
    }
    
    InterfaceType superType = getSuperType();
    if (superType != null) {
      element = superType.lookupMember(name);
      if (element != null) {
        return element;
      }
    }
    
    for(InterfaceType interfaze: getInterfaces()) {
      element = interfaze.lookupMember(name);
      if (element != null) {
        return element;
      }
    }
    return null;
  }
  
  @Override
  NullableType merge(NullableType type) {
    if (!(type instanceof OwnerType)) {
      return super.merge(type);
    }
    OwnerType ownerType = (OwnerType) type;
    
    // isOwnerTypeAssignable don't care about nullability
    boolean nullable = isNullable() || type.isNullable();
    if (Types.isOwnerTypeAssignable(this, ownerType)) {
      return (nullable)? type.asNullable(): type;
    }
    if (Types.isOwnerTypeAssignable(ownerType, this)) {
      return (nullable)? this.asNullable(): this;
    }
    return super.merge(type);
  }
}
