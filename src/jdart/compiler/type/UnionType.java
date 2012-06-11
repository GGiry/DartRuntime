package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

// TODO not sure if it need to implements NumType or just add the methods without @Override annotation
public class UnionType extends NullableType implements NumType {
  // each component type should be non nullable and not a union type
  private final LinkedHashSet<NullableType> types;

  private UnionType(boolean nullable, LinkedHashSet<NullableType> types) {
    super(nullable);
    this.types = types;
  }

  static UnionType createUnionType(NullableType type1, NullableType type2) {
    boolean nullable = type1.isNullable() || type2.isNullable();
    LinkedHashSet<NullableType> types = new LinkedHashSet<>();
    if (type1 instanceof IntType && type2 instanceof IntType) {
      IntType iType1 = (IntType) type1;
      IntType iType2 = (IntType) type2;
      if (iType1.getMinBound() == null || iType1.getMinBound().compareTo(iType2.getMinBound()) < 0) {
        types.add(type1.asNonNull());
        types.add(type2.asNonNull());
      } else {
        types.add(type2.asNonNull());
        types.add(type1.asNonNull());
      }
    } else {
      types.add(type1.asNonNull());
      types.add(type2.asNonNull());
    }
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
  NullableType merge(NullableType type) {
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
   * @param unionType
   *          an union type
   * @param nullable
   *          is the collection is nullable
   * @param collection
   *          must have at least one element and each element must be non null.
   * @return the new union or one type if it can be reduced to one element
   */
  private static NullableType reduce(UnionType unionType, boolean nullable, Collection<NullableType> collection) {
    // first filter out abstract type from collection that already exists in the
    // union
    LinkedHashSet<NullableType> unionSet = unionType.types;
    LinkedList<NullableType> candidates = new LinkedList<>();
    //ArrayList<NullableType> candidates = new ArrayList<>(collection.size());
    for (NullableType type : collection) {
      assert !type.isNullable();
      assert !(type instanceof UnionType);

      if (unionSet.contains(type)) {
        if (!(type instanceof IntType)) {
          continue;
        }
        unionSet.remove(type);
      }
      candidates.add(type);
    }

    if (candidates.isEmpty()) {
      return (nullable) ? unionType.asNullable() : unionType;
    }

    // compute nullability
    nullable |= unionType.isNullable();

    LinkedHashSet<NullableType> newUnionSet = new LinkedHashSet<>(unionSet);
    Iterator<NullableType> candidateIt = candidates.iterator();
    NullableType candidate = candidateIt.next();

    loop: for (;;) {
      Iterator<NullableType> it = newUnionSet.iterator();
      while (it.hasNext()) {
        NullableType type = it.next();
        NullableType merge = candidate.merge(type);

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
        NullableType singleton = newUnionSet.iterator().next();
        return (nullable) ? singleton.asNullable() : singleton;
      }
      newUnionSet = sortUnionSet(newUnionSet);
      return new UnionType(nullable, newUnionSet);
    }
  }

  /**
   * Sort unionSet for reduce method.
   * 
   * Only sort the {@link IntType} instances.
   * 
   * @param newUnionSet Set to sort.
   * @return Sorted set.
   */
  private static LinkedHashSet<NullableType> sortUnionSet(LinkedHashSet<NullableType> unionSet) {
    LinkedList<IntType> intTypes = new LinkedList<>();
    LinkedHashSet<NullableType> result = new LinkedHashSet<>();

    for (NullableType candidate : unionSet) {
      if (candidate instanceof IntType) {
        intTypes.add((IntType) candidate);
      } else {
        result.add(candidate);
      }
    }

    Collections.sort(intTypes, new Comparator<IntType>() {
      @Override
      public int compare(IntType o1, IntType o2) {
        if (o1.getMinBound() == null || o2.getMaxBound() == null) {
          return -1;
        }
        if (o2.getMinBound() == null || o1.getMaxBound() == null) {
          return 1;
        }
        return o1.getMinBound().compareTo(o2.getMinBound());
      }
    });

    result.addAll(intTypes);
    return result;
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitUnionType(this, parameter);
  }

  @Override
  public Object asConstant() {
    return null;
  }

  @Override
  public Type map(TypeMapper typeMapper) {
    Type resultType = null;
    for (Type type : types) {
      Type mappedType = typeMapper.transform(type);
      if (mappedType == null) {
        continue;
      }
      if (resultType == null) {
        resultType = mappedType;
        continue;
      }
      resultType = Types.union(resultType, mappedType);
    }
    if (resultType != null) {
      return resultType;
    }
    return DYNAMIC_NON_NULL_TYPE;
  }

  @Override
  public Type commonValuesWith(final Type other) {
    return map(new TypeMapper() {
      @Override
      public Type transform(Type type) {
        return type.commonValuesWith(other);
      }
    });
  }

  @Override
  public Type invert() {    
    LinkedHashSet<NullableType> result = new LinkedHashSet<>();
    boolean minIsDone = false;
    boolean maxIsDone = false;
    BigInteger last = null;

    for (Type type : types) {
      if (type instanceof IntType) {
        IntType iType = (IntType) type;
        BigInteger minBound = iType.getMinBound();
        BigInteger maxBound = iType.getMaxBound();

        if (maxBound == null) {
          maxIsDone = true;
        }

        if (!minIsDone) {
          if (maxBound != null) {
            last = maxBound.add(BigInteger.ONE);
          } else {
            last = null;
          }
          if (minBound != null) {
            result.add(new IntType(isNullable(), null, minBound.subtract(BigInteger.ONE)));
          }
          minIsDone = true;
        } else {
          if (last != null) {
            result.add(new IntType(isNullable(), last, minBound.subtract(BigInteger.ONE)));
            if (maxBound != null) {
              last = maxBound.add(BigInteger.ONE);
            } else {
              last = null;
            }
          }
        }

      } else {
        NullableType invert = (NullableType) type.invert();
        if (invert != null) {
          result.add(invert);
        }
      }
    }

    if (!maxIsDone) {
      result.add(new IntType(isNullable(), last, null));
    }

    if (result.isEmpty()) {
      return null;
    }
    if (result.size() == 1) {
      NullableType[] array = new NullableType[1];
      result.toArray(array);
      return array[0];
    }
    return new UnionType(isNullable(), result);
  }

  @Override
  public Type lessThanOrEqualsValues(final Type other, final boolean inLoop) {
    return map(new TypeMapper() {
      @Override
      public Type transform(Type type) {
        if (type instanceof NumType) {
          Type lteValues = ((NumType) type).lessThanOrEqualsValues(other, inLoop);
          if (lteValues == VOID_TYPE) {
            return type;
          }
          return lteValues;
        } 
        return null;
      }
    });
  }

  @Override
  public Type lessThanValues(final Type other, final boolean inLoop) {
    return map(new TypeMapper() {
      @Override
      public Type transform(Type type) {
        if (type instanceof NumType) {
          Type ltValues = ((NumType) type).lessThanValues(other, inLoop);
          if (ltValues == VOID_TYPE) {
            return type;
          }
          return ltValues;
        }
        return null;
      }
    });
  }

  @Override
  public Type greaterThanOrEqualsValues(final Type other, final boolean inLoop) {
    return map(new TypeMapper() {
      @Override
      public Type transform(Type type) {
        Type gteValues = ((NumType) type).greaterThanOrEqualsValues(other, inLoop);
        if (gteValues == VOID_TYPE) {
          return type;
        }
        return gteValues;
      }
    });
  }

  @Override
  public Type greaterThanValues(final Type other, final boolean inLoop) {
    return map(new TypeMapper() {
      @Override
      public Type transform(Type type) {
        Type ltValues = ((NumType) type).greaterThanValues(other, inLoop);
        if (ltValues == VOID_TYPE) {
          return type;
        }
        return ltValues;
      }
    });
  }

  @Override
  public Type exclude(final Type other) {
    return map(new TypeMapper() {

      @Override
      public Type transform(Type type) {
        return type.exclude(other);
      }
    });
  }

  @Override
  public Type add(final Type other) {
    return map(new TypeMapper() {

      @Override
      public Type transform(Type type) {
        if (type instanceof NumType)  {
          return ((NumType) type).add(other);
        }
        return null;
      }
    });
  }

  public Type sub(final Type other) {
    return map(new TypeMapper() {

      @Override
      public Type transform(Type type) {
        if (type instanceof NumType)  {
          return ((NumType) type).sub(other);
        }
        return null;
      }
    });
  }

  public Type mod(final Type other) {
    return map(new TypeMapper() {

      @Override
      public Type transform(Type type) {
        if (type instanceof NumType)  {
          return ((NumType) type).mod(other);
        }
        return null;
      }
    });
  }

  public Type reverseSub(final Type other) {
    return map(new TypeMapper() {

      @Override
      public Type transform(Type type) {
        if (type instanceof NumType)  {
          return ((NumType) other).sub(type);
        }
        return null;
      }
    });
  }

  public Type reverseMod(final Type other) {
    return map(new TypeMapper() {

      @Override
      public Type transform(Type type) {
        if (type instanceof NumType)  {
          return ((NumType) other).mod(type);
        }
        return null;
      }
    });
  }

  public boolean containsType(Type parentWidening) {
    for (NullableType type : types) {
      if (type.equals(parentWidening)) {
        return true;
      }
    }
    return false;
  }
}
