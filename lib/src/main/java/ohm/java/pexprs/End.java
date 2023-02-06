package ohm.java.pexprs;

import ohm.java.*;

public class End extends Prim {
	private static final End instance = new End();

	public static final End getInstance() {
		return instance;
	}

	private End() {
		super();
	}

	@Override
	public boolean eval(MatchState matchState, InputStream inputStream, int originalPosition) {
		if (inputStream.atEnd()) {
			matchState.pushBinding(TerminalNode.get(0), originalPosition);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("end");
	}

	@Override
	public String recipeName() {
		return "end";
	}
}
