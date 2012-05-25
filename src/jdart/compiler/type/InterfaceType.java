package jdart.compiler.type;

import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

public class InterfaceType extends ElementType {
  private final ClassElement element;
  private final TypeRepository typeRepository;
  private transient InterfaceType dualType;

  InterfaceType(boolean nullable, TypeRepository typeRepository, ClassElement element) {
    super(nullable);
    this.typeRepository = typeRepository;
    this.element = element;
  }

  void postInitDualType(InterfaceType dualType) {
    this.dualType = dualType;
  }

  @Override
  public int hashCode() {
    return (isNullable() ? 1 : 0) ^ Objects.hashCode(getElement());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InterfaceType)) {
      return false;
    }

    InterfaceType interfaceType = (InterfaceType) obj;
    return isNullable() == interfaceType.isNullable() && getElement().equals(interfaceType.getElement());
  }

  @Override
  public ClassElement getElement() {
    return element;
  }

  @Override
  public String toString() {
    return getElement().getName() + super.toString();
  }

  @Override
  TypeRepository getTypeRepository() {
    return typeRepository;
  }

  @Override
  public InterfaceType asNullable() {
    return (isNullable()) ? this : dualType;
  }

  @Override
  public InterfaceType asNonNull() {
    return (!isNullable()) ? this : dualType;
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitInterfaceType(this, parameter);
  }

  @Override
  public Object asConstant() {
    return null;
  }

  @Override
  public Type commonValuesWith(Type type) {
    if (type instanceof InterfaceType) {
      return (equals(type)) ? this : null;
    }

    if (type instanceof UnionType) {
      return ((UnionType) type).commonValuesWith(this);
    }

    return null;
  }
  
  @Override
  public Type invert() {
    // TODO 
    return null;
  }
}
