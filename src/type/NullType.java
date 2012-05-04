package type;

public class NullType implements Type {
	@Override
	public boolean isNullable() {
		return true;
	}
}
