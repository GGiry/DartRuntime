package type;

public class VoidType implements Type {

  VoidType() {
    // enforce singleton
  }

  @Override
  public boolean isNullable() {
    throw new IllegalStateException("void type");
  }

  @Override
  public AbstractType asNullable() {
    throw new IllegalStateException("void type");
  }

  @Override
  public AbstractType asNonNull() {
    throw new IllegalStateException("void type");
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitVoidType(this, parameter);
  }

  @Override
  public Object asConstant() {
    throw new IllegalStateException("void type");
  }
}
