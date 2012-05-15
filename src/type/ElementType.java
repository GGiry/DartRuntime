package type;

import java.util.ArrayList;
import java.util.List;

import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.Element;

/**
 * Base class for all type that own members.
 */
abstract class ElementType extends OwnerType {
  private Object superType = NOT_INITIALIZED; // lazy allocated
  private ArrayList<InterfaceType> interfaces; // lazy allocated

  private static final Object NOT_INITIALIZED = new Object();
  
  ElementType(boolean isNullable) {
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

  @Override
  public InterfaceType getSuperType() {
    if (superType != NOT_INITIALIZED) {
      return (InterfaceType)superType;
    }
    com.google.dart.compiler.type.InterfaceType superclass = getElement().getSupertype();
    if (superclass == null) {
      this.superType = null;
      return null;
    }
    Type superType = getTypeRepository().findType(isNullable(), superclass.getElement());
    this.superType = superType;
    return (InterfaceType)superType; 
  }

  @Override
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
  
  @Override
  public Element localLookupMember(String name) {
    return getElement().lookupLocalElement(name);
  }
}
