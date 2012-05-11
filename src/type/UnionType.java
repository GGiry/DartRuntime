package type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class UnionType extends AbstractType {
  // each component type should be non nullable and not a union type
  private final HashSet<AbstractType> types;

  private UnionType(boolean nullable, HashSet<AbstractType> types) {
    super(nullable);
    this.types = types;
  }

  static UnionType createUnionType(AbstractType type1, AbstractType type2) {
    boolean nullable = type1.isNullable() || type2.isNullable();
    HashSet<AbstractType> types = new HashSet<>();
    types.add(type1.asNonNull());
    types.add(type2.asNonNull());
    return new UnionType(nullable, types);
  }

  @Override
  public int hashCode() {
    return isNullable() ? 1 : 0 ^ types.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof UnionType)) {
      return false;
    }
    UnionType unionType = (UnionType) obj;
    return isNullable() == unionType.isNullable() && types.equals(unionType.types);
  }

  @Override
  public String toString() {
    return "union" + super.toString() + types;
  }

  @Override
  public UnionType asNullable() {
    if (isNullable()) {
      return this;
    }
    return new UnionType(true, types);
  }

  @Override
  public UnionType asNonNull() {
    if (!isNullable()) {
      return this;
    }
    return new UnionType(false, types);
  }

  @Override
  AbstractType merge(AbstractType type) {
    if (type instanceof UnionType) {
      UnionType unionType = (UnionType) type;
      
      if (this.types.size() >= unionType.types.size())
        return reduce(this, unionType.isNullable(), unionType.types);
      return reduce(unionType, isNullable(), types);
    }
    
    return reduce(this, type.isNullable(), Collections.singleton(type.asNonNull()));
  }
  
  /**
   * Create a new union from a union and a collection of types.
   * 
   * @param unionType an union type
   * @param nullable  is the collection is nullable
   * @param collection must have at least one element and each element must be non null.
   * @return the new union or one type if it can be reduced to one element
   */
  private static AbstractType reduce(UnionType unionType, boolean nullable, Collection<AbstractType> collection) {
    // first filter out abstract type from collection that already exists in the union
    HashSet<AbstractType> unionSet = unionType.types;
    ArrayList<AbstractType> candidates = new ArrayList<>(collection.size());
    for(AbstractType type: collection) {
      assert !type.isNullable();
      assert !(type instanceof UnionType);
      
      if (unionSet.contains(type)) {
        continue;
      }
      candidates.add(type);
    }
    
    if (candidates.isEmpty()) {
      return (nullable)? unionType.asNullable(): unionType;
    }
    
    // compute nullability
    nullable |= unionType.isNullable();
    
    HashSet<AbstractType> newUnionSet = new HashSet<>(unionSet);
    Iterator<AbstractType> candidateIt = candidates.iterator();
    AbstractType candidate = candidateIt.next();
    
    loop: for(;;) {
      Iterator<AbstractType> it = newUnionSet.iterator();
      while(it.hasNext()) {
        AbstractType type = it.next();
        AbstractType merge = candidate.merge(type);
        
        // if merge is not a UnionType,
        // then types were successfully merged
        if (!(merge instanceof UnionType)) {
          it.remove();
          candidate = merge;
          continue loop;
        }
      }
     
      newUnionSet.add(candidate);
      
      if (candidateIt.hasNext()) {
        candidate = candidateIt.next();
        continue loop;
      }
      
      if (newUnionSet.size() == 1) {
        AbstractType singleton = newUnionSet.iterator().next();
        return (nullable)? singleton.asNullable(): singleton;
      }
      return new UnionType(nullable, newUnionSet);
    }
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitUnionType(this, parameter);
  }

  @Override
  public Object asConstant() {
    return null;
  }
}
