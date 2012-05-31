package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class UnionType extends NullableType {
  // each component type should be non nullable and not a union type
  private final HashSet<NullableType> types;

  private UnionType(boolean nullable, HashSet<NullableType> types) {
    super(nullable);
    this.types = types;
  }

  private static void sortedAdd(HashSet<NullableType> set, NullableType type) {
    set.add(type);
  }

  static UnionType createUnionType(NullableType type1, NullableType type2) {
    boolean nullable = type1.isNullable() || type2.isNullable();
    HashSet<NullableType> types = new HashSet<>();
    sortedAdd(types, type1.asNonNull());
    sortedAdd(types, type2.asNonNull());
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
    HashSet<NullableType> unionSet = unionType.types;
    ArrayList<NullableType> candidates = new ArrayList<>(collection.size());
    for (NullableType type : collection) {
      assert !type.isNullable();
      assert !(type instanceof UnionType);

      if (unionSet.contains(type)) {
        continue;
      }
      candidates.add(type);
    }

    if (candidates.isEmpty()) {
      return (nullable) ? unionType.asNullable() : unionType;
    }

    // compute nullability
    nullable |= unionType.isNullable();

    HashSet<NullableType> newUnionSet = new HashSet<>(unionSet);
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

  @Override
  public Type map(TypeMapper typeMapper) {
    Type resultType = null;
    for (Type type : types) {
      Type mappedType = typeMapper.transform(type);
      System.out.println("res: " + resultType);
      System.out.println("map: " + mappedType);
      if (mappedType == null) {
        continue;
      }
      if (resultType == null) {
        resultType = mappedType;
        continue;
      }
      resultType = Types.union(resultType, mappedType);
      System.out.println("union: " + resultType);
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
    LinkedList<BigInteger> list = new LinkedList<>();
    HashSet<NullableType> finalSet = new HashSet<>();
    boolean minNull = false;
    boolean maxNull = false;
    BigInteger min = null;
    BigInteger max = null;

    for (Type type : types) {
      if (type instanceof IntType) {
        IntType iType = (IntType) type;
        BigInteger minBound = iType.getMinBound();
        BigInteger maxBound = iType.getMaxBound();
        if (minBound != null) {
          BigInteger minSubtract = minBound.subtract(BigInteger.ONE);
          list.add(minSubtract);
          if (min == null || minSubtract.compareTo(min) < 0) {
            min = minSubtract;
          }
        } else {
          minNull = true;
        }
        if (maxBound != null) {
          BigInteger maxAdd = maxBound.add(BigInteger.ONE);
          list.add(maxAdd);
          if (max == null || maxAdd.compareTo(max) < 0) {
            max = maxAdd;
          }
        } else {
          maxNull = true;
        }
      } else {
        finalSet.add((NullableType) type.invert());
      }
    }

    Collections.sort(list);

    int i = 0;
    BigInteger last = null;
    for (BigInteger value : list) {
      i++;
      if (!minNull) {
        finalSet.add(new IntType(isNullable(), null, value));
        minNull = true;
        continue;
      }
      if (!maxNull && i == list.size()) {
        finalSet.add(new IntType(isNullable(), value, null));
        maxNull = true;
        continue;
      }

      if (last == null) {
        last = value;
      } else {
        finalSet.add(new IntType(isNullable(), last, value));
        last = null;
      }
    }

    return new UnionType(isNullable(), finalSet);
  }

  @Override
  public Type LTEValues(final Type other) {
    return map(new TypeMapper() {
      @Override
      public Type transform(Type type) {
        Type lteValues = type.LTEValues(other);
        if (lteValues == VOID_TYPE) {
          return type;
        }
        return lteValues;
      }
    });
  }

  @Override
  public Type LTValues(final Type other) {
    return map(new TypeMapper() {
      @Override
      public Type transform(Type type) {
        Type ltValues = type.LTValues(other);
        if (ltValues == VOID_TYPE) {
          return type;
        }
        return ltValues;
      }
    });
  }

  public Type GTEValues(final Type other) {
    return map(new TypeMapper() {
      @Override
      public Type transform(Type type) {
        Type lteValues = other.LTEValues(type);
        if (lteValues == VOID_TYPE) {
          return type;
        }
        return lteValues;
      }
    });
  }

  public Type GTValues(final Type other) {
    return map(new TypeMapper() {
      @Override
      public Type transform(Type type) {
        Type ltValues = other.LTValues(type);
        if (ltValues == VOID_TYPE) {
          return type;
        }
        return ltValues;
      }
    });
  }
}
