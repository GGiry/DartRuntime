package type;

import java.util.ArrayList;
import java.util.List;

import com.google.dart.compiler.resolver.ClassElement;

/**
 * Base class for all type that own members.
 */
abstract class OwnerType extends AbstractType implements Type {
  private InterfaceType superType; // lazy allocated
  private ArrayList<InterfaceType> interfaces; // lazy allocated

  OwnerType(boolean isNullable) {
    super(isNullable);
  }

  abstract TypeRepository getTypeRepository();

  /**
   * Returns the corresponding Dart compiler resolved element. A owner type has
   * only one corresponding class element but one class element can correspond
   * to many types.
   * 
   * @return the corresponding Dart compiler resolved element.
   */
  public abstract ClassElement getElement();

  /**
   * Returns the super type of the current type.
   * 
   * @return the super type of the current type or null otherwise.
   */
  public InterfaceType getSuperType() {
    if (superType != null) {
      return superType;
    }
    return superType = (InterfaceType) getTypeRepository().findType(isNullable(), getElement().getSupertype().getElement());
  }

  /**
   * Returns the super interfaces of the current type.
   * 
   * @return the super interfaces of the current type or null otherwise.
   */
  public List<InterfaceType> getInterfaces() {
    if (interfaces != null) {
      return interfaces;
    }

    ArrayList<InterfaceType> interfaces = new ArrayList<>();
    for (com.google.dart.compiler.type.InterfaceType interfaze : getElement().getInterfaces()) {
      interfaces.add((InterfaceType) getTypeRepository().findType(isNullable(), interfaze.getElement()));
    }
    return this.interfaces = interfaces;
  }
}
