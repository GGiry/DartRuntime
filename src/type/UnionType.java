package type;

import java.util.HashSet;

public class UnionType extends AbstractType {
  private final HashSet<Type> types;
  
  private UnionType(boolean nullable, HashSet<Type> types) {
    super(nullable);
    this.types = types;
  }
  
  static UnionType createUnionType(Type type1, Type type2) {
    HashSet<Type> types = new HashSet<>();
    types.add(type1);
    types.add(type2);
    return new UnionType(type1.isNullable() || type2.isNullable(), types);
  }
  
  private static UnionType createUnionType(UnionType unionType, Type type) {
    HashSet<Type> types = new HashSet<>();
    types.addAll(unionType.types);
    types.add(type);
    return new UnionType(unionType.isNullable() || type.isNullable(), types);
  }
  
  private static UnionType createUnionType(UnionType unionType1, UnionType unionType2) {
    HashSet<Type> types = new HashSet<>();
    types.addAll(unionType1.types);
    types.addAll(unionType2.types);
    return new UnionType(unionType1.isNullable() || unionType2.isNullable(), types);
  }
  
  @Override
  public int hashCode() {
    return isNullable()?1: 0 ^ types.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof UnionType)) {
      return false;
    }
    UnionType unionType = (UnionType)obj;
    return isNullable() == unionType.isNullable() &&
           types.equals(unionType.types);
  }
  
  @Override
  public String toString() {
    return super.toString()+types;
  }
  
  @Override
  public String getName() {
    return "union";
  }
  
  @Override
  public Type asNullable() {
    if (isNullable()) {
      return this;
    }
    return new UnionType(true, types);
  }
  
  @Override
  public Type asNonNull() {
    if (!isNullable()) {
      return this;
    }
    return new UnionType(false, types);
  }
  
  @Override
  Type merge(AbstractType type) {
    if (type instanceof UnionType) {
      UnionType unionType = (UnionType)type;
      if (types.containsAll(unionType.types)) {
        if (unionType.isNullable()) {
          return asNullable();
        }
        return this;
      }
      if (unionType.types.containsAll(types)) {
        if (isNullable()) {
          return unionType.asNullable();
        }
        return unionType;
      }
      return createUnionType(this, unionType);
    }
    if (types.contains(type)) {
      if (type.isNullable()) {
        return asNullable();
      }
      return this;
    }
    
    return createUnionType(this, type);
  }
  
  @Override
  public <R,P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitUnionType(this, parameter);
  }
  
  @Override
  public Object asConstant() {
    return null;
  }
}
