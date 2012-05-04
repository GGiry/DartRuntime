package type;

import java.math.BigInteger;

public class IntType extends AbstractType {
	private final boolean maxInfinity;
	private final boolean minInfinity;
	private final BigInteger max;
	private final BigInteger min;

	public IntType(boolean isNullable, boolean maxInfinity, boolean minInfinity, 
			BigInteger max, BigInteger min) {
		super(isNullable);
		this.maxInfinity = maxInfinity;
		this.minInfinity = minInfinity;
		this.max = max;
		this.min = min;
	}

	public BigInteger getMax() {
		return max;
	}
	
	public BigInteger getMin() {
		return min;
	}
	
	public boolean maxIsInfinity() {
		return maxInfinity;
	}

	public boolean minIsInfinity() {
		return minInfinity;
	}
}
