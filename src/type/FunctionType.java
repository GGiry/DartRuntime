package type;

public class FunctionType extends AbstractType {

  FunctionType(boolean isNullable) {
    super(isNullable);
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitFunctionType(this, parameter);
  }

  @Override
  public Object asConstant() {
    return null;
  }

  @Override
  public String getName() {
    return "function";
  }

  @Override
  public AbstractType asNullable() {
    if (isNullable()) {
      return this;
    }
    return new FunctionType(false);
  }

  @Override
  public AbstractType asNonNull() {
    if (!isNullable()) {
      return this;
    }
    return new FunctionType(true);
  }
}
