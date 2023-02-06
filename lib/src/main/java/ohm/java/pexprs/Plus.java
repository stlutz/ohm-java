package ohm.java.pexprs;

public class Plus extends Iter {
	public static final char operator = '+';
	public static final int minNumMatches = 1;
	public static final int maxNumMatches = Integer.MAX_VALUE;

	public Plus(PExpr expr) {
		super();
		this.expr = expr;
	}

	@Override
	public char getOperator() {
		return operator;
	}

	@Override
	public int getMinNumMatches() {
		return minNumMatches;
	}

	@Override
	public int getMaxNumMatches() {
		return maxNumMatches;
	}

	@Override
	protected Iter newInstance(PExpr expr) {
		return new Plus(expr);
	}

	@Override
	public String recipeName() {
		return "plus";
	}
}
