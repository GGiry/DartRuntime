package type;


public class DynamicType extends AbstractType {

  public DynamicType() {
    super(false);
  }
  
  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    visitor.visitDynamicType(this, parameter);
    return null;
  }

  @Override
  public Object asConstant() {
    return null;
  }

  @Override
  public String getName() {
    return "dynamic";
  }

  @Override
  public AbstractType asNullable() {
    throw new IllegalStateException("dynamic type");
  }

  @Override
  public AbstractType asNonNull() {
    return this;
  }
}
