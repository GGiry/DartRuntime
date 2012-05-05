package type;

public class Types {
  public static final BoolType BOOL_TYPE = new BoolType(true, null);
  public static final BoolType BOOL_NON_NULL_TYPE = new BoolType(false, null);
  public static final BoolType TRUE = new BoolType(false, true);
  public static final BoolType FALSE = new BoolType(false, false);
  
  public static final IntType INT_TYPE = new IntType(true, null, null);
  public static final IntType INT_NON_NULL_TYPE = new IntType(false, null, null);
  
  public static final DoubleType DOUBLE_TYPE = new DoubleType(true, null);
  public static final DoubleType DOUBLE_NON_NULL_TYPE = new DoubleType(false, null);
  
  public static final NullType NULL_TYPE = new NullType();
  
  public static Type union(Type type1, Type type2) {
    if (type1 == NULL_TYPE) {
      return type2.asNullable();
    }
    if (type2 == NULL_TYPE) {
      return type1.asNullable();
    }
    return ((AbstractType)type1).merge((AbstractType)type2);
  }
}
