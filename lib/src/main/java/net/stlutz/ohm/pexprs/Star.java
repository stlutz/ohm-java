package net.stlutz.ohm.pexprs;

public class Star extends Iter {
	public static final char operator = '*';
	public static final int minNumMatches = 0;
	public static final int maxNumMatches = Integer.MAX_VALUE;

	public Star(PExpr expr) {
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
		return new Star(expr);
	}

	@Override
	public String recipeName() {
		return "star";
	}
}
