package type;

import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.CoreTypeProvider;

public class CoreTypeRepository extends TypeRepository {
  private final CoreTypeProvider coreTypeProvider;

  private CoreTypeRepository(CoreTypeProvider coreTypeProvider) {
    super(null);
    this.coreTypeProvider = coreTypeProvider;
    
    // fill up with primitive type,
    // this must be done *after* the initialization of
    // the field coreTypeProvider
    map.put(getBoolClassElement(), Types.BOOL_TYPE);
    map.put(getIntClassElement(), Types.INT_TYPE);
    map.put(getDoubleClassElement(), Types.DOUBLE_TYPE);
    //TODO maybe add void here ??
  }
  
  private static CoreTypeRepository coreTypeRepository;
  
  public static CoreTypeRepository getCoreTypeRepository() {
    return Objects.requireNonNull(coreTypeRepository);
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
  
  public static CoreTypeRepository createCoreTypeRepository(CoreTypeProvider coreTypeProvider) {
    Objects.requireNonNull(coreTypeProvider);
    if (coreTypeRepository != null) {
      throw new IllegalStateException("core type repository already initialized");
    }
    return coreTypeRepository = new CoreTypeRepository(coreTypeProvider);
  }
}
