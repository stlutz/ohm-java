package ohm.java.pexprs;

import ohm.java.InputStream;
import ohm.java.MatchState;

public class Seq extends Aggregation {
	public Seq(PExpr[] terms) {
		super(terms);
	}

	@Override
	protected Aggregation newInstance(PExpr[] terms) {
		return new Seq(terms);
	}

	@Override
	public int getArity() {
		int arity = 0;
		for (PExpr term : terms) {
			arity += term.getArity();
		}
		return arity;
	}

	@Override
	public boolean eval(MatchState matchState, InputStream inputStream, int originalPosition) {
		for (PExpr term : terms) {
			if (!matchState.eval(term)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected String getOperator() {
		return " ";
	}

	@Override
	public String recipeName() {
		return "seq";
	}
}
