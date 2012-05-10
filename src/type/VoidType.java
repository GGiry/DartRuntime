package type;

public class VoidType extends AbstractType {

  VoidType(boolean isNullable) {
    super(isNullable);
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitVoidType(this, parameter);
  }

  @Override
  public String getName() {
    return "void";
  }

  @Override
  public Object asConstant() {
    return VOID_VALUE;
  }

  @Override
  public AbstractType asNullable() {
    return new VoidType(true);
  }

  @Override
  public AbstractType asNonNull() {
    return new VoidType(false);
  }
}
