package type;

import com.google.dart.compiler.resolver.ClassElement;

abstract class PrimitiveType extends OwnerType {
  private ClassElement element;  // lazy allocated
  
  PrimitiveType(boolean isNullable) {
    super(isNullable);
  }
  
  @Override
  TypeRepository getTypeRepository() {
    return CoreTypeRepository.getCoreTypeRepository();
  }
  
  abstract ClassElement getLazyElement();

  @Override
  public ClassElement getElement() {
    if (element != null) {
      return element;
    }
    return this.element = getLazyElement();
  }
}
