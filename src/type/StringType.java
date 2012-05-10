package type;

import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

import static type.CoreTypeRepository.*;

public class StringType extends PrimitiveType {
  private final String constant;
  
  StringType(boolean isNullable, /* can be null */ String constant) {
    super(isNullable);
    this.constant = constant;
  }

  public static StringType constant(String constant) {
    return new StringType(false, constant);
  }
  
  @Override
  public int hashCode() {
    return (isNullable() ? 1 : 0) ^ Objects.hashCode(constant);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StringType)) {
      return false;
    }
    StringType stringType = (StringType) obj;
    return isNullable() == stringType.isNullable() && Objects.equals(constant, stringType.constant);
  }
  
  @Override
  ClassElement getLazyElement() {
    return CoreTypeRepository.getCoreTypeRepository().getStringClassElement();
  }
  
  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
     return visitor.visitStringType(this, parameter);
  }

  @Override
  public Object asConstant() {
    return constant;
  }

  @Override
  public String getName() {
    return "string";
  }

  @Override
  public String toString() {
    return super.toString() + ' ' + ((constant != null) ? constant : "");
  }

  @Override
  public AbstractType asNullable() {
    if (isNullable()) {
      return this;
    }
    if (constant == null) {
      return STRING_TYPE;
    }
    return new StringType(true, constant);
  }

  @Override
  public AbstractType asNonNull() {
    if (!isNullable()) {
      return this;
    }
    if (constant == null) {
      return STRING_NON_NULL_TYPE;
    }
    return new StringType(false, constant);
  }

  @Override
  AbstractType merge(AbstractType type) {
    if (type == STRING_TYPE) {
      return STRING_TYPE;
    }
    if (type == STRING_NON_NULL_TYPE) {
      return (isNullable())? STRING_TYPE: STRING_NON_NULL_TYPE;
    }
    if (!(type instanceof StringType)) {
      return super.merge(type);
    }
    if (this == STRING_TYPE) {
      return STRING_TYPE;
    }
    if (this == STRING_NON_NULL_TYPE) {
      return (isNullable())? STRING_TYPE: STRING_NON_NULL_TYPE;
    }
    return super.merge(type);
  }
}
