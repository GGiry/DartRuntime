package type;

import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.CoreTypeProvider;

public class CoreTypeRepository extends TypeRepository {
  private final CoreTypeProvider coreTypeProvider;

  private CoreTypeRepository(CoreTypeProvider coreTypeProvider) {
    super(null);
    this.coreTypeProvider = coreTypeProvider;
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
  
  public static void createCoreTypeRepository(CoreTypeProvider coreTypeProvider) {
    Objects.requireNonNull(coreTypeProvider);
    if (coreTypeRepository != null) {
      throw new IllegalStateException("core type repository already initialized");
    }
    coreTypeRepository = new CoreTypeRepository(coreTypeProvider);
  }
}
