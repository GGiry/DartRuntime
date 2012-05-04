package type;

public abstract class AbstractType implements Type {
	private final boolean isNullable;

	public AbstractType(boolean isNullable) {
		this.isNullable = isNullable;
	}

	public boolean isNullable() {
		return isNullable;
	}
}
