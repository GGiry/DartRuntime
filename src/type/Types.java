package type;

import static type.CoreTypeRepository.NULL_TYPE;

public class Types {
  public static Type union(Type type1, Type type2) {
    if (type1 == NULL_TYPE) {
      return type2.asNullable();
    }
    if (type2 == NULL_TYPE) {
      return type1.asNullable();
    }
    return ((OwnerType) type1).merge((OwnerType) type2);
  }
}
