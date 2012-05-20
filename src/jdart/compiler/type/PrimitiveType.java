package jdart.compiler.type;

import com.google.dart.compiler.resolver.ClassElement;

abstract class PrimitiveType extends ElementType {
  private ClassElement element; // lazy found

  PrimitiveType(boolean isNullable) {
    super(isNullable);
  }

  @Override
  CoreTypeRepository getTypeRepository() {
    return CoreTypeRepository.getCoreTypeRepository();
  }

  @Override
  public ClassElement getElement() {
    if (element != null) {
      return element;
    }
    return element = getLazyElement();
  }

  abstract ClassElement getLazyElement();
}
