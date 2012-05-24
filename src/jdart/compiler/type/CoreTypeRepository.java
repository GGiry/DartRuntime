package jdart.compiler.type;

import java.math.BigInteger;
import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.CoreTypeProvider;

public class CoreTypeRepository extends TypeRepository {
  private final CoreTypeProvider coreTypeProvider;

  public final static BoolType BOOL_TYPE = new BoolType(true, null);
  public final static BoolType BOOL_NON_NULL_TYPE = new BoolType(false, null);
  public final static BoolType TRUE_TYPE = new BoolType(false, true);
  public final static BoolType FALSE_TYPE = new BoolType(false, false);

  public final static IntType INT_TYPE = new IntType(true, null, null);
  public final static IntType INT_NON_NULL_TYPE = new IntType(false, null, null);
  public final static IntType INT32_TYPE = new IntType(true, BigInteger.valueOf(Integer.MIN_VALUE), BigInteger.valueOf(Integer.MAX_VALUE));
  public final static IntType POSITIVE_INT32_TYPE = new IntType(true, BigInteger.ZERO, BigInteger.valueOf(Integer.MAX_VALUE));
  public final static IntType NEGATIVE_INT32_TYPE = new IntType(true, BigInteger.valueOf(Integer.MIN_VALUE), BigInteger.ZERO);
  
 /* public final static IntType POSITIVE_INT32 = new IntType(true, BigInteger.ZERO, BigInteger.valueOf((((long) 1) << 32) - 1));
  public final static IntType NEGATIVE_INT32 = new IntType(true, BigInteger.valueOf(- ((((long) 1) << 32) - 1)), BigInteger.ZERO);*/

  public final static DoubleType DOUBLE_TYPE = new DoubleType(true, null);
  public final static DoubleType DOUBLE_NON_NULL_TYPE = new DoubleType(false, null);

  public final static VoidType VOID_TYPE = new VoidType();

  public final static DynamicType DYNAMIC_TYPE = new DynamicType(true);
  public final static DynamicType DYNAMIC_NON_NULL_TYPE = new DynamicType(false);

  public final static NullType NULL_TYPE = new NullType();  
  
  private final InterfaceType stringType;
  private final InterfaceType functionType;

  //public static final InterfaceType INTERFACE_TYPE = new InterfaceType(true, null, null);

  private CoreTypeRepository(CoreTypeProvider coreTypeProvider) {
    super(null);
    this.coreTypeProvider = coreTypeProvider;

    // fill up with primitive type,
    // this must be done *after* the initialization of
    // the field coreTypeProvider

    map.put(getBoolClassElement(), BOOL_TYPE);
    map.put(getIntClassElement(), INT_TYPE);
    map.put(getDoubleClassElement(), DOUBLE_TYPE);
    // TODO maybe add void here ??
    
    stringType = createInterfaceType(coreTypeProvider.getStringType().getElement());
    functionType = createInterfaceType(coreTypeProvider.getFunctionType().getElement());
  }

  private static CoreTypeRepository CORE_TYPE_REPOSITORY;

  public static CoreTypeRepository initCoreTypeRepository(CoreTypeProvider coreTypeProvider) {
    if (CORE_TYPE_REPOSITORY != null) {
      if (CORE_TYPE_REPOSITORY.coreTypeProvider != coreTypeProvider) {
        throw new IllegalStateException("a core type repository with a different core provider");
      }
      return CORE_TYPE_REPOSITORY;
    }

    return CORE_TYPE_REPOSITORY = new CoreTypeRepository(coreTypeProvider);
  }

  static CoreTypeRepository getCoreTypeRepository() {
    Objects.requireNonNull(CORE_TYPE_REPOSITORY);
    return CORE_TYPE_REPOSITORY;
  }
  
  
  public InterfaceType getStringType() {
    return stringType;
  }
  public InterfaceType getFunctionType() {
    return functionType;
  }

  
  // class elements of lazy evaluated primitive types
  
  ClassElement getBoolClassElement() {
    return coreTypeProvider.getBoolType().getElement();
  }

  ClassElement getIntClassElement() {
    return coreTypeProvider.getIntType().getElement();
  }

  ClassElement getDoubleClassElement() {
    return coreTypeProvider.getDoubleType().getElement();
  }
  
  ClassElement getFunctionClassElement() {
    return coreTypeProvider.getFunctionType().getElement();
  }
}