package type;

import static type.CoreTypeRepository.NULL_TYPE;

import java.util.Objects;

public class Types {
  public static Type union(Type type1, Type type2) {
    Objects.requireNonNull(type1);
    Objects.requireNonNull(type2);
    if (type1 == NULL_TYPE) {
      return type2.asNullable();
    }
    if (type2 == NULL_TYPE) {
      return type1.asNullable();
    }
    return ((AbstractType) type1).merge((AbstractType) type2);
  }
}
