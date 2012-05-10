package type;

import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.CoreTypeProvider;

public class CoreTypeRepository extends TypeRepository {
  private final CoreTypeProvider coreTypeProvider;

  public final static BoolType BOOL_TYPE = new BoolType(true, null);
  public final static BoolType BOOL_NON_NULL_TYPE = new BoolType(false, null);
  public final static BoolType TRUE = new BoolType(false, true);
  public final static BoolType FALSE = new BoolType(false, false);

  public final static IntType INT_TYPE = new IntType(true, null, null);
  public final static IntType INT_NON_NULL_TYPE = new IntType(false, null, null);

  public final static DoubleType DOUBLE_TYPE = new DoubleType(true, null);
  public final static DoubleType DOUBLE_NON_NULL_TYPE = new DoubleType(false, null);

  public final static StringType STRING_TYPE = new StringType(true, null);
  public final static StringType STRING_NON_NULL_TYPE = new StringType(false, null);
  
  public final static VoidType VOID_TYPE = new VoidType(true);
  public final static VoidType VOID_NON_NULL_TYPE = new VoidType(false);
  
  public final static NullType NULL_TYPE = new NullType();

  private CoreTypeRepository(CoreTypeProvider coreTypeProvider) {
    super(null);
    this.coreTypeProvider = coreTypeProvider;

    // fill up with primitive type,
    // this must be done *after* the initialization of
    // the field coreTypeProvider

    map.put(getBoolClassElement(), BOOL_TYPE);
    map.put(getIntClassElement(), INT_TYPE);
    map.put(getDoubleClassElement(), DOUBLE_TYPE);
    map.put(getStringClassElement(), STRING_TYPE);
    // TODO maybe add void here ??
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

  ClassElement getBoolClassElement() {
    return coreTypeProvider.getBoolType().getElement();
  }

  ClassElement getIntClassElement() {
    return coreTypeProvider.getIntType().getElement();
  }

  ClassElement getDoubleClassElement() {
    return coreTypeProvider.getDoubleType().getElement();
  }

  ClassElement getStringClassElement() {
    return coreTypeProvider.getStringType().getElement();
  }
}
